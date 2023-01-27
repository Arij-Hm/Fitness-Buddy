package com.example.tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import java.util.*
import com.example.tracker.Birth

class Birth : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

        val datePicker = findViewById<DatePicker>(R.id.DatePicker)
        val today = Calendar.getInstance()
        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)

        ) { view, year, month, day ->
            val month = month + 1
            val msg = "You Selected: $day/$month/$year"
            Toast.makeText(this@Birth, msg, Toast.LENGTH_SHORT).show()
        }
    }
}