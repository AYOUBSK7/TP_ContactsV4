package com.example.contactsv4.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv4.ContactViewModel
import com.example.contactsv4.MainActivity
import com.example.contactsv4.PreferencesManager
import com.example.contactsv4.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Écran liste : filtre, RecyclerView, bouton "+", bouton Appeler par ligne
class ListeContactsFragment : Fragment(R.layout.fragment_liste) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var preferences: PreferencesManager
    private lateinit var adapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = PreferencesManager(requireContext())

        val etFiltre = view.findViewById<EditText>(R.id.etFiltre)
        val tvVide = view.findViewById<TextView>(R.id.tvVide)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        val fabAjouter = view.findViewById<FloatingActionButton>(R.id.fabAjouter)

        adapter = ContactAdapter(
            onClic = { contact -> ouvrirDetail(contact.id) },
            onAppel = { contact -> appeler(contact.telephone) }
        )
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        // La liste vient du ViewModel (lui-même chargé depuis SQLite)
        viewModel.contacts.observe(viewLifecycleOwner) { liste ->
            adapter.mettreAJour(liste)
            tvVide.visibility = if (liste.isEmpty()) View.VISIBLE else View.GONE
        }

        // Filtrage au fil de la saisie (requête SQL LIKE dans le DAO)
        etFiltre.doAfterTextChanged { texte ->
            viewModel.filtrer(texte?.toString() ?: "")
        }

        // "+" : DetailContactFragment en mode création (id = 0)
        fabAjouter.setOnClickListener { ouvrirDetail(0L) }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.titre_liste)

        // Préférences relues à chaque retour sur la liste -> appliquées tout de suite
        adapter.appliquerPreferences(
            preferences.tailleTexte(),
            preferences.couleurTexte(),
            preferences.appelDepuisListe()
        )
        // Read : la liste est relue depuis la base à chaque affichage
        viewModel.chargerContacts()
    }

    // On passe l'identifiant (_id) du contact, jamais sa position
    private fun ouvrirDetail(id: Long) {
        (requireActivity() as MainActivity).ouvrir(DetailContactFragment.nouvelleInstance(id))
    }

    // ACTION_DIAL : ouvre le composeur sans appeler (aucune permission nécessaire)
    private fun appeler(telephone: String) {
        if (telephone.isBlank()) {
            Toast.makeText(requireContext(), R.string.msg_telephone_vide, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telephone, null))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.msg_aucune_application, Toast.LENGTH_SHORT).show()
        }
    }
}
