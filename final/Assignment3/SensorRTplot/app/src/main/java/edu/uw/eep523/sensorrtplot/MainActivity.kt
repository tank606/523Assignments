package edu.uw.eep523.sensorrtplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

const val MODE = "NULL"

class MainActivity : AppCompatActivity() {

    lateinit var easyMode: Button
    lateinit var category: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        easyMode = findViewById(R.id.fixrep)
        easyMode.text = "fix"

        category = findViewById(R.id.category)
        category.text = "mode selection"
    }

    //send mode to next page, "extra"
    fun playwithfix (view: View) {
        val intent = Intent (this, FixRepetition::class.java).apply {
            putExtra(MODE, "fix")
        }
        startActivity(intent)
    }

    }

