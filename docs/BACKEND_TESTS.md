# Backend Tests

## 1. Objectif des tests backend

Les tests backend valident les comportements critiques de la V1 avant le lancement du frontend final. Ils protègent les règles de création, publication, lecture publique, suppression et validation des landing pages automobiles.

## 2. Types de tests ajoutés

- Tests unitaires de service avec JUnit 5 et Mockito.
- Tests de contrôleurs avec MockMvc.
- Tests de validation et de format d'erreur via `GlobalExceptionHandler`.
- Tests de l'interceptor admin par header `X-Admin-Api-Key`.

## 3. Règles métier testées

- Une page créée sans statut explicite devient `DRAFT`.
- Un slug dupliqué est refusé avec une erreur `409 Conflict`.
- Les slugs sont normalisés avec `trim()` et `lowercase`.
- La mise à jour modifie les champs principaux et fusionne le contenu JSON existant.
- Un slug déjà utilisé par une autre page est refusé à la mise à jour.
- La lecture publique ne retourne que les pages `PUBLISHED`.
- `hero_image` doit être une URL HTTP ou HTTPS valide.
- `price` doit être une chaîne de 100 caractères maximum.
- La suppression retourne `404 Not Found` si la page n'existe pas.
- Les routes admin exigent une API key valide.
- Les routes publiques restent accessibles sans API key.
- Les slugs publics invalides retournent `400 Bad Request`.

## 4. Fichiers de test créés

- `backend/src/test/java/com/autohall/lpbuilderapi/services/LandingPageServiceTest.java`
- `backend/src/test/java/com/autohall/lpbuilderapi/controllers/LandingPageControllerTest.java`
- `backend/src/test/java/com/autohall/lpbuilderapi/controllers/PublicLandingPageControllerTest.java`

## 5. Comment lancer les tests

Depuis le dossier `backend` :

```bash
mvn test
```

Pour compiler, tester et produire le package :

```bash
mvn package
```

## 6. Résultat attendu

- Tous les tests unitaires et contrôleur passent.
- Les tests service ne nécessitent pas PostgreSQL, car `LandingPageRepository` est mocké.
- Les tests contrôleur utilisent une clé dédiée :

```properties
app.security.admin-api-key=test-admin-key
```

## 7. Limites restantes

- Pas encore de tests d'intégration JPA avec une base de test.
- Pas encore de tests de migration ou de schéma PostgreSQL JSONB.
- Pas encore de tests sécurité Spring Security, car le projet utilise encore l'API key temporaire.
- Pas encore de tests pour les futurs modules leads, media et auth.

## 8. Recommandations futures

- Ajouter des tests d'intégration repository avec Testcontainers PostgreSQL quand le modèle de données se stabilise.
- Ajouter des tests de contrat API lorsque le frontend commence à consommer les endpoints.
- Couvrir les futurs endpoints leads, media et auth dès leur introduction.
- Ajouter une étape CI qui exécute `mvn test` à chaque pull request.
