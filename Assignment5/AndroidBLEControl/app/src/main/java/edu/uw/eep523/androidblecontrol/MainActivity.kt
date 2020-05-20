package edu.uw.eep523.androidblecontrol


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import java.util.*
import kotlin.concurrent.schedule


const val DEVICE_NAME = "Kai_Tan"
const val EMERGENCY_CONTACT = "2020"

class MainActivity : AppCompatActivity(), BLEControl.Callback {


    // Bluetooth
    private var ble: BLEControl? = null
    private var messages: TextView? = null
    private var rssiAverage:Double = 0.0

    private var closeDialog: Boolean = false

    private var alert: AlertDialog? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        val adapter: BluetoothAdapter?
        adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            }        }

        // Get Bluetooth
        messages = findViewById(R.id.bluetoothText)
        messages!!.movementMethod = ScrollingMovementMethod()
        ble = BLEControl(applicationContext, DEVICE_NAME)

        // Check permissions
        ActivityCompat.requestPermissions(this,
                arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)


    }
    //Function that reads the RSSI value associated with the bluetooth connection between the phone and the Arudino board
    //If you use the RSSI to calculate distance, you may want to record a set of values over a period of time
    //and obtain the average
    override fun onRSSIread(uart:BLEControl,rssi:Int){
        rssiAverage = rssi.toDouble()
        writeLine("RSSI $rssiAverage")
    }
    fun getRSSI (v:View){
        ble!!.getRSSI()
    }

    fun clearText (v:View){
        messages!!.text=""

    }

    override fun onResume() {
        super.onResume()
        //updateButtons(false)
        ble!!.registerCallback(this)
    }

    override fun onStop() {
        super.onStop()
        ble!!.unregisterCallback(this)
        ble!!.disconnect()
    }

    fun connect(v: View) {
        startScan()
    }

    private fun startScan() {
        writeLine("Scanning for devices ...")
        ble!!.connectFirstAvailable()
    }


    /**
     * Press button to receive the temperature value form the board
     */
    fun buttTouch(v: View) {
        ble!!.send("readtemp")
        Log.i("BLE", "READ TEMP")
    }

    /**
     * Press button to set the lEDs to color red (see arduino code)
     */
    fun buttRed(v: View) {
        ble!!.send("red")
        Log.i("BLE", "SEND RED")
    }


    /**
     * Writes a line to the messages textbox
     * @param text: the text that you want to write
     */
    private fun writeLine(text: CharSequence) {
        runOnUiThread {
            messages!!.append(text)
            messages!!.append("\n")
        }
    }

    /**
     * Called when a UART device is discovered (after calling startScan)
     * @param device: the BLE device
     */
    override fun onDeviceFound(device: BluetoothDevice) {
        writeLine("Found device : " + device.name)
        writeLine("Waiting for a connection ...")
    }

    /**
     * Prints the devices information
     */
    override fun onDeviceInfoAvailable() {
        writeLine(ble!!.deviceInfo)
    }

    /**
     * Called when UART device is connected and ready to send/receive data
     * @param ble: the BLE UART object
     */
    override fun onConnected(ble: BLEControl) {
        writeLine("Connected!")

    }

    /**
     * Called when some error occurred which prevented UART connection from completing
     * @param ble: the BLE UART object
     */
    override fun onConnectFailed(ble: BLEControl) {
        writeLine("Error connecting to device!")
    }

    /**
     * Called when the UART device disconnected
     * @param ble: the BLE UART object
     */
    override fun onDisconnected(ble: BLEControl) {
        writeLine("Disconnected!")
    }

    /**
     * Called when data is received by the UART
     * @param ble: the BLE UART object
     * @param rx: the received characteristic
     */
    override fun onReceive(ble: BLEControl, rx: BluetoothGattCharacteristic) {
        writeLine("Received value: " + rx.getStringValue(0))
            runOnUiThread() {
                if (rx.getStringValue(0) == "detected") {
                    val alertDialog: AlertDialog? = this?.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setPositiveButton(
                                "OK",
                                DialogInterface.OnClickListener {
                                    //user press the positive button
                                        dialog, id ->
                                    ble!!.send("cancelnotice")
                                    dialog.cancel()
                                })
                            setNegativeButton("cancel",
                                DialogInterface.OnClickListener {
                                    // User cancelled the dialog
                                        dialog, id ->
                                    dialog.cancel()
                                })
                        }
// Set other dialog properties
                            .setMessage("Do you want to cancel the arlarm")
                            .setTitle("Arduino ALARM")
// Create the AlertDialog
                        builder.create()
                    }
                    //writeLine("Received value: " + "222")

                        alert = alertDialog
                        alert?.show()



                } else if (rx.getStringValue(0) == "dismiss") {
                   // writeLine("Received value: " + "333")
                    //alertDialog?.cancel()
                    alert?.dismiss()
                    alert = null

                } else if (rx.getStringValue(0) == "expired") {
                    try {
                        val uri: String = "tel:" + EMERGENCY_CONTACT
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CALL_PHONE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CALL_PHONE), 1
                            )
                        }
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {

                    }
                }



//                if (alertDialog != null && !alertDialog.isShowing()) {
//                    alertDialog?.show()
//
//                }



            //alertDialog?.show()

        }
    }



// permission response
//override fun onRequestPermissionsResult(requestCode: Int,
//                                        permissions: Array<String>, grantResults: IntArray) {
//    when (requestCode) {
//
//    }

    companion object {
        private val REQUEST_ENABLE_BT = 0
    }



//    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
//        return activity?.let {
//            // Use the Builder class for convenient dialog construction
//            val builder = AlertDialog.Builder(it)
//            builder.setMessage(R.string.dialog_fire_missiles)
//                .setPositiveButton(R.string.fire,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // FIRE ZE MISSILES!
//                    })
//                .setNegativeButton(R.string.cancel,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // User cancelled the dialog
//                    })
//            // Create the AlertDialog object and return it
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }


}

