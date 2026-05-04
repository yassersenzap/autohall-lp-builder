# API Contract V1

## 1. Vue generale

- Base URL locale: `http://localhost:8080/api/v1`
- Content-Type attendu pour les requetes JSON: `application/json`
- API admin:
  utilise les routes `/landing-pages`
  exige le header `X-Admin-Api-Key`
- API publique:
  utilise les routes `/public/landing-pages`
  ne doit pas envoyer `X-Admin-Api-Key`

Exemple de header admin:

```http
X-Admin-Api-Key: dev-admin-key-change-me
Content-Type: application/json
```

## 2. Modele LandingPageRequest

Structure JSON envoyee au backend pour creer ou mettre a jour une landing page.

```json
{
  "title": "Chery Tiggo 8 Pro Max",
  "slug": "chery-tiggo-8-pro-max",
  "description": "Landing page promotionnelle",
  "status": "DRAFT",
  "content": {
    "hero_title": "Nouveau Tiggo 8 Pro Max",
    "hero_image": "https://cdn.autohall.ma/tiggo-8.jpg",
    "price": "319000 MAD"
  }
}
```

Champs:

- `title`: string obligatoire, `max 160` caracteres.
- `slug`: string obligatoire, `max 120` caracteres, motif `^[a-z0-9]+(?:-[a-z0-9]+)*$`.
  uniquement lettres minuscules, chiffres et tirets simples.
- `description`: string optionnelle, `max 500` caracteres.
- `status`: string optionnelle, valeurs autorisees: `DRAFT`, `PUBLISHED`, `ARCHIVED`.
  si `null` ou vide, le backend cree la page en `DRAFT`.
- `content`: objet JSON flexible, optionnel.
  limite DTO: `max 50` cles de premier niveau.
  limite service: taille JSON serializee `max 200 KB`.

Validations metier supplementaires sur `content`:

- `hero_image`:
  si present, doit etre une URL `http` ou `https` valide, avec host, longueur maximale `2048`.
- `price`:
  si present, doit etre une string de `max 100` caracteres.

Comportements de normalisation:

- `title` est `trim()`.
- `slug` est `trim()` puis converti en lowercase.
- `description` est `trim()`, puis stockee a `null` si vide.

## 3. Modele LandingPageResponse

Structure JSON retournee par le backend.

```json
{
  "id": "0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f",
  "title": "Chery Tiggo 8 Pro Max",
  "slug": "chery-tiggo-8-pro-max",
  "description": "Landing page promotionnelle",
  "status": "PUBLISHED",
  "content": {
    "hero_title": "Nouveau Tiggo 8 Pro Max",
    "hero_image": "https://cdn.autohall.ma/tiggo-8.jpg",
    "price": "319000 MAD"
  },
  "createdAt": "2026-05-04T14:30:12.215",
  "updatedAt": "2026-05-04T14:33:40.781"
}
```

Champs:

- `id`: UUID de la landing page.
- `title`: titre normalise et stocke.
- `slug`: slug normalise et stocke.
- `description`: description ou `null`.
- `status`: `DRAFT`, `PUBLISHED` ou `ARCHIVED`.
- `content`: objet JSON libre.
- `createdAt`: date de creation.
- `updatedAt`: date de derniere mise a jour.

## 4. Modele ApiErrorResponse

Format d'erreur personnalise utilise par `GlobalExceptionHandler`.

```json
{
  "timestamp": "2026-05-04T14:35:18.921",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/landing-pages",
  "validationErrors": {
    "title": "Title is required",
    "slug": "Slug must contain lowercase letters, numbers and single hyphens only"
  }
}
```

Champs:

- `timestamp`: date et heure de l'erreur.
- `status`: code HTTP.
- `error`: libelle HTTP standard.
- `message`: message global lisible.
- `path`: chemin HTTP appele.
- `validationErrors`: map champ -> message.
  peut etre `null` pour les erreurs non liees a la validation.

## 5. Endpoints admin

Tous les endpoints admin ci-dessous exigent:

- Header `X-Admin-Api-Key`
- `Content-Type: application/json` pour `POST` et `PUT`

### GET /landing-pages

Objectif:
recuperer toutes les landing pages, quel que soit leur statut.

- Methode HTTP: `GET`
- URL complete: `http://localhost:8080/api/v1/landing-pages`
- Header requis: `X-Admin-Api-Key`
- Body JSON: aucun
- Reponse succes: `200 OK` avec `LandingPageResponse[]`
- Erreurs possibles:
  `401 Unauthorized` si header absent ou invalide
  `503 Service Unavailable` si la cle admin n'est pas configuree

Exemple PowerShell:

```powershell
curl.exe -X GET "http://localhost:8080/api/v1/landing-pages" `
  -H "X-Admin-Api-Key: dev-admin-key-change-me"
```

### GET /landing-pages/{id}

Objectif:
recuperer une landing page admin par son UUID.

- Methode HTTP: `GET`
- URL complete: `http://localhost:8080/api/v1/landing-pages/{id}`
- Header requis: `X-Admin-Api-Key`
- Body JSON: aucun
- Reponse succes: `200 OK` avec `LandingPageResponse`
- Erreurs possibles:
  `401 Unauthorized` si header absent ou invalide
  `404 Not Found` si l'UUID n'existe pas
  `503 Service Unavailable` si la cle admin n'est pas configuree

Exemple PowerShell:

```powershell
curl.exe -X GET "http://localhost:8080/api/v1/landing-pages/0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f" `
  -H "X-Admin-Api-Key: dev-admin-key-change-me"
```

### POST /landing-pages

Objectif:
creer une nouvelle landing page.

- Methode HTTP: `POST`
- URL complete: `http://localhost:8080/api/v1/landing-pages`
- Header requis:
  `X-Admin-Api-Key`
  `Content-Type: application/json`
- Body JSON: `LandingPageRequest`
- Reponse succes: `201 Created` avec `LandingPageResponse`
- Erreurs possibles:
  `400 Bad Request` si DTO invalide ou si `content` echoue aux validations metier
  `401 Unauthorized` si header absent ou invalide
  `409 Conflict` si le `slug` existe deja
  `503 Service Unavailable` si la cle admin n'est pas configuree

Exemple PowerShell:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/landing-pages" `
  -H "X-Admin-Api-Key: dev-admin-key-change-me" `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Chery Tiggo 8 Pro Max\",\"slug\":\"chery-tiggo-8-pro-max\",\"description\":\"Landing page promotionnelle\",\"status\":\"DRAFT\",\"content\":{\"hero_title\":\"Nouveau Tiggo 8 Pro Max\",\"hero_image\":\"https://cdn.autohall.ma/tiggo-8.jpg\",\"price\":\"319000 MAD\"}}"
```

### PUT /landing-pages/{id}

Objectif:
mettre a jour une landing page existante.

- Methode HTTP: `PUT`
- URL complete: `http://localhost:8080/api/v1/landing-pages/{id}`
- Header requis:
  `X-Admin-Api-Key`
  `Content-Type: application/json`
- Body JSON: `LandingPageRequest`
- Reponse succes: `200 OK` avec `LandingPageResponse`
- Erreurs possibles:
  `400 Bad Request` si DTO invalide ou si `content` echoue aux validations metier
  `401 Unauthorized` si header absent ou invalide
  `404 Not Found` si la page n'existe pas
  `409 Conflict` si le `slug` existe deja sur une autre page
  `503 Service Unavailable` si la cle admin n'est pas configuree

Regle de merge sur `content`:

- si `content` est `null`, le contenu existant est conserve
- si `content` est fourni, les cles envoyees fusionnent avec le contenu existant
- les cles absentes du payload ne sont pas supprimees

Exemple PowerShell:

```powershell
curl.exe -X PUT "http://localhost:8080/api/v1/landing-pages/0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f" `
  -H "X-Admin-Api-Key: dev-admin-key-change-me" `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Chery Tiggo 8 Pro Max - Mise a jour\",\"slug\":\"chery-tiggo-8-pro-max\",\"description\":\"Version publiee\",\"status\":\"PUBLISHED\",\"content\":{\"hero_title\":\"Nouvelle campagne\",\"price\":\"315000 MAD\"}}"
```

### DELETE /landing-pages/{id}

Objectif:
supprimer une landing page.

- Methode HTTP: `DELETE`
- URL complete: `http://localhost:8080/api/v1/landing-pages/{id}`
- Header requis: `X-Admin-Api-Key`
- Body JSON: aucun
- Reponse succes: `204 No Content`
- Erreurs possibles:
  `401 Unauthorized` si header absent ou invalide
  `404 Not Found` si la page n'existe pas
  `503 Service Unavailable` si la cle admin n'est pas configuree

Exemple PowerShell:

```powershell
curl.exe -X DELETE "http://localhost:8080/api/v1/landing-pages/0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f" `
  -H "X-Admin-Api-Key: dev-admin-key-change-me"
```

## 6. Endpoint public

### GET /public/landing-pages/{slug}

Objectif:
recuperer la landing page publique a afficher cote visiteur.

- Methode HTTP: `GET`
- URL complete: `http://localhost:8080/api/v1/public/landing-pages/{slug}`
- Header requis: aucun
- Accessible sans `X-Admin-Api-Key`
- Body JSON: aucun
- Reponse succes: `200 OK` avec `LandingPageResponse`
- Retourne uniquement les pages `PUBLISHED`
- Retourne `404 Not Found` si la page est `DRAFT`, `ARCHIVED` ou inexistante
- Erreurs possibles:
  `400 Bad Request` si le `slug` ne respecte pas le motif attendu

Exemple PowerShell:

```powershell
curl.exe -X GET "http://localhost:8080/api/v1/public/landing-pages/chery-tiggo-8-pro-max"
```

## 7. Exemples JSON

### Exemple de creation landing page

```json
{
  "title": "Chery Tiggo 8 Pro Max",
  "slug": "chery-tiggo-8-pro-max",
  "description": "Landing page promotionnelle",
  "status": "DRAFT",
  "content": {
    "hero_title": "Nouveau Tiggo 8 Pro Max",
    "hero_subtitle": "Disponible chez Auto Hall",
    "hero_image": "https://cdn.autohall.ma/tiggo-8.jpg",
    "price": "319000 MAD",
    "cta_label": "Demander un essai"
  }
}
```

### Exemple de reponse landing page

```json
{
  "id": "0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f",
  "title": "Chery Tiggo 8 Pro Max",
  "slug": "chery-tiggo-8-pro-max",
  "description": "Landing page promotionnelle",
  "status": "PUBLISHED",
  "content": {
    "hero_title": "Nouveau Tiggo 8 Pro Max",
    "hero_subtitle": "Disponible chez Auto Hall",
    "hero_image": "https://cdn.autohall.ma/tiggo-8.jpg",
    "price": "319000 MAD",
    "cta_label": "Demander un essai"
  },
  "createdAt": "2026-05-04T14:30:12.215",
  "updatedAt": "2026-05-04T14:33:40.781"
}
```

### Exemple erreur validation 400

```json
{
  "timestamp": "2026-05-04T14:35:18.921",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/landing-pages",
  "validationErrors": {
    "title": "Title is required",
    "slug": "Slug must contain lowercase letters, numbers and single hyphens only"
  }
}
```

### Exemple erreur unauthorized 401

Reponse non contractuelle en V1:
le `401` est declenche par `AdminApiKeyInterceptor` via `sendError`, donc le frontend doit surtout se baser sur le code HTTP.

Exemple minimal:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid admin API key"
}
```

### Exemple erreur not found 404

Exemple pour une erreur service comme `PUT /landing-pages/{id}` ou `DELETE /landing-pages/{id}`:

```json
{
  "timestamp": "2026-05-04T14:37:05.403",
  "status": 404,
  "error": "Not Found",
  "message": "Page not found",
  "path": "/api/v1/landing-pages/0d7f91a5-4d54-44dd-a3d8-0219d76fbb3f",
  "validationErrors": null
}
```

### Exemple erreur conflict 409

```json
{
  "timestamp": "2026-05-04T14:38:22.177",
  "status": 409,
  "error": "Conflict",
  "message": "Slug already exists",
  "path": "/api/v1/landing-pages",
  "validationErrors": null
}
```

## 8. Notes importantes pour le frontend

- Le frontend admin doit toujours envoyer `X-Admin-Api-Key` sur les routes `/landing-pages`.
- Le frontend public ne doit pas envoyer la cle admin.
- Le frontend public doit appeler `/public/landing-pages/{slug}`.
- Le champ `content` est flexible et sert de stockage pour le builder admin et le renderer public.
- Ne pas utiliser directement les routes admin pour afficher les pages publiques.
- Le frontend doit gerer les reponses `401` par code HTTP en priorite.
- Le frontend doit gerer le cas `404` avec corps vide sur certains `GET`.

## 9. Limites V1

- API key admin temporaire.
- Pas encore de Spring Security complet.
- Pas encore de users / roles.
- Pas encore de gestion leads.
- Pas encore de media upload.
- Pas encore de pagination sur la liste admin.
- Pas encore de DTO public separe de `LandingPageResponse`.

## 10. Prochaines etapes recommandees

- Mettre en place le chassis frontend.
- Creer un client API Axios centralise.
- Construire le dashboard admin.
- Construire la liste des landing pages.
- Construire le builder de landing pages.
- Construire la page publique `/p/:slug`.

## Remarques

- Un endpoint supplementaire existe dans le code admin: `GET /api/v1/landing-pages/slug/{slug}`.
  il retourne lui aussi uniquement une page `PUBLISHED`.
  il n'a pas ete demande dans la liste principale ci-dessus.
- `GET /api/v1/landing-pages/slug/{slug}` est exclu de l'interceptor admin via `WebConfig`.
  en pratique, cette route est accessible sans `X-Admin-Api-Key`, ce qui la rapproche d'une route publique.
- `GET /api/v1/landing-pages/{id}` et `GET /api/v1/public/landing-pages/{slug}` retournent `404` avec un corps vide quand la ressource n'existe pas.
  a l'inverse, les `404` provenant du service sur `PUT` et `DELETE` retournent un `ApiErrorResponse`.
- La documentation ci-dessus fige le contrat actuel sans modifier le backend.
