package com.example.contactsv4

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

// Fonctions utilitaires pour les photos des contacts

// Dossier privé de l'application où sont copiées les photos
fun dossierPhotos(context: Context): File {
    val dossier = File(context.filesDir, "photos")
    dossier.mkdirs()
    return dossier
}

// Copie une image (galerie ou caméra) dans filesDir/photos, en version réduite.
// Renvoie le chemin de la copie, ou null si l'image est illisible.
// (Une URI de galerie est temporaire : après un redémarrage elle peut devenir illisible.)
fun copierPhoto(context: Context, uri: Uri): String? {
    return try {
        // a) lire seulement la taille de l'image
        val taille = BitmapFactory.Options()
        taille.inJustDecodeBounds = true
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, taille) }

        // b) réduire l'image (environ 800 px max) pour ne pas saturer la mémoire
        var echelle = 1
        while (taille.outWidth / echelle > 800 || taille.outHeight / echelle > 800) {
            echelle *= 2
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = echelle
        val image = context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        // c) enregistrer la copie en JPEG
        val fichier = File(dossierPhotos(context), "contact_${System.currentTimeMillis()}.jpg")
        FileOutputStream(fichier).use { image.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        fichier.absolutePath
    } catch (e: Exception) {
        null
    }
}

// Affiche la photo du contact, ou l'avatar par défaut
fun afficherPhoto(imageView: ImageView, chemin: String?) {
    imageView.clipToOutline = true // photo découpée en rond (fond @drawable/cercle)
    val bitmap = if (chemin != null && File(chemin).exists()) BitmapFactory.decodeFile(chemin) else null
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap)
    } else {
        imageView.setImageResource(R.drawable.ic_avatar)
    }
}
