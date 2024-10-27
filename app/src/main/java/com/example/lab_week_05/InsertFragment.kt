package com.example.lab_week_05

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class InsertFragment : Fragment() {
    private lateinit var currentTimeText: TextView
    private lateinit var absenMasukButton: Button
    private lateinit var absenPulangButton: Button
    private lateinit var captureImageButton: Button
    private lateinit var selectDateButton: Button
    private lateinit var photoImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    private val CAMERA_REQUEST_CODE = 100
    private var attendanceType: String? = null
    private var selectedDate: String? = null
    private var capturedImage: Bitmap? = null
    private var currentDocumentId: String? = null

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insert, container, false)

        // Initialize views
        initializeViews(view)

        // Initialize Firebase and SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AbsensiPrefs", Context.MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()

        // Setup initial state and listeners
        setupInitialState()
        setupClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        currentTimeText = view.findViewById(R.id.current_time_text)
        absenMasukButton = view.findViewById(R.id.absen_masuk_button)
        absenPulangButton = view.findViewById(R.id.absen_pulang_button)
        captureImageButton = view.findViewById(R.id.capture_image_button)
        selectDateButton = view.findViewById(R.id.select_date_button)
        photoImageView = view.findViewById(R.id.photo_image_view)
    }

    private fun setupInitialState() {
        updateTime()
        captureImageButton.isEnabled = false

        // Start time update timer
        startTimeUpdates()
    }

    private fun startTimeUpdates() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    updateTime()
                }
            }
        }, 0, 1000) // Update every second
    }

    private fun setupClickListeners() {
        absenMasukButton.setOnClickListener {
            handleAbsen("masuk")
        }

        absenPulangButton.setOnClickListener {
            handleAbsen("pulang")
        }

        captureImageButton.setOnClickListener {
            if (attendanceType != null) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Silakan lakukan absen terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }

        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.showBottomNavigation()
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedTime = sdf.format(currentTime)
        currentTimeText.text = "Waktu saat ini: $formattedTime"
    }

    private fun handleAbsen(type: String) {
        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Silakan pilih tanggal terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }

        val absenHistory = getAbsenHistory()
        val dailyRecord = absenHistory[selectedDate] ?: mutableMapOf()

        when (type) {
            "masuk" -> {
                if (dailyRecord["masuk"] != null) {
                    Toast.makeText(requireContext(), "Anda sudah absen masuk pada tanggal ini.", Toast.LENGTH_SHORT).show()
                    return
                }
                processAttendance(type)
            }
            "pulang" -> {
                if (dailyRecord["pulang"] != null) {
                    Toast.makeText(requireContext(), "Anda sudah absen pulang pada tanggal ini.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (dailyRecord["masuk"] == null) {
                    Toast.makeText(requireContext(), "Anda harus absen masuk terlebih dahulu.", Toast.LENGTH_SHORT).show()
                    return
                }
                processAttendance(type)
            }
        }
    }

    private fun processAttendance(type: String) {
        saveAbsenTime(type)
        attendanceType = type
        captureImageButton.isEnabled = true
        uploadAttendanceToFirestore(type)
    }

    private fun saveAbsenTime(type: String) {
        val absenHistory = getAbsenHistory()
        val absenTimes = absenHistory[selectedDate] ?: mutableMapOf()

        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val description = if (type == "masuk") "Masuk" else "Keluar"

        // Store both time and description
        absenTimes[type] = "$description: $currentTime"

        absenHistory[selectedDate!!] = absenTimes
        val editor = sharedPreferences.edit()
        editor.putString("absen_history", gson.toJson(absenHistory))
        editor.apply()
    }

    private fun uploadAttendanceToFirestore(type: String) {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        // Update the attendance data to include description
        val description = if (type == "masuk") "Masuk" else "Keluar"

        val attendanceData = hashMapOf(
            "status" to type,
            "date" to selectedDate,
            "time" to "$description: $currentTime",
            "imageUrl" to null,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "userId" to getUserId() // Assuming you have a method to get the current user's ID
        )

        firestore.collection("attendance")
            .add(attendanceData)
            .addOnSuccessListener { documentReference ->
                currentDocumentId = documentReference.id
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(
                    requireContext(),
                    "Absen $type berhasil dicatat pada $selectedDate jam $currentTime",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document", e)
                Toast.makeText(
                    requireContext(),
                    "Gagal mencatat absensi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun getUserId(): String {
        // Implement this method based on your authentication system
        return sharedPreferences.getString("user_id", "") ?: ""
    }

    private fun getAbsenHistory(): MutableMap<String, MutableMap<String, String>> {
        val jsonString = sharedPreferences.getString("absen_history", null)
        val type: Type = object : TypeToken<MutableMap<String, MutableMap<String, String>>>() {}.type
        return if (jsonString != null) gson.fromJson(jsonString, type) else mutableMapOf()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                Toast.makeText(requireContext(), "Tanggal terpilih: $selectedDate", Toast.LENGTH_SHORT).show()
                checkExistingAttendance(selectedDate!!)
            },
            year,
            month,
            day
        ).show()
    }

    private fun checkExistingAttendance(date: String) {
        firestore.collection("attendance")
            .whereEqualTo("date", date)
            .whereEqualTo("userId", getUserId())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val status = document.getString("status")
                    if (status != null) {
                        Log.d("Firestore", "Existing attendance found: $status")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting documents", e)
            }
    }

    private fun openCamera() {
        // Logic for opening the camera to capture a photo
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            capturedImage = imageBitmap
            photoImageView.setImageBitmap(capturedImage)
            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase() {
        // Logic for uploading the captured image to Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val baos = ByteArrayOutputStream()
        capturedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                updateImageUrlInFirestore(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateImageUrlInFirestore(imageUrl: String) {
        currentDocumentId?.let { documentId ->
            firestore.collection("attendance").document(documentId)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener {
                    Log.d("Firestore", "Image URL updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating image URL", e)
                }
        }
    }
}
