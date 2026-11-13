package com.example.contactsv4.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.contactsv4.ContactViewModel
import com.example.contactsv4.MainActivity
import com.example.contactsv4.PreferencesManager
import com.example.contactsv4.R
import com.example.contactsv4.afficherPhoto
import com.example.contactsv4.copierPhoto
import com.example.contactsv4.data.Contact
import com.example.contactsv4.dossierPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Détail d'un contact : création (id = 0), consultation, modification, suppression
class DetailContactFragment : Fragment(R.layout.fragment_detail) {

    private val viewModel: ContactViewModel by activityViewModels()

    private var contactId = 0L              // _id du contact (0 = nouveau contact)
    private var photo: String? = null       // photo affichée (pas encore enregistrée)
    private var photoOrigine: String? = null // photo enregistrée en base à l'ouverture
    private var fichierCamera: File? = null // fichier où l'appareil photo écrit
    private var champsCharges = false       // les champs ont déjà été remplis depuis la base

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var cbFavori: CheckBox
    private lateinit var btnEnregistrer: Button

    // ---------- Galerie, caméra, permission ----------

    private val choisirPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                message(R.string.msg_photo_annulee) // l'utilisateur a annulé
            } else {
                changerPhoto(uri)
            }
        }

    private val prendrePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { reussi ->
            val fichier = fichierCamera
            fichierCamera = null
            if (reussi && fichier != null && fichier.length() > 0) {
                changerPhoto(Uri.fromFile(fichier), fichier)
            } else {
                fichier?.delete()
                message(R.string.msg_photo_annulee)
            }
        }

    private val demanderCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordee ->
            if (accordee) ouvrirCamera() else message(R.string.msg_camera_refusee)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactId = requireArguments().getLong(ARG_ID, 0L)

        ivPhoto = view.findViewById(R.id.ivPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)
        cbFavori = view.findViewById(R.id.cbFavori)
        btnEnregistrer = view.findViewById(R.id.btnEnregistrer)
        val btnSupprimer = view.findViewById<Button>(R.id.btnSupprimer)
        val btnCarte = view.findViewById<Button>(R.id.btnCarte)

        // Après une rotation : on récupère l'état (les EditText se restaurent seuls)
        if (savedInstanceState != null) {
            photo = savedInstanceState.getString(ETAT_PHOTO)
            photoOrigine = savedInstanceState.getString(ETAT_PHOTO_ORIGINE)
            champsCharges = savedInstanceState.getBoolean(ETAT_CHARGES)
            savedInstanceState.getString(ETAT_CAMERA)?.let { fichierCamera = File(it) }
        }

        // Premier affichage : on lit le contact dans la base (sauf en création)
        if (!champsCharges) {
            if (contactId != 0L) chargerContact() else champsCharges = true
        }
        afficherPhoto(ivPhoto, photo)

        // En création, pas de suppression ni de carte
        val modeCreation = contactId == 0L
        btnSupprimer.visibility = if (modeCreation) View.GONE else View.VISIBLE
        btnCarte.visibility = if (modeCreation) View.GONE else View.VISIBLE

        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnGalerie).setOnClickListener { choisirPhoto.launch("image/*") }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener { clicCamera() }
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener { annuler() }
        btnCarte.setOnClickListener { ouvrirCarte() }
        btnSupprimer.setOnClickListener { confirmerSuppression() }
        btnEnregistrer.setOnClickListener { enregistrer() }

        // Bouton Retour du téléphone (et flèche de la barre) = Annuler
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { annuler() }

        appliquerPreferences()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(if (contactId == 0L) R.string.titre_nouveau else R.string.titre_detail)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ETAT_PHOTO, photo)
        outState.putString(ETAT_PHOTO_ORIGINE, photoOrigine)
        outState.putBoolean(ETAT_CHARGES, champsCharges)
        outState.putString(ETAT_CAMERA, fichierCamera?.absolutePath)
    }

    // ---------- Lecture du contact ----------

    private fun chargerContact() {
        viewLifecycleOwner.lifecycleScope.launch {
            val contact = viewModel.getContact(contactId) // lu sur Dispatchers.IO
            if (contact == null) {
                message(R.string.msg_contact_introuvable)
                retourListe()
                return@launch
            }
            etNom.setText(contact.nom)
            etPrenom.setText(contact.prenom)
            etTelephone.setText(contact.telephone)
            etEmail.setText(contact.email ?: "")
            etLatitude.setText(contact.latitude?.toString() ?: "")
            etLongitude.setText(contact.longitude?.toString() ?: "")
            cbFavori.isChecked = contact.favori
            photo = contact.photoUri
            photoOrigine = contact.photoUri
            afficherPhoto(ivPhoto, photo)
            champsCharges = true
        }
    }

    // Préférences : taille (nom, prénom, téléphone) et couleur (nom, prénom)
    private fun appliquerPreferences() {
        val preferences = PreferencesManager(requireContext())
        val taille = preferences.tailleTexte()
        val couleur = preferences.couleurTexte()
        for (champ in listOf(etNom, etPrenom, etTelephone)) {
            champ.setTextSize(TypedValue.COMPLEX_UNIT_SP, taille)
        }
        etNom.setTextColor(couleur)
        etPrenom.setTextColor(couleur)
    }

    // ---------- Enregistrer (insert ou update) ----------

    private fun enregistrer() {
        val contact = lireFormulaire() ?: return // champs invalides : rien n'est écrit en base

        btnEnregistrer.isEnabled = false // évite un double clic
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = viewModel.enregistrer(contact)
            if (ok) {
                // L'ancienne photo a été remplacée : on supprime son fichier
                val ancienne = photoOrigine
                if (ancienne != null && ancienne != photo) File(ancienne).delete()
                photoOrigine = photo
                message(R.string.msg_contact_enregistre)
                retourListe()
            } else {
                message(R.string.msg_echec_enregistrement)
                btnEnregistrer.isEnabled = true
            }
        }
    }

    // Vérifie les champs. Renvoie le contact, ou null si un champ est invalide.
    private fun lireFormulaire(): Contact? {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        // On accepte la virgule comme séparateur décimal
        val latTexte = etLatitude.text.toString().trim().replace(',', '.')
        val lonTexte = etLongitude.text.toString().trim().replace(',', '.')

        var valide = true

        // Champs obligatoires (NOT NULL en base)
        if (nom.isEmpty()) {
            etNom.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (prenom.isEmpty()) {
            etPrenom.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (telephone.isEmpty()) {
            etTelephone.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.msg_email_invalide)
            valide = false
        }

        // GPS : les deux vides (NULL) ou les deux valides
        var latitude: Double? = null
        var longitude: Double? = null
        if (latTexte.isNotEmpty() || lonTexte.isNotEmpty()) {
            if (latTexte.isEmpty()) {
                etLatitude.error = getString(R.string.msg_gps_incomplet)
                valide = false
            } else if (lonTexte.isEmpty()) {
                etLongitude.error = getString(R.string.msg_gps_incomplet)
                valide = false
            } else {
                latitude = latTexte.toDoubleOrNull()
                longitude = lonTexte.toDoubleOrNull()
                if (latitude == null) {
                    etLatitude.error = getString(R.string.msg_nombre_invalide)
                    valide = false
                } else if (latitude !in -90.0..90.0) {
                    etLatitude.error = getString(R.string.msg_latitude_hors_limites)
                    valide = false
                }
                if (longitude == null) {
                    etLongitude.error = getString(R.string.msg_nombre_invalide)
                    valide = false
                } else if (longitude !in -180.0..180.0) {
                    etLongitude.error = getString(R.string.msg_longitude_hors_limites)
                    valide = false
                }
            }
        }

        if (!valide) return null

        return Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email.ifEmpty { null },
            photoUri = photo,
            latitude = latitude,
            longitude = longitude,
            favori = cbFavori.isChecked
        )
    }

    // ---------- Supprimer (avec confirmation) ----------

    private fun confirmerSuppression() {
        val texte = getString(
            R.string.msg_confirmer_suppression,
            etPrenom.text.toString().trim(),
            etNom.text.toString().trim()
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titre_supprimer)
            .setMessage(texte)
            .setPositiveButton(R.string.oui) { _, _ -> supprimer() }
            .setNegativeButton(R.string.non, null)
            .show()
    }

    private fun supprimer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = viewModel.supprimer(contactId)
            if (ok) {
                // On supprime aussi les fichiers photo du contact
                photoOrigine?.let { File(it).delete() }
                photo?.let { File(it).delete() }
                message(R.string.msg_contact_supprime)
                retourListe()
            } else {
                message(R.string.msg_echec_suppression)
            }
        }
    }

    // ---------- Annuler / retour ----------

    private fun annuler() {
        // Nouvelle photo prise mais pas enregistrée : on supprime sa copie
        val nouvelle = photo
        if (nouvelle != null && nouvelle != photoOrigine) File(nouvelle).delete()
        photo = photoOrigine
        retourListe()
    }

    private fun retourListe() {
        if (isAdded && !parentFragmentManager.isStateSaved) {
            parentFragmentManager.popBackStack()
        }
    }

    private fun ouvrirCarte() {
        (requireActivity() as MainActivity).ouvrir(CarteContactFragment.nouvelleInstance(contactId))
    }

    // ---------- Intents implicites : appel et e-mail ----------

    private fun appeler() {
        val telephone = etTelephone.text.toString().trim()
        if (telephone.isEmpty()) {
            etTelephone.error = getString(R.string.msg_champ_obligatoire)
            return
        }
        lancer(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telephone, null)))
    }

    private fun envoyerEmail() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.msg_champ_obligatoire)
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        lancer(intent)
    }

    private fun lancer(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            message(R.string.msg_aucune_application)
        }
    }

    // ---------- Photo ----------

    private fun clicCamera() {
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            message(R.string.msg_camera_indisponible)
            return
        }
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            ouvrirCamera()
        } else {
            demanderCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun ouvrirCamera() {
        val contexte = requireContext()
        val fichier = File(dossierPhotos(contexte), "camera_${System.currentTimeMillis()}.jpg")
        fichierCamera = fichier
        val uri = FileProvider.getUriForFile(contexte, "${contexte.packageName}.fileprovider", fichier)
        try {
            prendrePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            fichierCamera = null
            message(R.string.msg_aucune_application)
        }
    }

    // Copie l'image dans filesDir (hors du thread principal) puis l'affiche
    private fun changerPhoto(uri: Uri, fichierTemporaire: File? = null) {
        val contexte = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val chemin = withContext(Dispatchers.IO) { copierPhoto(contexte, uri) }
            fichierTemporaire?.delete()
            if (chemin == null) {
                message(R.string.msg_photo_erreur)
                return@launch
            }
            // Si une autre photo non enregistrée était affichée, on la supprime
            val precedente = photo
            if (precedente != null && precedente != photoOrigine) File(precedente).delete()
            photo = chemin
            afficherPhoto(ivPhoto, photo)
        }
    }

    private fun message(texte: Int) {
        context?.let { Toast.makeText(it, texte, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val ARG_ID = "contact_id"
        private const val ETAT_PHOTO = "photo"
        private const val ETAT_PHOTO_ORIGINE = "photo_origine"
        private const val ETAT_CHARGES = "champs_charges"
        private const val ETAT_CAMERA = "fichier_camera"

        // L'identifiant du contact est passé par Bundle (0 = création)
        fun nouvelleInstance(id: Long): DetailContactFragment {
            val fragment = DetailContactFragment()
            fragment.arguments = bundleOf(ARG_ID to id)
            return fragment
        }
    }
}
