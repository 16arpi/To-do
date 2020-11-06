package com.pigeoff.todo

import com.heinrichreimersoftware.materialintro.app.IntroActivity
import android.os.Bundle
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide

class MainIntroActivity : IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_welcome_t)
            .description(R.string.intro_welcome_m)
            .image(R.drawable.cover)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_add_t)
            .description(R.string.intro_add_m)
            .image(R.drawable.bttm_help_1)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_edit_t)
            .description(R.string.intro_edit_m)
            .image(R.drawable.top_help_1)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_deletesingle_t)
            .description(R.string.intro_deletesingle_m)
            .image(R.drawable.top_help_2)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_edittitle_t)
            .description(R.string.intro_edittitle_m)
            .image(R.drawable.top_help_3)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_listechange_t)
            .description(R.string.intro_listechange_m1)
            .image(R.drawable.top_help_4)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_deleteall_t)
            .description(R.string.intro_deleteall_m)
            .image(R.drawable.bttm_help_2)
            .background(android.R.color.white)
            .build())

        addSlide(SimpleSlide.Builder()
            .title(R.string.intro_starting_t)
            .description(R.string.intro_starting_m)
            .background(android.R.color.white)
            .build())
    }
}
