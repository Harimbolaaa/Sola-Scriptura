# Base de données locale — Room (SQLite)

`SolaScripturaDatabase` (version 1, export de schéma activé vers `app/schemas`) est la **source de vérité unique** pour l'ensemble des données hors ligne.

## Tables (7 entités)

1. **`studies`** (`StudyEntity`)
   - Clé primaire composite `id = "${studyId}_${locale}"` permettant la coexistence de variantes multilingues (`fr`, `en`, `mg`) pour une même étude logique `studyId`.
   - Contient le JSON complet (`jsonContent`), la somme SHA-256 (`checksum`), la version de schéma et de contenu, `editorialStatus` (`DEMO` / `REVIEWED`) et `isBundledAsset`.
2. **`categories`** (`CategoryEntity`)
   - Les 4 saisons d'étude (`foundations`, `salvation`, `forgotten_truths`, `prophecies`).
3. **`dialogue_progress`** (`DialogueProgressEntity`)
   - Progression active par `studyId` : `currentNodeId`, `historyJson`, `answersJson`, `unlockedNotionsCsv`, `personalConclusion`, `isUnconvinced`, `isCompleted`, horodatages `startedAt`, `lastOpenedAt`, `completedAt`, `updatedAt`.
4. **`user_notes`** (`UserNoteEntity`)
   - Carnet de réflexion (`CONCLUSION`, `QUESTION`, `NOTE`, `VERSE`, `DIFFICULTY`) avec référence biblique optionnelle.
5. **`favorites`** (`FavoriteEntity`)
   - Études marquées en favori par `studyId`.
6. **`sync_queue`** (`SyncQueueEntity`)
   - File d'attente transactionnelle locale (`PENDING`, `FAILED`, `DONE`) avec compteur `retryCount` pour la synchronisation différée vers Appwrite.
7. **`reading_history`** (`ReadingHistoryEntity`)
   - Journal local de consultation des nœuds afin de calculer les statistiques de lecture de manière fiable.

## Migrations et intégrité

- Toute évolution future du schéma doit implémenter un objet `Migration(from, to)` explicite (`MIGRATION_1_2` est pré-enregistrée comme modèle). `fallbackToDestructiveMigration()` est strictement proscrit pour ne jamais perdre les notes ou progressions utilisateur.
- Avant toute mise à jour d'étude dans `studies`, `DialogueValidator` valide intégralement le JSON ; en cas d'échec, l'étude existante reste intacte.
