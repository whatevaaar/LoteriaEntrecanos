package com.entrecanos.loteria

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.entrecanos.loteria.control.FirebaseLoginService
import com.entrecanos.loteria.vista.ToastCreator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_registrar.*

const val RC_SIGN_IN = 200

class RegistrarActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val toastCreator = ToastCreator(this)
    private val loginService = FirebaseLoginService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)
        supportActionBar?.title = "Registro"
        inicializarGoogleSignIn()

        sign_in_button.setOnClickListener {
            if (checkBoxTerminos.isChecked)
                signIn()
            else toastCreator.toastErrorTerminos()
        }

        boton_registrar.setOnClickListener {
            if (checkBoxTerminos.isChecked)
                registrarConCorreo()
            else toastCreator.toastErrorTerminos()
        }
    }

    private fun inicializarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun registrarConCorreo() {
        val nombre = texto_nombre.editText!!.text.toString()
        val correo = texto_correo.editText!!.text.toString()
        val pass = texto_password.editText!!.text.toString()
        if (nombre.isEmpty() or correo.isEmpty() or pass.isEmpty()) {
            toastCreator.llenarCampos()
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toastCreator.exitoAutenticacion()
                    registrarEnFirebase(nombre)
                    redirigirAMenu()
                }
            }
    }

    private fun registrarEnFirebase(nombre: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nombre).build()
        val usuario = firebaseAuth.currentUser
        usuario!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    loginService.registrarUsuarioSiNoExiste(usuario)
            }
    }

    override fun onStart() {
        super.onStart()
        if (loginService.usuarioLogeado())
            redirigirAMenu()
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
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}

