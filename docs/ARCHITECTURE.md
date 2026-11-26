# Architecture — Sola Scriptura : Maïeutique

L'application suit une architecture **Clean Architecture + MVVM** strictement découplée :

```
UI (Jetpack Compose)
  └── ViewModel (StateFlow / Hilt)
        └── Domain (UseCases + Repository Interfaces + DomainModels)
              └── Data (OfflineFirstRepositories + Mappers)
                    ├── Local Sources: Room (SQLite), DataStore, Bundled JSON Assets
                    ├── Engine: DialogueEngine (Pur Kotlin, sans dépendance Android)
                    └── Remote Source: Appwrite Cloud SDK (Account, TablesDB, Storage)
```

## Organisation des packages

- `com.example` : point d'entrée Android (`SolaScripturaApp`, `MainActivity`) conservant le namespace Gradle.
- `com.harimbola.solascriptura.core.common` : constantes, types `AppError` / `AppResult`, gestionnaire de locale dynamique `LocaleHelper`.
- `com.harimbola.solascriptura.core.designsystem` : 6 thèmes visuels complets, palettes sémantiques, typographies locales, dimensions adaptatives et composants réutilisables (`SolaSurfaceCard`, `DiscourseLayerPill`, `ThemePreviewCard`).
- `com.harimbola.solascriptura.core.datastore` : persistance asynchrone des préférences utilisateur et d'apparence.
- `com.harimbola.solascriptura.core.database` : base Room (`SolaScripturaDatabase`), 7 entités et DAOs réactifs `Flow`.
- `com.harimbola.solascriptura.core.json` : DTOs `kotlinx.serialization`, `DialogueValidator` (15 règles de validation atomique) et `AssetStudyLoader`.
- `com.harimbola.solascriptura.core.engine` : moteur socratique pur Kotlin (`DialogueEngine`, `DialogueGraph`, `DialogueSession`, `DialogueHistory`, `DialogueTransition`, `DialogueProgressManager`).
- `com.harimbola.solascriptura.core.appwrite` : intégration officielle Appwrite Android (`Account`, `TablesDB`, `Storage`) avec mode hors ligne gracieux si non configuré.
- `com.harimbola.solascriptura.core.sync` : fusion déterministe (`ProgressMerger`), `SyncWorker` (WorkManager) et `SyncScheduler`.
- `com.harimbola.solascriptura.domain` : modèles métier, contrats de repositories et cas d'usage (`BuildTruthTreeUseCase`, `CalculateDailyChallengeUseCase`, `CalculateProgressStatsUseCase`).
- `com.harimbola.solascriptura.data` : implémentations offline-first et mappers JSON/Entity/Domain.
- `com.harimbola.solascriptura.feature.*` : écrans et ViewModels autonomes (`onboarding`, `home`, `studies`, `dialogue`, `duel`, `investigation`, `truthmap`, `notebook`, `dailychallenge`, `progress`, `account`, `settings`).
