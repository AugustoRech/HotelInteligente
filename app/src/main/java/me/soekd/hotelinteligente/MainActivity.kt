package me.soekd.hotelinteligente

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    companion object {

        // Request code aleatório para verificar se o bluetooth foi ativado.
        const val REQUEST_ENABLE_BT = 103

    }

    var bluetoothConnected = false


    lateinit var buttonBluetoothConnect: Button

    var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // para esconder uma barra que fica em cima por padrão.
        supportActionBar?.hide()

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        buttonBluetoothConnect = findViewById(R.id.button_connect)

        buttonBluetoothConnect.setOnClickListener { view ->

            if (bluetoothConnected) {

            } else {

            }

        }

        /*

        Função do when:

        Checar se o bluetooth existe;
        Requisitar a habilitação do bluetooth caso esteja desativado;


         */

        when {
            //
            mBluetoothAdapter == null ->
                Toast.makeText(applicationContext, "Seu dispositivo não tem suporte a bluetooth!", Toast.LENGTH_LONG).show()
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
                        "O seu bluetooth foi ligado com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Este aplicativo funciona apenas com conexão bluetooth.",
                        Toast.LENGTH_LONG
                    ).show()

                    // desliga tudo
                    finish()
                }

            }

        }

    }

}