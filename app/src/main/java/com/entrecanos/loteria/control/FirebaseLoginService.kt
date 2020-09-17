package com.entrecanos.loteria.control

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.entrecanos.loteria.MenuActivity
import com.entrecanos.loteria.RC_SIGN_IN
import com.entrecanos.loteria.modelo.Usuario
import com.entrecanos.loteria.vista.ToastCreator
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseLoginService(private val contexto: Context) {
    private val toastCreator = ToastCreator(contexto)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun registrarUsuarioSiNoExiste(usuario: FirebaseUser) {
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios/${usuario.uid}")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCreator.errorIntentarDeNuevo()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    ref.setValue(Usuario(usuario.uid, usuario.displayName!!))
                    MyFirebaseMessagingService().conseguirToken()
                } else toastCreator.correoYaRegistrado()
            }
        }
        ref.addListenerForSingleValueEvent(postListener)
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            toastCreator.errorAutenticacion()
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(contexto as Activity) { task ->
                if (task.isSuccessful) {
                    toastCreator.exitoAutenticacion()
                    registrarUsuarioSiNoExiste(firebaseAuth.currentUser!!)
                    redirigirAMenu()
                } else toastCreator.errorAutenticacion()
            }
    }


    private fun redirigirAMenu() {
        val intent = Intent(contexto, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }

    fun usuarioLogeado(): Boolean = FirebaseAuth.getInstance().currentUser != null
}