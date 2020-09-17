package com.entrecanos.loteria.vista

import android.content.Context
import android.widget.Toast

class ToastCreator(contexto: Context) {
    private val contexto = contexto
    fun toastErrorTerminos() = Toast.makeText(contexto, "Tienes que leer los términos y condiciones.", Toast.LENGTH_LONG).show()
    fun errorAutenticacion() = Toast.makeText(contexto, "Error al autenticarse. Intenta otra vez", Toast.LENGTH_LONG).show()
    fun exitoAutenticacion() = Toast.makeText(contexto, "Login exitoso!", Toast.LENGTH_LONG).show()
    fun correoYaRegistrado() = Toast.makeText(contexto, "Ese correo ya se encuentra registrado", Toast.LENGTH_LONG).show()
    fun errorIntentarDeNuevo() = Toast.makeText(contexto, "Error, Intentar de nuevo", Toast.LENGTH_LONG).show()
    fun llenarCampos() = Toast.makeText(contexto, "Por favor, llena todos los campos", Toast.LENGTH_LONG).show()
    fun errorTimeOut()= Toast.makeText(contexto, "Error al conectarse al servidor. Revise conexión", Toast.LENGTH_SHORT).show()
}