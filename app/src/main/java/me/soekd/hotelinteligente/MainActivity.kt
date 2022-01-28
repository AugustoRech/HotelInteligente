package me.soekd.hotelinteligente

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {

        // Request code aleatório para verificar se o bluetooth foi ativado.
        private const val REQUEST_ENABLE_BT = 103
//        private const val REQUEST_CONNECTION = 104

    }

    lateinit var buttonBluetoothConnect: Button

    var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // para esconder uma barra que fica em cima por padrão.
        supportActionBar?.hide()

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        buttonBluetoothConnect = findViewById(R.id.button_connect)

        buttonBluetoothConnect.setOnClickListener { view -> showDevices() }

        /*

        Função do when:

        Checar se o bluetooth existe;
        Requisitar a habilitação do bluetooth caso esteja desativado;


         */

        when {
            //
            mBluetoothAdapter == null ->
                Toast.makeText(
                    applicationContext,
                    getString(R.string.ErrorDeviceDoesNotSupportBluetooth),
                    Toast.LENGTH_LONG
                ).show()
            !mBluetoothAdapter!!.isEnabled -> {
                // se não estiver ativado vamos obriga-lo a ativar...
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)
            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Verificando os callbacks.
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.SuccessBluetoothEnable),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.ErrorNoBluetooth),
                        Toast.LENGTH_LONG
                    ).show()

                    // desliga tudo
                    finish()
                }
            }
        }
    }

    private fun showDevices() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.DevicesListTitle))

        val devices: Set<BluetoothDevice> = mBluetoothAdapter?.bondedDevices ?: setOf()

        // Caso não tenha nenhum dispositivo pareado, retornar e mandar msg de aviso
        if (devices.isEmpty()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.ErrorDevicesNotFound),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Pegar todos dispositivos e transforma-lo em uma lista
        val devicesToShow = devices.map { "${it.name}\n${it.address}" }.toTypedArray()

        // Callback para pegar o dispositivo desejado
        builder.setItems(devicesToShow) { dialog, slot ->
            try {
                val deviceSignature = devicesToShow[slot]

                // Este é o mac que precisamos, agora só preciamos mandar para a activity onde vai ficar tudo.
                val mac = deviceSignature.split("\n")[1]

                val intent = Intent(this, RoomActivity::class.java)

                // Enviando para o mac para a RoomActivity
                intent.putExtra("mac", mac)

                // Iniciando...
                startActivity(intent)

            } catch (exception: Exception) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.ConnectDialogError),
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        val dialog = builder.create()
        dialog.show()
    }

}