package com.pigeoff.todo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat

class HelpActivity : AppCompatActivity() {

    val PROMO_LAYOUT = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        this.window.statusBarColor = ContextCompat.getColor(this, R.color.ratp_vert)

        val promoBtn = findViewById<Button>(R.id.btnPlay2)
        if (PROMO_LAYOUT) findViewById<ConstraintLayout>(R.id.promoLayout).visibility = View.VISIBLE
        promoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.pigeoff.library"))
            startActivity(intent)
        }

        val btnClose = findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener {
            this.finish()
        }

        val btnGooglePlay = findViewById<Button>(R.id.btnPlay)
        btnGooglePlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.pigeoff.todo"))
            startActivity(intent)
        }
    }
}