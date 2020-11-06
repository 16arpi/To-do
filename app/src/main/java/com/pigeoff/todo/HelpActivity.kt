package com.pigeoff.todo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class HelpActivity : AppCompatActivity() {

    val PROMO_LAYOUT = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        this.window.statusBarColor = ContextCompat.getColor(this, R.color.ratp_gris_bleu)

        val promoBtn = findViewById<Button>(R.id.btnPlay2)
        if (PROMO_LAYOUT) findViewById<ConstraintLayout>(R.id.promoLayout).visibility = View.VISIBLE
        promoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.pigeoff.library"))
            startActivity(intent)
        }

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener {
            this.finish()
        }

        val btnAssistance: ConstraintLayout = findViewById(R.id.helpLayout)
        btnAssistance.setOnClickListener {
            val intent = Intent(this, MainIntroActivity::class.java)
            startActivity(intent)
        }

        val btnGithub: ConstraintLayout = findViewById(R.id.githubLayout)
        btnGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(("https://github.com/16arpi/To-do")))
            startActivity(intent)
        }

        val btnGooglePlay = findViewById<ConstraintLayout>(R.id.rateLayout)
        btnGooglePlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.pigeoff.todo"))
            startActivity(intent)
        }

        val btnOpensource: ConstraintLayout = findViewById(R.id.opensourceLayout)
        btnOpensource.setOnClickListener {
            val notices = Notices()
            notices.addNotice(Notice("Androit Jetpack Library","https://developer.android.com/jetpack","", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Google Material Components","https://github.com/material-components/material-components-android","", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("material-intro","https://github.com/heinrichreimer/material-intro","Copyright (c) 2017 Jan Heinrich Reimer", MITLicense()))

            LicensesDialog.Builder(this)
                .setTitle(R.string.help_opensource)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .build()
                .show()
        }
    }
}