# Configuration Appwrite Cloud

L'intégration Appwrite Cloud est **entièrement facultative** : l'application fonctionne hors ligne sans aucune configuration serveur. Lorsque Appwrite est configuré, les utilisateurs peuvent créer un compte, synchroniser leurs progressions et télécharger des études supplémentaires.

## 1. Variables de configuration (`BuildConfig`)

Les identifiants publics côté client (jamais de clé API secrète serveur) peuvent être injectés via Gradle / Secrets panel ou `app/build.gradle.kts` :

- `APPWRITE_ENDPOINT` (ex. `https://cloud.appwrite.io/v1`)
- `APPWRITE_PROJECT_ID`
- `APPWRITE_DATABASE_ID`
- `APPWRITE_STUDIES_BUCKET_ID`

Si `APPWRITE_PROJECT_ID` est vide, `AppwriteClientProvider.isConfigured` retourne `false` et l'application reste en mode local sans erreur bloquante.

## 2. TablesDB dans Appwrite

### Table `studies_catalog` (lecture publique)
- `studyId` (String)
- `title` (String)
- `subtitle` (String)
- `description` (String)
- `categoryId` (String)
- `locale` (String)
- `contentVersion` (Integer)
- `schemaVersion` (Integer)
- `storageFileId` (String)
- `checksum` (String)
- `estimatedMinutes` (Integer)
- `difficulty` (String)
- `editorialStatus` (String)
- `published` (Boolean)

### Table `user_progress` (permissions strictes par utilisateur : `Role.user(userId)`)
- `userId` (String)
- `studyId` (String)
- `currentNodeId` (String)
- `historyJson` (String)
- `answersJson` (String)
- `unlockedNotionsJson` (String)
- `personalConclusion` (String)
- `isUnconvinced` (Boolean)
- `isCompleted` (Boolean)
- `startedAt` (Integer/Long)
- `lastOpenedAt` (Integer/Long)
- `completedAt` (Integer/Long, nullable)
- `updatedAt` (Integer/Long)

### Table `user_notes` (permissions strictes par utilisateur : `Role.user(userId)`)
- `userId`, `noteId`, `studyId`, `nodeId`, `noteType`, `title`, `content`, `verseRefLabel`, `createdAt`, `updatedAt`

### Table `user_preferences` (permissions strictes par utilisateur : `Role.user(userId)`)
- `userId`, `visualTheme`, `themeMode`, `language`, `updatedAt`

## 3. Bucket Storage (`studies_json`)

- Stocke les fichiers `.json` d'études validables par `DialogueValidator`.
- Permissions : lecture autorisée pour les utilisateurs authentifiés ou lecture publique selon le catalogue.
- Sécurité : aucune API Key secrète Appwrite ne doit être embarquée dans l'APK Android.
