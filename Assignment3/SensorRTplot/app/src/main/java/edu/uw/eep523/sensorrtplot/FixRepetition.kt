package edu.uw.eep523.sensorrtplot

import android.content.Context
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
import android.widget.TextView
import android.widget.Toast
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicStampedReference


class FixRepetition : AppCompatActivity(), SensorEventListener {

    private  lateinit var mSeriesXaccel: LineGraphSeries<DataPoint>
    private lateinit var mSeriesYaccel: LineGraphSeries<DataPoint>
    private lateinit var mSeriesZaccel: LineGraphSeries<DataPoint>

    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    //private lateinit var view: View
    //private lateinit var mSensorG: Sensor

    private var mediaPlayer: MediaPlayer? = null



    private lateinit var shake: TextView

    val linear_acceleration: Array<Float> = arrayOf(0.0f,0.0f,0.0f)

    private var accelerationThreshold = 2.7F
    private val SHAKE_SLOP_TIME_MS = 500
    private var shaketime = 0L
    private var isColor = false
    private var work = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixrep)
        //view.setBackgroundColor(Color.CYAN)



        shake = findViewById(R.id.shake)

        shaketime = System.currentTimeMillis();
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        mSensor = if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Sorry, there are no accelerometers on your device.
            null!!
        }

        //mSensorG =  (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE))

        mSensorManager.registerListener(this, mSensor, 40000)



        // use less power and CPU resources, just reduce detection frequence

//        mSeriesXaccel = LineGraphSeries()
//        mSeriesYaccel = LineGraphSeries()
//        mSeriesZaccel = LineGraphSeries()
//        initGraphRT(mGraphX,mSeriesXaccel!!)
//        initGraphRT(mGraphY,mSeriesYaccel!!)
//        initGraphRT(mGraphZ,mSeriesZaccel!!)
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
        val ax = event.values[0] / SensorManager.GRAVITY_EARTH
        val ay = event.values[1] / SensorManager.GRAVITY_EARTH
        val az = event.values[2] / SensorManager.GRAVITY_EARTH

        val magnitudeSquared = (ax  * ax + ay * ay + az * az).toDouble()

        if (magnitudeSquared > accelerationThreshold * accelerationThreshold) {
            val now = System.currentTimeMillis();
            if (now - shaketime < 200) {
                return
            }
//            shake.text = "shaked!!!"
//            shake.setTextColor (Color.parseColor("#0000ff"))
            //Toast.makeText(applicationContext, "shake", Toast.LENGTH_SHORT)
            shaketime = now


            work = true
            if (!isColor) {
                shake.text = "shaked111"
                shake.setTextColor (Color.parseColor("#0000ff"))
                mediaPlayer = MediaPlayer.create(this, R.raw.ring)
                mediaPlayer?.start()

            } else {
                shake.text = "shaked222"
                shake.setTextColor (Color.parseColor("#0000ff"))
                //mediaPlayer = MediaPlayer.create(this, R.raw.ring)
                mediaPlayer?.pause()
                mediaPlayer?.stop()
                mediaPlayer?.release()

            }
            isColor = !isColor
        }

        //detect pull-UP
//        if (work) {
//            if (event.values[0] <= 3.0f) {
//
//            }









//        linear_acceleration[0] = event.values[0]
//        linear_acceleration[1] = event.values[1]
//        linear_acceleration[2] = event.values[2]
//
//
//        val xval = System.currentTimeMillis()/1000.toDouble()//graphLastXValue += 0.1
//        mSeriesXaccel!!.appendData(DataPoint(xval, linear_acceleration[0].toDouble()), true, 50)
//        mSeriesYaccel!!.appendData(DataPoint(xval, linear_acceleration[1].toDouble()), true, 50)
//        mSeriesZaccel!!.appendData(DataPoint(xval, linear_acceleration[2].toDouble()), true, 50)

    }

    // return true if the device is currently accelerating
//    private fun isAccelerating(event: SensorEvent): Boolean {
//        val ax = event.values[0] / SensorManager.GRAVITY_EARTH
//        val ay = event.values[1] / SensorManager.GRAVITY_EARTH
//        val az = event.values[2] / SensorManager.GRAVITY_EARTH
//
//        val magnitudeSquared = (ax  * ax + ay * ay + az * az).toDouble()
//        return magnitudeSquared > accelerationThreshold * accelerationThreshold
//    }


//    private fun initGraphRT(mGraph: GraphView, mSeriesXaccel :LineGraphSeries<DataPoint>){
//
//        mGraph.getViewport().setXAxisBoundsManual(true)
//        //mGraph.getViewport().setMinX(0.0)
//        //mGraph.getViewport().setMaxX(4.0)
//        mGraph.getViewport().setYAxisBoundsManual(true);
//
//
//        mGraph.getViewport().setMinY(0.0);
//        mGraph.getViewport().setMaxY(10.0);
//        mGraph.getGridLabelRenderer().setLabelVerticalWidth(100)
//
//        // first mSeries is a line
//        mSeriesXaccel.setDrawDataPoints(false)
//        mSeriesXaccel.setDrawBackground(false)
//        mGraph.addSeries(mSeriesXaccel)
//        setLabelsFormat(mGraph,1,2)
//
//    }

//    /* Formatting the plot*/
//    fun setLabelsFormat(mGraph:GraphView,maxInt:Int,maxFraction:Int){
//        val nf = NumberFormat.getInstance()
//        nf.setMaximumFractionDigits(maxFraction)
//        nf.setMaximumIntegerDigits(maxInt)
//
//        mGraph.getGridLabelRenderer().setVerticalAxisTitle("Accel data")
//        mGraph.getGridLabelRenderer().setHorizontalAxisTitle("Time")
//
//        mGraph.getGridLabelRenderer().setLabelFormatter(object : DefaultLabelFormatter(nf,nf) {
//            override fun formatLabel(value: Double, isValueX: Boolean): String {
//                return if (isValueX) {
//                    super.formatLabel(value, isValueX)+ "s"
//                } else {
//                    super.formatLabel(value, isValueX)
//                }
//            }
//        })
//
//    }


    override fun onResume() {
        Log.d("tag","onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("tag","onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager.unregisterListener(this)
    }


}
