package edu.uw.eep523.sensorrtplot

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_fixrep.*


class FixRepetition : AppCompatActivity(), SensorEventListener {


    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    private lateinit var et: EditText


    private var mediaPlayer: MediaPlayer? = null


    private lateinit var times: TextView
    //lateinit var start: Button

    private var accelerationThreshold = 27F
    private val SHAKE_SLOP_TIME_MS = 500
    private var shaketime = 0L
    private var isColor = false
    private var work = false
    private var pullup = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixrep)
        //view.setBackgroundColor(Color.CYAN)


        times = findViewById(R.id.times)
        et = findViewById(R.id.editText)

        shaketime = System.currentTimeMillis()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        mSensor = if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Sorry, there are no accelerometers on your device.
            null!!
        }


        mSensorManager.registerListener(this, mSensor, 40000)

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }


    override fun onSensorChanged(event: SensorEvent) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER)
            return

        /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
       */
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]


        val magnitudeSquared = (ax * ax + ay * ay + az * az).toDouble()


//        if (magnitudeSquared > accelerationThreshold * accelerationThreshold) {
//            val now = System.currentTimeMillis()
//            if (now - shaketime < 200) {
//                return
//            }
////            shake.text = "shaked!!!"
////            shake.setTextColor (Color.parseColor("#0000ff"))
//            //Toast.makeText(applicationContext, "shake", Toast.LENGTH_SHORT)
//            shaketime = now
//
//            work = true
//            Toast.makeText(applicationContext, "shaked", Toast.LENGTH_SHORT).show()
       if (work) {
                if (event.values[1] >= 13f) {
                    val now = System.currentTimeMillis()
                    if (now - shaketime < 200) {
                        return
                    }
                    shaketime = now

                    if (et.text.toString() == "") {
                        Toast.makeText(applicationContext, "Please input times", Toast.LENGTH_SHORT).show()
                        return
                    }
                    //begin.text = "Target:"+et.getText()
//                    val bg = "Target:"+et.getText()
//                    begin.text = bg
//                    begin.setTextColor(Color.parseColor("#0000ff"))

                    pullup++
                    val temp = "pull-up numbers:$pullup"
                    times.text = temp
                    //times.text = "pull-up numbers:"+pullup.toString()
                    times.setTextColor(Color.parseColor("#0000ff"))

                    var target = et.text.toString().toInt()
                    if (pullup == target) {
                        mediaPlayer = MediaPlayer.create(this, R.raw.ring)
                        mediaPlayer?.start()
                        times.text = "Done!!!"
                        times.setTextColor(Color.parseColor("#0000ff"))
                        work = false

                    }
                }
            }
    }

    override fun onResume() {
        Log.d("tag","onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        //mediaPlayer?.stop()
        Log.d("tag","onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mSensorManager.unregisterListener(this)
    }

//    fun stop (view: View) {
//        work = false
//        Toast.makeText(applicationContext, "stopped", Toast.LENGTH_SHORT).show()
//    }

    fun start(View: View) {
        work = true;
        times.text = "You can start!"
        times.setTextColor(Color.parseColor("#0000ff"))
    }


}
