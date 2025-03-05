package net.ivanvega.mibroadcastreceivertelefono

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btnSwitch: Switch
    private lateinit var edtNumero: EditText
    private lateinit var edtMensaje: EditText
    private lateinit var btnGuardar: Button
    private val REQUEST_CODE_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSwitch = findViewById(R.id.swPhoneState)
        edtNumero = findViewById(R.id.edtNumero)
        edtMensaje = findViewById(R.id.edtMensaje)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Verificar y solicitar permisos si es necesario
        checkAndRequestPermissions()

        // Recuperar valores guardados previamente
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        edtNumero.setText(prefs.getString("numero", ""))
        edtMensaje.setText(prefs.getString("mensaje", ""))

        // Guardar valores ingresados en SharedPreferences
        btnGuardar.setOnClickListener {
            val numero = edtNumero.text.toString().trim()
            val mensaje = edtMensaje.text.toString().trim()

            if (numero.isNotEmpty() && mensaje.isNotEmpty()) {
                val editor = prefs.edit()
                editor.putString("numero", numero)
                editor.putString("mensaje", mensaje)
                editor.apply()
                Log.d("MainActivity", "Datos guardados -> Número: $numero, Mensaje: $mensaje")
            } else {
                Log.d("MainActivity", "Error: No se pueden guardar valores vacíos")
            }
        }

        // Configurar el estado del switch según el estado del servicio
        btnSwitch.isChecked = isMyServiceRunning(ServicePhoneState::class.java)

        // Configurar el switch para iniciar/detener el servicio
        btnSwitch.setOnClickListener { view: View? ->
            val stateSwitch = btnSwitch.isChecked
            val callService = Intent(this, ServicePhoneState::class.java)

            if (stateSwitch) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(callService) // Para Android 8+
                    } else {
                        startService(callService) // Para versiones anteriores
                    }
                    Log.d(packageName, "Servicio iniciado")
                } catch (ex: Exception) {
                    Log.e(packageName, "Error al iniciar servicio: ${ex.message}")
                }
            } else {
                stopService(callService)
                Log.d(packageName, "Servicio detenido")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_CODE_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permiso concedido")
            } else {
                Log.e("MainActivity", "Permiso denegado")
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any { it.service.className == serviceClass.name }
    }
}
