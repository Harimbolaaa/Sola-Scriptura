# Changelog

## 1.0.0

- Architecture Clean Architecture + MVVM avec Hilt DI, Room (`SolaScripturaDatabase`), DataStore et Navigation Compose typée (`@Serializable`).
- Moteur socratique déterministe (`DialogueEngine`) en 4 phases (`IRONY`, `ELENCHUS`, `MAIEUTICS`, `SYNTHESIS`), historique, retour arrière, choix non bloquants et protection contre les cycles (`MAX_DIALOGUE_STEPS = 60`).
- Validateur atomique JSON (`DialogueValidator`, 15 règles) et 14 fichiers d'études embarqués sur 4 saisons (`foundations`, `salvation`, `forgotten_truths`, `prophecies`) avec variantes multilingues `fr` / `en` / `mg`.
- 6 thèmes visuels complets (*Classic Paper*, *Midnight Study*, *Sage Nature*, *Warm Sand*, *Minimal Cloud*, *High Contrast*), Material You (Android 12+), 4 typographies locales et prise en charge dynamique FR / MG / EN.
- Écrans dédiés : Onboarding, Accueil, Bibliothèque & Détail, Dialogue Socratique, Duel de Versets, Enquête Biblique, Arbre de Vérité interactif (`Canvas`), Carnet avec export Markdown, Défi du jour déterministe, Cheminement, Compte Appwrite Cloud optionnel et Paramètres.
- Intégration Appwrite Cloud (`Account`, `TablesDB`, `Storage`), fusion déterministe (`ProgressMerger`) et synchronisation différée (`SyncWorker`).
- Suite complète de tests unitaires, Room in-memory, validation des assets JSON et tests UI Compose sous Robolectric.
