package edu.uw.eep523.sensorrtplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

const val MODE = "NULL"

class MainActivity : AppCompatActivity() {

    lateinit var fixmode: Button
    lateinit var freemode: Button
    lateinit var category: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fixmode = findViewById(R.id.fixrep)
        fixmode.text = "define repetitions"
        freemode = findViewById(R.id.freemode)
        freemode.text = "free mode"

        category = findViewById(R.id.category)
        category.text = "Mode Selection"
    }

    //send mode to next page, "extra"
    fun playwithfix (view: View) {
        val intent = Intent (this, FixRepetition::class.java).apply {
            putExtra(MODE, "fix")
        }
        startActivity(intent)
    }

    fun playWithFree (view: View) {
        val intent = Intent (this, FreeMode::class.java).apply {
            putExtra(MODE, "free")
        }
        startActivity(intent)
    }

}
