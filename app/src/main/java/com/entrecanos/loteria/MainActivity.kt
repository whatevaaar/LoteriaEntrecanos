package com.entrecanos.loteria

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

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
}
