package me.soekd.hotelinteligente

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

/*

Todo este código está disponível na documentação do android:
https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data?hl=lt

 */
class BluetoothThread(
    private val handler: Handler,
    private val mmSocket: BluetoothSocket
) : Thread() {

    private val mmInStream: InputStream = mmSocket.inputStream
    private val mmOutStream: OutputStream = mmSocket.outputStream
    private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

    override fun run() {
        var numBytes: Int // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            // Read from the InputStream.
            numBytes = try {
                mmInStream.read(mmBuffer)
            } catch (e: IOException) {
                e.printStackTrace()
                break
            }

//          Send the obtained bytes to the UI activity.
            val readMsg = handler.obtainMessage(
                MESSAGE_READ, numBytes, -1,
                mmBuffer
            )
            readMsg.sendToTarget()
        }
    }

    // Call this from the main activity to send data to the remote device.
    fun write(bytes: ByteArray) {
        try {
            mmOutStream.write(bytes)
        } catch (e: IOException) {

            // Send a failure message back to the activity.
            val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
            val bundle = Bundle().apply {
                putString("toast", "Ocorreu um erro ao tentar enviar o comando.")
            }
            writeErrorMsg.data = bundle
            handler.sendMessage(writeErrorMsg)
            return
        }

        // Share the sent message with the UI activity.
        val writtenMsg = handler.obtainMessage(
            MESSAGE_WRITE, -1, -1, mmBuffer
        )
        writtenMsg.sendToTarget()
    }

    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
        }
    }

}