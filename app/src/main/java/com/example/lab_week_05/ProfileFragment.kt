package com.example.profileapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lab_week_05.R
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private var userId: String? = null
    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize Views
        nameEditText = view.findViewById(R.id.edit_name)
        emailEditText = view.findViewById(R.id.edit_email)
        phoneEditText = view.findViewById(R.id.edit_phone)
        editButton = view.findViewById(R.id.btn_edit)
        saveButton = view.findViewById(R.id.btn_save)
        deleteButton = view.findViewById(R.id.btn_delete)

        // Set initial state
        setEditingEnabled(false)

        // Load user data
        loadUserProfile()

        // Set click listeners
        editButton.setOnClickListener {
            isEditMode = true
            setEditingEnabled(true)
        }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveUserProfile()
                isEditMode = false
                setEditingEnabled(false)
            }
        }

        deleteButton.setOnClickListener {
            deleteUserProfile()
        }

        return view
    }

    private fun setEditingEnabled(enabled: Boolean) {
        // Enable/disable EditTexts
        nameEditText.isEnabled = enabled
        emailEditText.isEnabled = enabled
        phoneEditText.isEnabled = enabled

        // Show/hide buttons
        editButton.visibility = if (enabled) View.GONE else View.VISIBLE
        saveButton.visibility = if (enabled) View.VISIBLE else View.GONE
        deleteButton.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun validateInputs(): Boolean {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return false
        }

        // Add email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun loadUserProfile() {
        val userRef = firestore.collection("users").document(getUserId())

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nameEditText.setText(document.getString("name"))
                    emailEditText.setText(document.getString("email"))
                    phoneEditText.setText(document.getString("phone"))
                    userId = document.id

                    // Enable edit button only if profile exists
                    editButton.isEnabled = true
                } else {
                    // If no profile exists, show message and enable editing
                    Toast.makeText(context, "No profile found. Create a new one.", Toast.LENGTH_SHORT).show()
                    isEditMode = true
                    setEditingEnabled(true)
                    editButton.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting document", e)
                Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()

        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone
        )

        if (userId != null) {
            // Update existing user profile
            firestore.collection("users").document(userId!!)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    editButton.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating document", e)
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Create a new user profile
            firestore.collection("users").add(user)
                .addOnSuccessListener { documentReference ->
                    userId = documentReference.id
                    Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    editButton.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding document", e)
                    Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteUserProfile() {
        userId?.let {
            firestore.collection("users").document(it)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile deleted successfully", Toast.LENGTH_SHORT).show()
                    // Clear the EditText fields
                    nameEditText.setText("")
                    emailEditText.setText("")
                    phoneEditText.setText("")
                    userId = null
                    // Reset to edit mode for new profile creation
                    isEditMode = true
                    setEditingEnabled(true)
                    editButton.isEnabled = false
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error deleting document", e)
                    Toast.makeText(context, "Failed to delete profile", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(context, "No profile to delete", Toast.LENGTH_SHORT).show()
    }

    private fun getUserId(): String {
        // Replace this with actual user ID retrieval
        return "exampleUserId"
    }
}