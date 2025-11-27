package com.example.agora.services

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateService {
    private const val DEFAULT_PATTERN = "dd/MM/yyyy HH:mm"

    fun format(date: Date, pattern: String = DEFAULT_PATTERN): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun parse(dateString: String, pattern: String = DEFAULT_PATTERN): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
