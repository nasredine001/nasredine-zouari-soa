# TP4 — Spring for GraphQL + appels batch REST

Ce projet étend le TP3 (micro-services Spring Cloud) avec :

1. un **endpoint batch REST** dans le service Produits ;
2. un **micro-service Commandes** (REST + JPA + Feign) ;
3. un **BFF GraphQL** (`bff-service`) avec DataLoader anti-N+1 ;
4. deux **front-ends Thymeleaf** (Produits et Commandes) ;
5. le **filtrage des commandes** : toutes, livrées, non livrées, ou par ID.

## Architecture

```
                        Navigateur
                            │
                            ▼
        ProxyService / Spring Cloud Gateway (:8080)
        ├── /produits/**          → ProductService (:8081)
        ├── /commandes/**         → OrderService (:8084)
        ├── /graphql, /graphiql/**→ BffService (:8085)
        ├── /app/products/**      → ProductFrontend (:8082)
        └── /app/orders/**        → OrderFrontend (:8083)

        ConfigService (:8888)  ← configuration centralisée
        DiscoveryService (:8761) ← annuaire Eureka

        OrderService  ──Feign batch──► ProductService
        BffService    ──Feign batch──► ProductService
        BffService    ──Feign────────► OrderService
        OrderFrontend ──GraphQL──────► BffService
        ProductFrontend ──REST───────► ProductService
```

## Configuration centralisée

Les fichiers de configuration sont dans
`ConfigService/src/main/resources/myConfig`. Le Config Server utilise le
dépôt Git du projet :

```properties
spring.cloud.config.server.git.uri=file:${user.dir}
spring.cloud.config.server.git.search-paths=ConfigService/src/main/resources/myConfig
spring.cloud.config.server.git.default-label=main
```

Il faut donc lancer ConfigService depuis la racine du projet
(`nasredine-zouari-soa`) et committer les modifications de `myConfig` pour
qu'elles soient servies. Il ne faut **pas** créer un dépôt Git séparé dans
`myConfig`.

## Ports

| Service | Port | Rôle |
|---|---:|---|
| ProxyService | 8080 | API Gateway (point d'entrée) |
| ProductService | 8081 | CRUD produits + batch |
| ProductFrontend | 8082 | Interface Thymeleaf produits |
| OrderFrontend | 8083 | Interface Thymeleaf commandes |
| OrderService | 8084 | CRUD commandes + livraison |
| BffService | 8085 | API GraphQL (BFF) |
| ConfigService | 8888 | Spring Cloud Config |
| DiscoveryService | 8761 | Eureka |

## Nouveautés TP4

### Endpoint batch Produits

```http
POST /produits/batch
Content-Type: application/json

[1, 2, 3]
```

Un seul appel HTTP renvoie tous les produits demandés. Utilisé par `OrderService`
et par le DataLoader du BFF.

### Commandes et livraison

- `GET /commandes` : toutes les commandes
- `GET /commandes/{id}` : une commande
- `GET /commandes/client/{customerId}` : commandes d'un client
- `GET /commandes/non-livrees` : commandes non livrées
- `GET /commandes/livrees` : commandes livrées
- `POST /commandes` : créer une commande (prix/stock validés via le batch Produits)
- `PUT /commandes/{id}/statut?statut=VALIDATED` : changer le statut
- `PUT /commandes/{id}/livraison?livre=true` : marquer livrée / non livrée
- `DELETE /commandes/{id}` : supprimer

Le repository contient bien :

```java
List<Order> findByDeliveredFalse();
List<Order> findByDeliveredTrue();
```

### GraphQL (BFF)

Console : <http://localhost:8080/graphiql>

Requêtes :

```graphql
query Toutes {
  orders {
    id customerId orderDate status totalAmount delivered
    items { quantity unitPrice product { name price } }
  }
}

query Livrees {
  ordersByDelivery(delivered: true) {
    id delivered totalAmount
    items { quantity product { name price } }
  }
}

query NonLivrees {
  ordersByDelivery(delivered: false) { id delivered totalAmount }
}

query ParId {
  orderById(id: 1) { id status delivered totalAmount }
}
```

Mutation :

```graphql
mutation Creer {
  createOrder(input: {
    customerId: 3
    items: [{ productId: 1, quantity: 2 }]
  }) { id status totalAmount delivered }
}

mutation Livrer {
  updateOrderDelivery(id: 1, delivered: true) { id delivered }
}
```

Le champ `OrderItem.product` est résolu par un **DataLoader** : tous les
`productId` d'une requête sont regroupés en un seul `POST /produits/batch`.

La console GraphiQL est **auto-hébergée** : ses fichiers (React, GraphiQL)
sont dans `BffService/src/main/resources/static/graphiql/`. Elle ne dépend
donc plus de `unpkg.com` (dont `graphiql.min.js` renvoie 404, ce qui bloquait
la page sur « Loading... »).

### Filtres du front-end Commandes

- <http://localhost:8080/app/orders/commandes> : toutes
- `...?filtre=livrees` : livrées
- `...?filtre=non-livrees` : non livrées
- `...?id=1` : recherche par ID
- Bouton « Marquer livrée / non livrée » sur chaque ligne

## Lancer le projet

Prérequis : **JDK 17** et Maven.

### Dans IntelliJ IDEA (recommandé)

1. Ouvrir le dossier `nasredine-zouari-soa` (le projet est déjà importé via `build-all-pom.xml`).
2. Vérifier **File → Project Structure → Project SDK = 17** (`ms-17`).
3. Dans la liste des configurations (en haut à droite), choisir et lancer
   dans cet ordre :
   - `1. ConfigService`
   - `2. DiscoveryService`
   - `3. ProductService`
   - `4. OrderService`
   - `5. BffService`
   - `6. ProductFrontend`
   - `7. OrderFrontend`
   - `8. ProxyService`

   Les configurations sont dans le dossier `.run/` du projet. Attendre que
   chaque service affiche `Started ...Application` avant de lancer le suivant.
4. Ouvrir <http://localhost:8080/app/orders/commandes>.

Si les configurations n'apparaissent pas : clic droit sur `build-all-pom.xml`
→ **Add as Maven Project**, puis **Reload All Maven Projects**.

### En ligne de commande

```powershell
# Depuis le dossier nasredine-zouari-soa
$env:JAVA_HOME = "C:\Users\aimen\.jdks\ms-17.0.20.1"

# 1. Compiler tous les modules
mvn -f build-all-pom.xml package -DskipTests

# 2. Démarrer les 8 services et ouvrir le navigateur
.\start-all.ps1

# 3. Tout arrêter
.\stop-all.ps1
```

Ou lancer chaque service manuellement dans l'ordre :
ConfigService → DiscoveryService → ProductService → OrderService →
BffService → ProductFrontend → OrderFrontend → ProxyService.

## URLs utiles

| URL | Description |
|---|---|
| <http://localhost:8080/app/orders/commandes> | Front-end commandes |
| <http://localhost:8080/app/products/produits> | Front-end produits |
| <http://localhost:8080/graphiql> | Console GraphiQL |
| <http://localhost:8080/produits> | API produits via Gateway |
| <http://localhost:8080/commandes/non-livrees> | API non livrées via Gateway |
| <http://localhost:8761> | Tableau de bord Eureka |
| <http://localhost:8888/product-service/default> | Configuration produits |

## Scénario de test rapide

1. Ouvrir le front-end Produits et ajouter un produit.
2. Ouvrir le front-end Commandes, créer une commande avec 2 articles.
3. Vérifier le total calculé par `OrderService` (appel batch Produits).
4. Cliquer sur « Marquer livrée », puis filtrer « Livrées » / « Non livrées ».
5. Rechercher la commande par son ID.
6. Dans GraphiQL, exécuter `orders` avec `items { product { name price } }`
   et vérifier dans les logs de `ProductService` qu'un seul appel batch est reçu.
