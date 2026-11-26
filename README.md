# Sola Scriptura : Maïeutique

Application Android native d'étude biblique socratique **100 % hors ligne par défaut**, développée en Kotlin et Jetpack Compose Material 3.

## Philosophie et Clarté Méthodologique

- **Démarche maïeutique** : l'utilisateur est invité à questionner, lire les passages dans leur contexte littéraire et historique, comparer les Écritures entre elles et formuler sa propre synthèse.
- **Aucune IA / aucun LLM** : toute la logique repose sur un moteur de graphe conversationnel déterministe (`DialogueEngine`) et des fichiers d'études externes au format JSON validés avant importation.
- **Transparence doctrinale** : l'application propose une perspective théologique **adventiste du septième jour**, tout en distinguant visuellement quatre niveaux dans l'interface :
  1. **Texte biblique** (`BIBLICAL_TEXT`)
  2. **Contexte historique & littéraire** (`HISTORICAL_CONTEXT`)
  3. **Interprétation proposée (SDA)** (`PROPOSED_INTERPRETATION`)
  4. **Opinion personnelle de l'utilisateur** (`PERSONAL_OPINION`)
- **Liberté de conscience** : options non bloquantes permanentes (*« Je ne suis pas convaincu »*, *« Examiner le contexte »*, *« Comparer les passages »*, *« Revenir à la question précédente »*, *« Je ne connais pas la réponse »*).

## Fonctionnalités principales

- **Bibliothèque structurée en 4 saisons** : Fondations, Plan du Salut, Vérités bibliques et Prophéties.
- **Moteur socratique hors ligne** : 4 phases (*Ironie / Question initiale*, *Elenchus / Confrontation*, *Maïeutique*, *Synthèse*), reprise automatique, historique du raisonnement et protection contre les boucles infinies.
- **Duel de versets** & **Enquête biblique** : comparaison de passages et assemblage d'indices avec sauvegarde dans le carnet.
- **Arbre de vérité** : carte thématique interactive (pan/zoom `Canvas`) générée localement depuis les études et progressions.
- **Carnet de réflexion** : conclusions, questions, notes, passages clés, difficultés, favoris et export Markdown.
- **Défi du jour déterministe** : calculé localement sans connexion réseau.
- **Personnalisation complète** : 6 thèmes visuels (*Classic Paper*, *Midnight Study*, *Sage Nature*, *Warm Sand*, *Minimal Cloud*, *High Contrast*), mode clair/sombre/système, Material You (Android 12+), 4 typographies locales et 3 langues d'interface (`fr`, `mg`, `en`).
- **Synchronisation optionnelle Appwrite Cloud** : création de compte facultative, stockage sécurisé par utilisateur, file locale `sync_queue` et fusion déterministe multi-appareils.

## Stack technique

- **Langage & UI** : Kotlin, Jetpack Compose, Material Design 3, Navigation Compose (`@Serializable`)
- **Architecture** : Clean Architecture + MVVM + Hilt DI
- **Persistance locale** : Room (SQLite) + Jetpack DataStore Preferences
- **Sérialisation & Validation** : `kotlinx.serialization` + `DialogueValidator` (15 règles)
- **Cloud & Arrière-plan** : SDK officiel Appwrite Android (`TablesDB`, `Account`, `Storage`) + WorkManager
- **Tests** : JUnit 4, Kotlin Coroutines Test, Room in-memory, Robolectric + Compose UI Test

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Base de données locale (Room)](docs/DATABASE.md)
- [Schéma JSON des études](docs/JSON_SCHEMA.md)
- [Configuration Appwrite Cloud](docs/APPWRITE_SETUP.md)
- [Guide de rédaction des études](docs/CONTENT_AUTHORING.md)
- [Règles de synchronisation et fusion](docs/SYNC_RULES.md)
- [Historique des versions](docs/CHANGELOG.md)
