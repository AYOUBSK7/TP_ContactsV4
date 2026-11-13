package com.example.contactsv4

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactsv4.data.Contact
import com.example.contactsv4.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel partagé par les fragments (activityViewModels).
// Il survit à la rotation et fait tous les appels au DAO hors du thread principal.
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ContactDao(application)
    private val preferences = PreferencesManager(application)

    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> = _contacts

    // Texte du filtre (gardé ici pour ne pas le perdre à la rotation)
    var filtre = ""
        private set

    // Recharge la liste depuis la base (avec le filtre et l'ordre de tri actuels)
    fun chargerContacts() {
        viewModelScope.launch {
            val tri = preferences.ordreTri()
            val texte = filtre.trim()
            val liste = withContext(Dispatchers.IO) {
                if (texte.isEmpty()) dao.getAll(tri) else dao.search(texte, tri)
            }
            _contacts.value = liste
        }
    }

    fun filtrer(texte: String) {
        if (texte == filtre) return
        filtre = texte
        chargerContacts()
    }

    // Lecture d'un contact par son _id (null s'il n'existe plus)
    suspend fun getContact(id: Long): Contact? {
        return withContext(Dispatchers.IO) { dao.getById(id) }
    }

    // Insertion (id = 0) ou mise à jour. Renvoie true si la base a bien été modifiée.
    suspend fun enregistrer(contact: Contact): Boolean {
        val ok = withContext(Dispatchers.IO) {
            if (contact.id == 0L) {
                dao.insert(contact) != -1L   // insert renvoie -1 en cas d'échec
            } else {
                dao.update(contact) > 0      // update renvoie le nombre de lignes modifiées
            }
        }
        if (ok) chargerContacts() // la liste est notifiée après chaque modification
        return ok
    }

    suspend fun supprimer(id: Long): Boolean {
        val ok = withContext(Dispatchers.IO) { dao.delete(id) > 0 }
        if (ok) chargerContacts()
        return ok
    }
}
