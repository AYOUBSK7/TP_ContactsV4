package com.example.contactsv4.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.contactsv4.R

// Écran Paramètres : décrit dans res/xml/preferences.xml.
// AndroidX enregistre tout seul les choix dans les SharedPreferences.
class ParametresFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.titre_parametres)
    }
}
