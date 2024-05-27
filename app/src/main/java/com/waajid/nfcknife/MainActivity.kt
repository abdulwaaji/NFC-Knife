package com.waajid.nfcknife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSubmit = findViewById<Button>(R.id.btn_scan)
        btnSubmit.setOnClickListener {
            val intent = Intent(this, NfcActivity::class.java)
            startActivity(intent)
        }
    }
}