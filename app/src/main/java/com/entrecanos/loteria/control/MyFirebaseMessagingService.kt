package com.entrecanos.loteria.control

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.entrecanos.loteria.MenuActivity
import com.entrecanos.loteria.modelo.Usuario
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.entrecanos.loteria.R

class MyFirebaseMessagingService: FirebaseMessagingService()  {
    private lateinit var usuario: Usuario
    private val tag = "FirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }

    override fun onNewToken(token: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios/${firebaseUser.uid}")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    usuario = dataSnapshot.getValue(Usuario::class.java)!!
                    usuario.token= token
                    actualizarToken()
                }
            }
        }
        ref.addListenerForSingleValueEvent(postListener)
    }

    private fun actualizarToken() {
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios")
        ref.child(usuario.uid).setValue(usuario)
    }

    fun conseguirToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token ?: return@OnCompleteListener
                // Log and toast
                onNewToken(token)
            })
    }
    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this,  "M_CH_ID")
            .setSmallIcon(R.drawable.logo_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
