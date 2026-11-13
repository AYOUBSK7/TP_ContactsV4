package com.example.contactsv4.data

// Modèle d'un contact.
// id vaut 0 tant que le contact n'est pas enregistré, puis prend le _id donné par la base.
data class Contact(
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val photoUri: String? = null,   // chemin d'une copie de la photo dans filesDir
    val latitude: Double? = null,
    val longitude: Double? = null,
    val favori: Boolean = false     // bonus : colonne ajoutée en version 2
)
