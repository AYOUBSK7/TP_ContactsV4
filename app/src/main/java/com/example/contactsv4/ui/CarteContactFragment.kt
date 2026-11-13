package com.example.contactsv4.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.contactsv4.ContactViewModel
import com.example.contactsv4.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

// Affiche la position du contact sur Google Maps avec un marqueur
class CarteContactFragment : Fragment(R.layout.fragment_carte) {

    private val viewModel: ContactViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val id = requireArguments().getLong(ARG_ID, 0L)

        viewLifecycleOwner.lifecycleScope.launch {
            val contact = viewModel.getContact(id)
            if (contact == null) {
                afficherMessage(tvMessage, getString(R.string.msg_contact_introuvable))
                return@launch
            }

            val latitude = contact.latitude
            val longitude = contact.longitude
            if (latitude == null || longitude == null) {
                // Pas de position : message explicite à la place de la carte
                afficherMessage(tvMessage, getString(R.string.msg_pas_de_position, contact.prenom, contact.nom))
                return@launch
            }

            val titre = getString(R.string.nom_complet, contact.prenom, contact.nom)
            val carte = childFragmentManager.findFragmentById(R.id.carte) as? SupportMapFragment
            carte?.getMapAsync { googleMap ->
                val position = LatLng(latitude, longitude)
                val marqueur = googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(titre)
                        .snippet(contact.telephone)
                )
                marqueur?.showInfoWindow()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.titre_carte)
    }

    private fun afficherMessage(tvMessage: TextView, texte: String) {
        tvMessage.text = texte
        tvMessage.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_ID = "contact_id"

        // L'identifiant du contact est passé par Bundle
        fun nouvelleInstance(id: Long): CarteContactFragment {
            val fragment = CarteContactFragment()
            fragment.arguments = bundleOf(ARG_ID to id)
            return fragment
        }
    }
}
