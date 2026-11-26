# Règles de synchronisation et de fusion (`SYNC_RULES.md`)

L'application est **offline-first** : toute action de lecture, prise de note ou progression est d'abord persistée dans Room, puis mise en file d'attente dans `sync_queue`.

## Politique de fusion (`ProgressMerger`)

Lorsqu'une progression locale et une progression distante existent pour un même `studyId` :

1. **Complétion monotone** : une étude marquée `isCompleted = true` sur l'un des appareils ne redevient jamais incomplète.
2. **Union des étapes visitées** : les `DialogueHistoryStep` locaux et distants sont fusionnés par union (`nodeId + choiceId`) et triés chronologiquement.
3. **Notions acquises** : union des `unlockedNotions`.
4. **Réponses sélectionnées** : fusion des clés distinctes ; en cas de clé commune, la version ayant le `updatedAt` le plus récent prévaut.
5. **Branche active** : le `currentNodeId` le plus récent est retenu, sauf si une version est déjà terminée.
6. **Conclusions personnelles** : si les deux appareils ont saisi des conclusions différentes, **aucune n'est écrasée** ; les deux textes sont combinés avec séparation claire (`hasUnresolvedConflict = true`).

## Fonctionnement hors ligne et reprise (`SyncWorker`)

- `SyncScheduler` planifie un `OneTimeWorkRequest` (contrainte `NetworkType.CONNECTED` ou `UNMETERED` selon `SyncMode`) et un `PeriodicWorkRequest` toutes les 12 heures si la synchronisation automatique est activée.
- En cas d'échec réseau, `SyncQueueEntity` passe à `FAILED` avec incrément de `retryCount` et reprise automatique par WorkManager (`Result.retry()`).
- Si Appwrite n'est pas configuré ou si l'utilisateur choisit de rester anonyme/hors ligne, aucune donnée n'est envoyée sur le réseau.
