package com.example.contactsv4

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.example.contactsv4.data.ContactDao

// Lecture centralisée des préférences : renvoie des valeurs déjà converties.
// Les préférences sont des réglages simples clé/valeur -> SharedPreferences (pas SQLite).
class PreferencesManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    // Taille du texte en sp (14, 16 ou 20)
    fun tailleTexte(): Float {
        return prefs.getString(CLE_TAILLE, "16")?.toFloatOrNull() ?: 16f
    }

    // Couleur du nom et du prénom (Int utilisable par setTextColor)
    fun couleurTexte(): Int {
        val valeur = prefs.getString(CLE_COULEUR, "#000000")
        return try {
            Color.parseColor(valeur)
        } catch (e: IllegalArgumentException) {
            Color.BLACK
        }
    }

    // Afficher ou non le bouton Appeler sur chaque ligne
    fun appelDepuisListe(): Boolean {
        return prefs.getBoolean(CLE_APPEL, false)
    }

    // Bonus : ordre de tri ("nom", "prenom" ou "favoris")
    fun ordreTri(): String {
        return prefs.getString(CLE_TRI, ContactDao.TRI_NOM) ?: ContactDao.TRI_NOM
    }

    companion object {
        const val CLE_TAILLE = "taille_police"
        const val CLE_COULEUR = "couleur_preferee"
        const val CLE_APPEL = "appel_depuis_liste"
        const val CLE_TRI = "ordre_tri"
    }
}
