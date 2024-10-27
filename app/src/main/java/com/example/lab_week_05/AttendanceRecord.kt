package com.example.lab_week_05

data class AttendanceRecord(
    val id: String,
    val status: String,
    val date: String,
    val time: String,
    val imageUrl: String?
)