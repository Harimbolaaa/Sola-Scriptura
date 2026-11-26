# Schéma JSON des études (`schemaVersion = 1`)

Toutes les études sont définies en JSON externe dans `app/src/main/assets/studies/` ou téléchargées depuis Appwrite Storage, puis validées par `DialogueValidator`.

## Structure racine

| Champ | Type | Obligatoire | Description |
|---|---|---|---|
| `schemaVersion` | `Int` | Oui | Doit être `1` (`<= SUPPORTED_SCHEMA_VERSION`) |
| `studyId` | `String` | Oui | Identifiant stable (ex. `"etat_des_morts_001"`) |
| `contentVersion` | `Int` | Oui | Version incrémentale du contenu |
| `locale` | `String` | Oui | `"fr"`, `"mg"`, ou `"en"` |
| `title` | `String` | Oui | Titre affiché dans la bibliothèque |
| `subtitle` | `String` | Non | Sous-titre court |
| `description` | `String` | Non | Présentation méthodologique |
| `categoryId` | `String` | Oui | `foundations`, `salvation`, `forgotten_truths`, `prophecies` |
| `editorialStatus` | `String` | Non | `"DEMO"` ou `"REVIEWED"` |
| `prerequisiteStudyIds` | `List<String>` | Non | Prérequis utilisés pour l'Arbre de vérité |
| `startNodeId` | `String` | Oui | Identifiant du nœud de départ |
| `nodes` | `List<DialogueNodeDto>` | Oui | Liste des nœuds du graphe socratique |
| `conclusions` | `List<DialogueConclusionDto>` | Oui | Synthèses finales référencées par `conclusionId` |
| `verseDuels` | `List<VerseDuelDto>` | Non | Duels comparatifs dédiés |
| `investigation` | `BibleInvestigationDto?` | Non | Dossier d'enquête biblique à indices |

## Règles de validation (`DialogueValidator`, 15 règles)

1. JSON syntaxiquement valide et taille <= 2 Mo.
2. `studyId` et `title` non vides.
3. `schemaVersion` compris entre `1` et `SUPPORTED_SCHEMA_VERSION`.
4. Unicité de tous les `node.id`, `choice.id` (par nœud) et `conclusion.id`.
5. `startNodeId` présent dans `nodes`.
6. Toutes les phases et types de nœuds reconnus (`IRONY`, `ELENCHUS`, `MAIEUTICS`, `SYNTHESIS`, `REFLECTION`).
7. Chaque `choice.nextNodeId` non nul pointe vers un `node.id` existant.
8. Chaque `node.conclusionId` non nul pointe vers un `conclusion.id` existant.
9. Références bibliques valides (`bookId`, `bookName`, `chapter > 0`, `startVerse > 0`, `endVerse >= startVerse`).
10. Chaque nœud non terminal possède au moins une transition sortante ou une conclusion.
11. Tous les nœuds sont atteignables depuis `startNodeId` (aucun nœud orphelin).
12. Aucun cycle bloquant : tout parcours valide peut atteindre un nœud terminal ou de synthèse.
13. La distance minimale vers une sortie respecte `MAX_DIALOGUE_STEPS` (`60`).
