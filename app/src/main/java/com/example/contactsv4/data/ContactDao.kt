package com.example.contactsv4.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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

// Seule classe qui fait des requêtes SQL.
// Toutes les valeurs variables passent par des arguments "?" (jamais de concaténation).
// Ces méthodes sont bloquantes : on les appelle toujours depuis Dispatchers.IO.
class ContactDao(context: Context) {

    private val helper = ContactDbHelper.getInstance(context)

    // Renvoie le _id du nouveau contact, ou -1 en cas d'erreur
    fun insert(contact: Contact): Long {
        return helper.writableDatabase.insert(TABLE, null, versValeurs(contact))
    }

    // Renvoie le nombre de lignes modifiées (1 si OK, 0 si le contact n'existe plus)
    fun update(contact: Contact): Int {
        return helper.writableDatabase.update(
            TABLE,
            versValeurs(contact),
            "$COL_ID = ?",
            arrayOf(contact.id.toString())
        )
    }

    // Renvoie le nombre de lignes supprimées
    fun delete(id: Long): Int {
        return helper.writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getAll(tri: String = TRI_NOM): List<Contact> {
        val cursor = helper.readableDatabase.query(
            TABLE, null, null, null, null, null, ordreSql(tri)
        )
        return lireContacts(cursor)
    }

    fun getById(id: Long): Contact? {
        val cursor = helper.readableDatabase.query(
            TABLE, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null
        )
        return lireContacts(cursor).firstOrNull()
    }

    // Filtre sur le nom, le prénom ou le téléphone (LIKE est insensible à la casse)
    fun search(texte: String, tri: String = TRI_NOM): List<Contact> {
        val motif = "%$texte%" // "%" ajoutés autour de la valeur, la valeur reste un argument "?"
        val cursor = helper.readableDatabase.query(
            TABLE,
            null,
            "$COL_NOM LIKE ? OR $COL_PRENOM LIKE ? OR $COL_TELEPHONE LIKE ?",
            arrayOf(motif, motif, motif),
            null,
            null,
            ordreSql(tri)
        )
        return lireContacts(cursor)
    }

    // ORDER BY choisi parmi des valeurs fixes (jamais la saisie de l'utilisateur)
    private fun ordreSql(tri: String): String {
        val nomPrenom = "$COL_NOM COLLATE NOCASE, $COL_PRENOM COLLATE NOCASE"
        return when (tri) {
            TRI_PRENOM -> "$COL_PRENOM COLLATE NOCASE, $COL_NOM COLLATE NOCASE"
            TRI_FAVORIS -> "$COL_FAVORI DESC, $nomPrenom"
            else -> nomPrenom
        }
    }

    // Contact -> ContentValues (sans _id : la base le génère)
    private fun versValeurs(contact: Contact): ContentValues {
        val valeurs = ContentValues()
        valeurs.put(COL_NOM, contact.nom)
        valeurs.put(COL_PRENOM, contact.prenom)
        valeurs.put(COL_TELEPHONE, contact.telephone)
        valeurs.put(COL_EMAIL, contact.email)
        valeurs.put(COL_PHOTO, contact.photoUri)
        valeurs.put(COL_LATITUDE, contact.latitude)   // null -> NULL en base
        valeurs.put(COL_LONGITUDE, contact.longitude)
        valeurs.put(COL_FAVORI, if (contact.favori) 1 else 0)
        return valeurs
    }

    // Cursor -> liste de contacts. use { } ferme le Cursor même en cas d'erreur.
    private fun lireContacts(cursor: Cursor): List<Contact> {
        val contacts = mutableListOf<Contact>()
        cursor.use { c ->
            val iId = c.getColumnIndexOrThrow(COL_ID)
            val iNom = c.getColumnIndexOrThrow(COL_NOM)
            val iPrenom = c.getColumnIndexOrThrow(COL_PRENOM)
            val iTelephone = c.getColumnIndexOrThrow(COL_TELEPHONE)
            val iEmail = c.getColumnIndexOrThrow(COL_EMAIL)
            val iPhoto = c.getColumnIndexOrThrow(COL_PHOTO)
            val iLatitude = c.getColumnIndexOrThrow(COL_LATITUDE)
            val iLongitude = c.getColumnIndexOrThrow(COL_LONGITUDE)
            val iFavori = c.getColumnIndexOrThrow(COL_FAVORI)

            while (c.moveToNext()) {
                contacts.add(
                    Contact(
                        id = c.getLong(iId),
                        nom = c.getString(iNom),
                        prenom = c.getString(iPrenom),
                        telephone = c.getString(iTelephone),
                        email = if (c.isNull(iEmail)) null else c.getString(iEmail),
                        photoUri = if (c.isNull(iPhoto)) null else c.getString(iPhoto),
                        latitude = if (c.isNull(iLatitude)) null else c.getDouble(iLatitude),
                        longitude = if (c.isNull(iLongitude)) null else c.getDouble(iLongitude),
                        favori = c.getInt(iFavori) == 1
                    )
                )
            }
        }
        return contacts
    }

    companion object {
        // Valeurs possibles de la préférence "ordre_tri" (voir arrays.xml)
        const val TRI_NOM = "nom"
        const val TRI_PRENOM = "prenom"
        const val TRI_FAVORIS = "favoris"
    }
}
