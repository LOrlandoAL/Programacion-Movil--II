package net.ivanvega.mibroadcastreceivertelefono

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.provider.CallLog
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                Log.d("MyBroadcastReceiver", "Estado del teléfono: RINGING")

                if (incomingNumber.isNullOrEmpty()) {
                    incomingNumber = getLastIncomingCall(context)
                }

                if (!incomingNumber.isNullOrEmpty()) {
                    Log.d("MyBroadcastReceiver", "Número detectado: $incomingNumber")

                    val prefs: SharedPreferences = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                    val numeroGuardado = prefs.getString("numero", "")
                    val mensajeGuardado = prefs.getString("mensaje", "")

                    Log.d("MyBroadcastReceiver", "Número guardado: $numeroGuardado")
                    Log.d("MyBroadcastReceiver", "Mensaje guardado: $mensajeGuardado")

                    if (incomingNumber == numeroGuardado) {
                        Log.d("MyBroadcastReceiver", "El número coincide, enviando SMS...")
                        enviarSMS(incomingNumber, mensajeGuardado ?: "", context)
                    } else {
                        Log.d("MyBroadcastReceiver", "El número no coincide, no se enviará SMS")
                    }
                } else {
                    Log.d("MyBroadcastReceiver", "No se pudo obtener el número de teléfono")
                }
            }
        }
    }

    private fun getLastIncomingCall(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Log.d("MyBroadcastReceiver", "Permiso de lectura del historial de llamadas concedido")

            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.INCOMING_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val lastNumber = it.getString(0)
                    Log.d("MyBroadcastReceiver", "Último número detectado en el historial: $lastNumber")
                    return lastNumber
                }
            }
        } else {
            Log.e("MyBroadcastReceiver", "No se tiene permiso para leer el historial de llamadas")
        }
        return null
    }

    private fun enviarSMS(numero: String, mensaje: String, context: Context) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Log.d("MyBroadcastReceiver", "SMS enviado a $numero: $mensaje")
        } catch (e: Exception) {
            Log.e("MyBroadcastReceiver", "Error al enviar SMS: ${e.message}")
        }
    }
}
