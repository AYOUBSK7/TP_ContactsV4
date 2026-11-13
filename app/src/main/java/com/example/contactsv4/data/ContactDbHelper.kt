package com.example.contactsv4.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.contactsv4.data.ContactContract.COL_EMAIL
import com.example.contactsv4.data.ContactContract.COL_FAVORI
import com.example.contactsv4.data.ContactContract.COL_ID
import com.example.contactsv4.data.ContactContract.COL_LATITUDE
import com.example.contactsv4.data.ContactContract.COL_LONGITUDE
import com.example.contactsv4.data.ContactContract.COL_NOM
import com.example.contactsv4.data.ContactContract.COL_PHOTO
import com.example.contactsv4.data.ContactContract.COL_PRENOM
import com.example.contactsv4.data.ContactContract.COL_TELEPHONE
import com.example.contactsv4.data.ContactContract.TABLE

// Crée et met à jour la base contacts.db.
// Constructeur privé : on passe toujours par getInstance() (une seule instance).
class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Appelée UNE seule fois : quand le fichier contacts.db n'existe pas encore
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_NOM TEXT NOT NULL, " +
                "$COL_PRENOM TEXT NOT NULL, " +
                "$COL_TELEPHONE TEXT NOT NULL, " +
                "$COL_EMAIL TEXT, " +
                "$COL_PHOTO TEXT, " +
                "$COL_LATITUDE REAL, " +
                "$COL_LONGITUDE REAL, " +
                "$COL_FAVORI INTEGER DEFAULT 0)"
        )
        insererContactsDemo(db)
    }

    // Appelée quand DATABASE_VERSION augmente (base déjà installée)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Version 1 -> 2 (bonus) : on ajoute la colonne favori sans perdre les contacts
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE ADD COLUMN $COL_FAVORI INTEGER DEFAULT 0")
        }
    }

    // 6 contacts de démonstration, dont 2 sans coordonnées GPS
    private fun insererContactsDemo(db: SQLiteDatabase) {
        val demo = listOf(
            Contact(nom = "Ben Salah", prenom = "Amine", telephone = "+216 98 123 456",
                email = "amine.bensalah@example.com", latitude = 33.8756, longitude = 10.8575),
            Contact(nom = "Trabelsi", prenom = "Sarra", telephone = "22 345 678",
                email = "sarra.trabelsi@example.com", latitude = 36.8065, longitude = 10.1815),
            Contact(nom = "Gharbi", prenom = "Mohamed", telephone = "55 111 222"),
            Contact(nom = "Jaziri", prenom = "Ines", telephone = "+216 50 765 432",
                email = "ines.jaziri@example.com", latitude = 34.7406, longitude = 10.7603),
            Contact(nom = "Mansouri", prenom = "Youssef", telephone = "29 876 543",
                email = "youssef.mansouri@example.com"),
            Contact(nom = "Hammami", prenom = "Nour", telephone = "97 222 333",
                email = "nour.hammami@example.com", latitude = 33.5039, longitude = 11.1122)
        )
        for (contact in demo) {
            val valeurs = ContentValues()
            valeurs.put(COL_NOM, contact.nom)
            valeurs.put(COL_PRENOM, contact.prenom)
            valeurs.put(COL_TELEPHONE, contact.telephone)
            valeurs.put(COL_EMAIL, contact.email)
            valeurs.put(COL_LATITUDE, contact.latitude)
            valeurs.put(COL_LONGITUDE, contact.longitude)
            db.insert(TABLE, null, valeurs)
        }
    }

    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 2 // 1 = schéma de l'énoncé, 2 = + colonne favori (bonus)

        @Volatile
        private var instance: ContactDbHelper? = null

        // Singleton construit avec applicationContext (pas de fuite d'Activity)
        fun getInstance(context: Context): ContactDbHelper {
            return instance ?: synchronized(this) {
                instance ?: ContactDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
