package com.entrecanos.loteria

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.entrecanos.loteria.modelo.Carta
import com.entrecanos.loteria.modelo.Jugador
import com.entrecanos.loteria.vista.ToastCreator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    private val toastCreator = ToastCreator(this)
    private lateinit var mazo: String
    private lateinit var jugador: Jugador
    private lateinit var cartaActiva: Carta
    private lateinit var idJuego: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        crearNuevoJugador()
        esconderNavegacion()
        buscarJuego()
    }

    private fun crearNuevoJugador() {
        val usuarioActual = FirebaseAuth.getInstance().currentUser ?: return
        jugador = Jugador(usuarioActual.uid, usuarioActual.displayName!!)
    }

    private fun buscarJuego() {
        val ref = FirebaseDatabase.getInstance().reference.child("juegos").orderByChild("activo").equalTo(true)
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCreator.errorIntentarDeNuevo()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                for (ds in dataSnapshot.children) {
                    idJuego = ds.key!!
                    mazo = ds.child("mazo").getValue(String::class.java)!!
                    iniciarListenerCartaActiva()
                    iniciarListenerDeFinDeJuego()
                    if (ds.hasChild("jugadores/${jugador.uid}")) instanciarJugadorExistente()
                    else conseguirCartas()
                }
                ref.removeEventListener(this)
            }
        }
        ref.addValueEventListener(postListener)
    }

    private fun iniciarListenerDeFinDeJuego() {
        val ref = FirebaseDatabase.getInstance().getReference("juegos/${idJuego}/activo")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
            }
        }
        ref.addValueEventListener(postListener)
    }

    private fun instanciarJugadorExistente() {
        val ref = FirebaseDatabase.getInstance().getReference("juegos/${idJuego}/jugadores/${jugador.uid}")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCreator.errorIntentarDeNuevo()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                jugador.puntos = dataSnapshot.child("puntos").getValue(Int::class.java)!!
                //Cargamos la planilla
                for (i in 0..15) {
                    val cartaTemp = dataSnapshot.child("planilla/${i}").getValue(Carta::class.java)!!
                    jugador.planilla.add(i, cartaTemp)
                }
                instanciarCartas()
            }
        }
        ref.addListenerForSingleValueEvent(postListener)
    }

    private fun iniciarListenerCartaActiva() {
        val ref = FirebaseDatabase.getInstance().getReference("juegos/${idJuego}/carta-activa")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCreator.errorIntentarDeNuevo()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                cartaActiva = dataSnapshot.getValue(Carta::class.java)!!
                Picasso.get().load(cartaActiva.url).into(cartaactiva)
            }
        }
        ref.addValueEventListener(postListener)
    }


    private fun conseguirCartas() {
        val ref = FirebaseDatabase.getInstance().getReference("mazos/${mazo}")
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastCreator.errorIntentarDeNuevo()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                val listaCartas = mutableListOf<Carta>()
                for (ds in dataSnapshot.children) {
                    val carta = ds.getValue(Carta::class.java)!!
                    listaCartas.add(carta)
                }
                jugador.planilla.addAll(
                    listaCartas.shuffled().take(16)
                ) //Revolvemos y repartimos las cartas de la planilla
                registrarJugadorEnJuego()
                instanciarCartas()
            }
        }
        ref.addListenerForSingleValueEvent(postListener)
    }

    private fun registrarJugadorEnJuego() =
        FirebaseDatabase.getInstance().getReference("juegos/${idJuego}/jugadores/${jugador.uid}").setValue(jugador)

    private fun instanciarCartas() {
        for ((index: Int, carta: Carta) in jugador.planilla.withIndex()) {
            val img = encontrarPortada(index)
            Picasso.get().load(carta.url).into(img)
            img!!.setOnClickListener {
                if (cartaCorrectaYNoJugadaAntes(index)) {
                    jugador.puntos += 1
                    actualizarPuntuajeDeJugador()
                    cartaActiva.jugada = true
                }
            }
        }
    }

    private fun cartaCorrectaYNoJugadaAntes(index: Int): Boolean =
        cartaActiva.nombre == jugador.planilla[index].nombre && !cartaActiva.jugada


    private fun actualizarPuntuajeDeJugador() =
        FirebaseDatabase.getInstance().getReference("juegos/${idJuego}/jugadores/${jugador.uid}/puntos")
            .setValue(jugador.puntos)

    private fun encontrarPortada(numero: Int): ImageView? {
        val id: Int = resources.getIdentifier("carta${numero + 1}", "id", packageName)
        return findViewById(id)
    }

    private fun esconderNavegacion() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    //Código de Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_perfil -> {
                startActivity(Intent(this, PerfilActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}