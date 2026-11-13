package com.example.contactsv4.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv4.R
import com.example.contactsv4.afficherPhoto
import com.example.contactsv4.data.Contact

// Adaptateur de la RecyclerView.
// Il reçoit les préférences déjà converties (taille en sp, couleur en Int, booléen).
class ContactAdapter(
    private val onClic: (Contact) -> Unit,   // clic sur la ligne -> détail
    private val onAppel: (Contact) -> Unit   // clic sur le bouton Appeler
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts: List<Contact> = emptyList()
    private var tailleSp = 16f
    private var couleur = Color.BLACK
    private var appelVisible = false

    init {
        setHasStableIds(true) // chaque ligne est identifiée par le _id du contact
    }

    class ContactViewHolder(vue: View) : RecyclerView.ViewHolder(vue) {
        val ivPhoto: ImageView = vue.findViewById(R.id.ivPhoto)
        val tvNom: TextView = vue.findViewById(R.id.tvNom)
        val tvTelephone: TextView = vue.findViewById(R.id.tvTelephone)
        val btnAppeler: ImageButton = vue.findViewById(R.id.btnAppeler)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun mettreAJour(liste: List<Contact>) {
        contacts = liste
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun appliquerPreferences(taille: Float, couleurTexte: Int, appel: Boolean) {
        tailleSp = taille
        couleur = couleurTexte
        appelVisible = appel
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val vue = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(vue)
    }

    override fun getItemCount(): Int = contacts.size

    override fun getItemId(position: Int): Long = contacts[position].id

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val context = holder.itemView.context

        // "★ Nom Prénom" pour un favori (bonus)
        val format = if (contact.favori) R.string.nom_complet_favori else R.string.nom_complet
        holder.tvNom.text = context.getString(format, contact.nom, contact.prenom)
        holder.tvTelephone.text = contact.telephone
        afficherPhoto(holder.ivPhoto, contact.photoUri)

        // Préférences : taille (nom, prénom, téléphone) et couleur (nom, prénom)
        holder.tvNom.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.tvTelephone.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.tvNom.setTextColor(couleur)

        // Bouton Appeler : GONE si la préférence est désactivée
        holder.btnAppeler.visibility = if (appelVisible) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onClic(contact) }
        // Le bouton consomme son clic : la fiche détaillée ne s'ouvre pas
        holder.btnAppeler.setOnClickListener { onAppel(contact) }
    }
}
