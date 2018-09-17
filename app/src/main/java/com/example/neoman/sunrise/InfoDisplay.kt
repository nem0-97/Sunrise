package com.example.neoman.sunrise


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_info_display.*

class InfoDisplay : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_display)

        val b:Bundle=intent.extras
        Loc.text=b.getString("loc")
        Sunrise.text=b.getString("sunrise")
        Sunset.text=b.getString("sunset")
    }

}
