# Todo Backend

API REST Spring Boot pour la gestion de tâches (authentification JWT + CRUD de tâches), avec déploiement conteneurisé sur AWS via Terraform et Ansible, et pipeline CI/CD GitHub Actions.

## Stack

- Java 17 / Spring Boot
- Spring Security (JWT)
- Spring Data JPA + MySQL
- Swagger / OpenAPI (springdoc)
- Docker / docker-compose
- Terraform (provisioning AWS)
- Ansible (configuration & déploiement)
- GitHub Actions (CI/CD)

## Lancement local

```bash
# Démarrer uniquement la base MySQL via docker-compose
docker-compose up -d mysql

# Lancer l'application Spring Boot
mvn spring-boot:run
```

L'API est alors disponible sur `http://localhost:8080`.

Alternative full-stack avec docker-compose (backend + MySQL) :

```bash
docker-compose up -d --build
```

## Documentation API (Swagger)

Une fois l'application démarrée : http://localhost:8080/swagger-ui/index.html

## Endpoints principaux

### Auth (`/api/v1/auth`) — public

| Méthode | Endpoint         | Description                    |
|---------|------------------|---------------------------------|
| POST    | `/register`      | Créer un nouveau compte utilisateur |
| POST    | `/login`         | Authentifier un utilisateur (retourne un JWT) |

### Tasks (`/api/v1/tasks`) — authentifié (Bearer JWT)

| Méthode | Endpoint      | Description                              |
|---------|---------------|-------------------------------------------|
| GET     | `/`           | Lister les tâches paginées de l'utilisateur |
| GET     | `/{id}`       | Détail d'une tâche                         |
| POST    | `/`           | Créer une nouvelle tâche                   |
| PUT     | `/{id}`       | Modifier une tâche existante               |
| DELETE  | `/{id}`       | Supprimer une tâche                        |

Toutes les réponses sont enveloppées dans un `ApiResponse` standard (`success`, `message`, `data`).

## Infrastructure — Terraform

Le dossier `terraform/` provisionne l'infrastructure AWS (instances EC2 front/back/db, réseau, sécurité).

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # à adapter avec vos valeurs
terraform init
terraform plan
terraform apply
```

Récupérez ensuite les IP générées avec `terraform output` pour compléter `ansible/inventory/hosts.ini`.

## Déploiement — Ansible

Le dossier `ansible/` configure et déploie les instances (Nginx reverse proxy, conteneurs backend/frontend, MySQL) via les rôles `common`, `back`, `db` et `front`.

```bash
cd ansible
# Compléter inventory/hosts.ini avec les IP issues de terraform output
ansible-playbook site.yml --ask-vault-pass
```

Le rôle `front` installe Nginx et déploie un reverse proxy (`templates/todo.conf.j2`) qui route `/` vers le conteneur frontend (port 4200) et `/api/` vers le backend (`back_private_ip:8080`).

## CI/CD — GitHub Actions

Le workflow `.github/workflows/ci-cd.yml` construit, teste, package l'image Docker du backend, la publie sur Docker Hub, puis déploie automatiquement sur l'instance EC2 via Ansible/SSH.

### Secrets GitHub requis

| Secret                 | Description                                      |
|-------------------------|---------------------------------------------------|
| `DOCKER_USERNAME`       | Identifiant Docker Hub                             |
| `DOCKER_PASSWORD`       | Mot de passe / token Docker Hub                    |
| `EC2_HOST`              | IP publique de l'instance front (bastion SSH)      |
| `EC2_USER`              | Utilisateur SSH (ex : `ec2-user`)                  |
| `SSH_PRIVATE_KEY`       | Clé privée SSH pour se connecter aux instances     |
| `BACK_PRIVATE_IP`       | IP privée de l'instance backend                    |
| `DB_PRIVATE_IP`         | IP privée de l'instance base de données            |
| `MYSQL_ROOT_PASSWORD`   | Mot de passe root MySQL                            |
| `MYSQL_DATABASE`        | Nom de la base de données MySQL                    |

## Build

```bash
mvn -q package -DskipTests
```

Le jar exécutable est généré dans `target/`.
