package com.example.lab_week_05

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        recyclerView = view.findViewById(R.id.attendance_recycler_view)
        sharedPreferences = requireActivity().getSharedPreferences("AbsensiPrefs", Context.MODE_PRIVATE)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadData()

        return view
    }

    private fun loadData() {
        val absenHistory = getAbsenHistory()

        // Prepare data for RecyclerView
        val attendanceList = mutableListOf<AttendanceItem>()
        for ((date, times) in absenHistory) {
            val absenMasuk = times["masuk"] ?: "No Data"
            val absenPulang = times["pulang"] ?: "No Data"
            attendanceList.add(AttendanceItem(date, absenMasuk, absenPulang))
        }

        // Set data to RecyclerView
        val adapter = AttendanceAdapter(attendanceList)
        recyclerView.adapter = adapter
    }

    private fun getAbsenHistory(): MutableMap<String, MutableMap<String, String>> {
        val jsonString = sharedPreferences.getString("absen_history", null)
        val type: Type = object : TypeToken<MutableMap<String, MutableMap<String, String>>>() {}.type
        return if (jsonString != null) gson.fromJson(jsonString, type) else mutableMapOf()
    }
}

data class AttendanceItem(val date: String, val masuk: String, val pulang: String)

class AttendanceAdapter(private val attendanceList: List<AttendanceItem>) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
        private val masukTextView: TextView = itemView.findViewById(R.id.masuk_text_view)
        private val pulangTextView:     TextView = itemView.findViewById(R.id.pulang_text_view)

        fun bind(attendanceItem: AttendanceItem) {
            dateTextView.text = attendanceItem.date
            masukTextView.text = attendanceItem.masuk
            pulangTextView.text = attendanceItem.pulang
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(attendanceList[position])
    }

    override fun getItemCount(): Int = attendanceList.size
}
