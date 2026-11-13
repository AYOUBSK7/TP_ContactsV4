# Contacts V4

TP Android Kotlin — Ayoub Boussoukaya, L3 DSI

Application de gestion de contacts : base SQLite, préférences utilisateur, fragments et Google Maps.

## Fonctionnalités

- Contacts enregistrés dans une base SQLite `contacts.db` (SQLiteOpenHelper + DAO, requêtes avec `?`)
- Ajouter (bouton +), modifier, supprimer (avec confirmation) : tout est gardé après fermeture
- Filtre au fil de la saisie (nom, prénom, téléphone)
- Appel, e-mail, photo (galerie ou caméra), position sur la carte
- Paramètres : taille de police, couleur du texte, bouton Appeler dans la liste
- Bonus : colonne `favori` ajoutée en version 2 de la base (onUpgrade) et préférence « Ordre de tri »

## Lancer le projet

1. Ouvrir le dossier dans Android Studio (Gradle JDK 17 ou 21).
2. Pour la carte, ajouter sa clé Google Maps dans `local.properties` (ce fichier n'est pas envoyé sur GitHub) :
   ```
   MAPS_API_KEY=votre_cle
   ```
3. Lancer sur un émulateur avec Google Play.

## Captures

| Liste après relance | Création | Détail | Modification |
|:-:|:-:|:-:|:-:|
| <img src="captures/01_liste.png" width="160"> | <img src="captures/02_creation.png" width="160"> | <img src="captures/03_detail.png" width="160"> | <img src="captures/04_modification.png" width="160"> |

| Suppression | Paramètres | Couleur | Taille |
|:-:|:-:|:-:|:-:|
| <img src="captures/05_suppression.png" width="160"> | <img src="captures/06_parametres.png" width="160"> | <img src="captures/07_choix_couleur.png" width="160"> | <img src="captures/08_choix_taille.png" width="160"> |

| Bleu, moyenne | Bleu, grande | Galerie | Permission caméra |
|:-:|:-:|:-:|:-:|
| <img src="captures/09_preferences_bleu_moyenne.png" width="160"> | <img src="captures/10_preferences_bleu_grande.png" width="160"> | <img src="captures/11_galerie.png" width="160"> | <img src="captures/12_permission_camera.png" width="160"> |

| Caméra | Composeur | Appel | E-mail |
|:-:|:-:|:-:|:-:|
| <img src="captures/13_camera.png" width="160"> | <img src="captures/14_composeur.png" width="160"> | <img src="captures/15_appel.png" width="160"> | <img src="captures/16_email.png" width="160"> |

| Carte |
|:-:|
| <img src="captures/17_carte.png" width="160"> |
