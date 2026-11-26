# Guide de rédaction des études (`CONTENT_AUTHORING.md`)

Ce guide s'adresse aux auteurs souhaitant créer de nouvelles études socratiques compatibles avec **Sola Scriptura : Maïeutique**.

## Principes éditoriaux

1. **Respecter la méthode maïeutique en 4 phases** :
   - `IRONY` : question ouverte révélant les présupposés initiaux sans jugement.
   - `ELENCHUS` : confrontation directe à un passage biblique précis dans son contexte.
   - `MAIEUTICS` : questions progressives reliant plusieurs passages pour faire émerger la compréhension.
   - `SYNTHESIS` : synthèse structurée laissant place à l'avis personnel.
2. **Distinguer les 4 niveaux de discours** :
   - `verseReferences[].quotedText` : uniquement le texte biblique exact (avec `translationId` et `licenseId`).
   - `contextText` / `verseReferences[].contextSummary` : contexte historique, littéraire ou grammatical factuel.
   - `proposedInterpretation` : lecture théologique adventiste du 7e jour proposée avec honnêteté.
   - `reflectionQuestion` : question ouverte invitant l'utilisateur à formuler sa conclusion personnelle.
3. **Inclure des choix non bloquants** :
   - `NOT_CONVINCED` (*« Je ne suis pas convaincu »*)
   - `EXAMINE_CONTEXT` (*« Examiner le contexte »*)
   - `DONT_KNOW` (*« Je ne connais pas la réponse »*)
   - `PREVIOUS_QUESTION` (*« Revenir à la question précédente »*)
4. **Statut éditorial honnête** :
   - Utiliser `"editorialStatus": "DEMO"` pour les brouillons/démonstrations, et `"REVIEWED"` uniquement après relecture théologique et biblique complète.
5. **Multilinguisme** :
   - Utiliser le même `studyId` (ex. `"etat_des_morts_001"`) avec des fichiers distincts par `locale` (`"fr"`, `"mg"`, `"en"`).
