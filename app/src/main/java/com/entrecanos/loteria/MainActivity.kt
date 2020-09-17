package com.entrecanos.loteria

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.stripe.stripeterminal.TerminalLifecycleObserver
import kotlinx.android.synthetic.main.activity_main.*

const val REQUEST_CODE_LOCATION = 123
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        establecerPantallaCompleta()
        //Listeners
        boton_iniciar_sesion.setOnClickListener { redirigirIniciarSesion() }
        boton_registrarse.setOnClickListener { redirigirRegistrarse() }
    }

    override fun onStart() {
        super.onStart()
        validarPermisos()
        if (usuarioLogeado())
            redirigirAMenu()
    }

    private fun redirigirAMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    fun usuarioLogeado(): Boolean = FirebaseAuth.getInstance().currentUser != null
    fun redirigirIniciarSesion() = startActivity(Intent(this, IniciarSesionActivity::class.java))
    fun redirigirRegistrarse() = startActivity(Intent(this, RegistrarActivity::class.java))

    fun establecerPantallaCompleta() {
        window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        supportActionBar?.hide()
    }
    //Permisos de Localización. Necesarios para el SDK de Stripe.
    private fun validarPermisos() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.isNotEmpty()
            && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            throw RuntimeException("Location services are required in order to " + "connect to a reader.")
        }
    }
}
