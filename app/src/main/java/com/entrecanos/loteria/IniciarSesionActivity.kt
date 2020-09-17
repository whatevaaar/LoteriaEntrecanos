package com.entrecanos.loteria

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.entrecanos.loteria.control.FirebaseLoginService
import com.entrecanos.loteria.control.MyFirebaseMessagingService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_iniciar_sesion.*

class IniciarSesionActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val loginService = FirebaseLoginService(this)
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inicializarGoogleSignIn()
        supportActionBar?.title = "Iniciar Sesión"
        setContentView(R.layout.activity_iniciar_sesion)
        sign_in_button.setOnClickListener {
            signIn()
        }
        boton_iniciar_sesion.setOnClickListener {
            signInCorreo()
        }
    }

    private fun inicializarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInCorreo() {
        val correo = texto_correo.editText?.text.toString()
        val pass = texto_password.editText?.text.toString()
        if (correo.isEmpty() or pass.isEmpty()) {
            Toast.makeText(this, "Error al autenticarse", Toast.LENGTH_LONG).show()
            return
        }
        firebaseAuth.signInWithEmailAndPassword(correo, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Authentication Completed.", Toast.LENGTH_LONG).show()
                redirigirAMenu()
            } else {
                Toast.makeText(this, "Credenciales Incorrectas", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signIn() {
        if (loginService.usuarioLogeado()) return
        else startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            loginService.handleSignInResult(task!!)
        }
    }

    private fun redirigirAMenu() {
        MyFirebaseMessagingService().conseguirToken()
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}