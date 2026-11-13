package com.example.contactsv4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.example.contactsv4.ui.ListeContactsFragment
import com.example.contactsv4.ui.ParametresFragment

// Activité unique : elle héberge les fragments et la barre d'application
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Valeurs par défaut des préférences (écrites seulement au premier lancement)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        // Seulement au premier affichage : après une rotation, Android recrée déjà
        // les fragments, on ne doit pas en ajouter un deuxième.
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.conteneur, ListeContactsFragment())
            }
        }

        // Flèche retour dans la barre quand on n'est pas sur la liste
        supportFragmentManager.addOnBackStackChangedListener { majFlecheRetour() }
        majFlecheRetour()
    }

    // Navigation entre fragments : replace + addToBackStack
    fun ouvrir(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.conteneur, fragment)
            addToBackStack(null)
        }
    }

    private fun majFlecheRetour() {
        val afficher = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(afficher)
    }

    // Flèche de la barre = bouton Retour du téléphone
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Menu de la barre : icône Paramètres
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_parametres) {
            // On n'empile pas deux fois l'écran Paramètres
            if (supportFragmentManager.findFragmentById(R.id.conteneur) !is ParametresFragment) {
                ouvrir(ParametresFragment())
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
