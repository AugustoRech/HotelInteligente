package me.soekd.hotelinteligente

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.nio.charset.Charset
import java.util.*
import kotlin.math.roundToInt


class RoomActivity : AppCompatActivity() {

    companion object {

        private var MAC_ADDRESS: String? = null

        // Este UUID é padrão
        private val BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    }

    private lateinit var ledButton: ImageButton
    private lateinit var rainButton: ImageButton
    private lateinit var fireAlertButton: ImageButton
    private lateinit var airButton: ImageButton
    private lateinit var lockButton: ImageButton

    private lateinit var temperatureInfo: TextView
    private lateinit var humidityInfo: TextView

    private lateinit var temperature: CircularProgressBar
    private lateinit var humidity: CircularProgressBar

    private var led = false

    private var air = false
    private var locked = true

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mBluetoothSocket: BluetoothSocket? = null

    private var mBluetoothThread: BluetoothThread? = null

    override fun onDestroy() {
        mBluetoothSocket?.close()
        mBluetoothThread?.cancel()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)


        // para esconder uma barra que fica em cima por padrão.
        supportActionBar?.hide()

        MAC_ADDRESS = this.intent.getStringExtra("mac")

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(MAC_ADDRESS)

        if (mBluetoothDevice == null) {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(
                applicationContext,
                getString(R.string.BluetoothConnectDeviceError),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        mBluetoothSocket =
            mBluetoothDevice!!.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_UUID)

        if (mBluetoothSocket == null) {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(
                applicationContext,
                getString(R.string.DeviceSocketConnectError),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            mBluetoothSocket?.connect()
        } catch (exception: Exception) {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(
                applicationContext,
                getString(R.string.DeviceSocketConnectError),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        mBluetoothThread = BluetoothThread(mHandler, mBluetoothSocket!!)

        ledButton = findViewById(R.id.led_button)
        rainButton = findViewById(R.id.rain_button)
        fireAlertButton = findViewById(R.id.fire_alert_button)
        airButton = findViewById(R.id.air_button)
        lockButton = findViewById(R.id.lock_button)

        temperatureInfo = findViewById(R.id.temperature_info)
        humidityInfo = findViewById(R.id.humidity_info)


        // Definiar a progressbar (umidade e temperatura)
        // Para isso, estou usando esta biblioteca: https://github.com/lopspower/CircularProgressBar

        temperature = findViewById(R.id.temperature_progress_bar)
        temperature.apply {
            progress = 65f
            setProgressWithAnimation(65f, 1000) // =1s
        }

        humidity = findViewById(R.id.humidity_progress_bar)
        humidity.apply {
            progress = 10f
            setProgressWithAnimation(65f, 1000) // =1s
        }

        // lógica para mudar a imagem do led
        ledButton.setOnClickListener {
            if (led) {
                ledButton.setBackgroundResource(R.drawable.ic_baseline_highlight_low)
                mBluetoothThread?.write("B".toByteArray())
            } else {
                ledButton.setBackgroundResource(R.drawable.ic_baseline_highlight_high)
                mBluetoothThread?.write("A".toByteArray())
            }
            led = !led

        }

        airButton.setOnClickListener {
            if (air) {
                airButton.setBackgroundResource(R.drawable.ic_baseline_airoff)
                mBluetoothThread?.write("D".toByteArray())
            } else {
                airButton.setBackgroundResource(R.drawable.ic_baseline_airon)
                mBluetoothThread?.write("C".toByteArray())
            }
            air = !air
        }

        lockButton.setOnClickListener {
            if (locked) {
                lockButton.setBackgroundResource(R.drawable.ic_baseline_lock_open)
                mBluetoothThread?.write("F".toByteArray())
            } else {
                lockButton.setBackgroundResource(R.drawable.ic_baseline_lock)
                mBluetoothThread?.write("E".toByteArray())
            }
            locked = !locked
        }

        mBluetoothThread?.start()

    }


    private val mHandler: Handler = @SuppressLint("HandlerLeak") object : Handler() {

        override fun handleMessage(message: Message) {

            when (message.what) {
                MESSAGE_READ -> {
                    val readBuf = message.obj as ByteArray
                    val readMessage = String(readBuf, Charset.defaultCharset())

                    when {
                        readMessage.startsWith("t:") -> {
                            val temperatureAndHumidity = readMessage.split(":")
                            val temperature = temperatureAndHumidity[1].toFloatOrNull() ?: return
                            val humidity = temperatureAndHumidity[2].toFloatOrNull() ?: return

                            this@RoomActivity.temperature.progress = temperature
                            this@RoomActivity.humidity.progress = humidity

                            humidityInfo.text = "${humidity.roundToInt()}%"
                            temperatureInfo.text = "$temperature°C"
                        }
                        readMessage.startsWith("CH") -> {
                            println(1)
                            rainButton.setBackgroundResource(R.drawable.ic_baseline_cloud_raining)
                        }
                        readMessage.startsWith("CL") -> rainButton.setBackgroundResource(R.drawable.ic_baseline_cloud_queue_norain)
                        readMessage.startsWith("GH") -> fireAlertButton.setBackgroundResource(R.drawable.ic_baseline_warning_onfire)
                        readMessage.startsWith("GL") -> fireAlertButton.setBackgroundResource(R.drawable.ic_baseline_warning_fire)
                    }

                }
                MESSAGE_TOAST -> {
                    Toast.makeText(
                        applicationContext, message.data.getString("toast"), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        mBluetoothThread?.cancel()
    }


}