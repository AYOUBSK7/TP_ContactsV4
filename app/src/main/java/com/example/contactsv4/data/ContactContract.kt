package com.example.contactsv4.data

// Noms de la table et des colonnes : on ne les écrit nulle part ailleurs en dur.
object ContactContract {
    const val TABLE = "contacts"

    const val COL_ID = "_id"
    const val COL_NOM = "nom"
    const val COL_PRENOM = "prenom"
    const val COL_TELEPHONE = "telephone"
    const val COL_EMAIL = "email"
    const val COL_PHOTO = "photo_uri"
    const val COL_LATITUDE = "latitude"
    const val COL_LONGITUDE = "longitude"
    const val COL_FAVORI = "favori" // bonus (version 2)
}
