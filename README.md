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


1. Chiffrer le vault Ansible

cd /Users/user/Documents/Projet/TD-DEVOPS/todo-backend/ansible
ansible-vault encrypt group_vars/vault.yml
Il te demande de créer un mot de passe de vault — choisis-en un et retiens-le, tu le retaperas à chaque commande Ansible ci-dessous. Le fichier contient déjà mysql_root_password: "TodoDB2024!Secure", identique à ton terraform.tfvars — pas besoin d'y toucher.

2. Tester la connexion SSH (attends ~1 min que les instances finissent de booter)

ansible all -m ping --ask-vault-pass
Tu dois voir SUCCESS pour les 3 hosts (front, back, db — ce dernier passe par ProxyJump via front).

3. Configurer les serveurs

ansible-playbook site.yml --ask-vault-pass
Ça installe Docker partout, Nginx+Certbot sur front, et lance les conteneurs backend/mysql/frontend.

4. Renseigner les secrets GitHub manquants pour le CD
Avec les valeurs qu'on a maintenant :


cd /Users/user/Documents/Projet/TD-DEVOPS/todo-backend
gh secret set EC2_HOST --body "18.145.19.9"
gh secret set EC2_USER --body "ec2-user"
gh secret set SSH_PRIVATE_KEY < ~/.ssh/id_rsa
gh secret set BACK_PRIVATE_IP --body "10.0.2.227"
gh secret set DB_PRIVATE_IP --body "10.0.2.132"
gh secret set MYSQL_ROOT_PASSWORD --body "TodoDB2024!Secure"
gh secret set MYSQL_DATABASE --body "todo_db"

cd /Users/user/Documents/Projet/TD-DEVOPS/todo-frontend
gh secret set EC2_HOST --body "18.145.19.9"
gh secret set EC2_USER --body "ec2-user"
gh secret set SSH_PRIVATE_KEY < ~/.ssh/id_rsa
5. Déclencher le CI/CD complet

cd /Users/user/Documents/Projet/TD-DEVOPS/todo-backend
git commit --allow-empty -m "test: deploy via CD on AWS"
git push origin main
gh run watch
Puis pareil côté todo-frontend.

Fais les étapes 1 à 3 d'abord et colle-moi la sortie (surtout le résultat du ping et la fin du ansible-playbook) avant de passer aux secrets, pour vérifier que l'infra est bien configurée.

vault.yml
alors a quoi cela sert un mot de passe vault c'est quoi l'interet 
alors je l'ai creer et  j'ai vu que mon fichier vault.yml c'est transformer en chiffre sur presque 34 ligne 
et aide a corriger pour le ping 
user@192 TD-DEVOPS % cd todo-backend/ansible
user@192 ansible % ansible-vault encrypt group_vars/vault.yml
New Vault password: 
Confirm New Vault password: 
Encryption successful
user@192 ansible % ansible all -m ping --ask-vault-pass

Vault password: 
[ERROR]: Task failed: Failed to connect to the host via ssh: Connection timed out during banner exchange
Connection to UNKNOWN port 65535 timed out
Origin: <adhoc 'ping' task>

{'action': 'ping', 'args': {}, 'timeout': 0, 'async_val': 0, 'poll': 15}

10.0.2.132 | UNREACHABLE! => {
    "changed": false,
    "msg": "Task failed: Failed to connect to the host via ssh: Connection timed out during banner exchange\r\nConnection to UNKNOWN port 65535 timed out",
    "unreachable": true
}
[ERROR]: Task failed: Failed to connect to the host via ssh: ssh: connect to host 18.145.19.9 port 22: Operation timed out
Origin: <adhoc 'ping' task>

{'action': 'ping', 'args': {}, 'timeout': 0, 'async_val': 0, 'poll': 15}

18.145.19.9 | UNREACHABLE! => {
    "changed": false,
    "msg": "Task failed: Failed to connect to the host via ssh: ssh: connect to host 18.145.19.9 port 22: Operation timed out",
    "unreachable": true
}
10.0.2.227 | UNREACHABLE! => {
    "changed": false,
    "msg": "Task failed: Failed to connect to the host via ssh: Connection timed out during banner exchange\r\nConnection to UNKNOWN port 65535 timed out",
    "unreachable": true
}
Bash Check the admin_ip currently set in terraform.tfvars
IN
grep admin_ip /Users/user/Documents/Projet/TD-DEVOPS/todo-backend/terraform/terraform.tfvars 2>&1

OUT
admin_ip             = "102.164.185.1/32"
Le vault Ansible, à quoi ça sert
ansible-vault chiffre un fichier de variables (ici mysql_root_password) avec un algorithme symétrique (AES256), protégé par le mot de passe que tu viens de définir. Sans ce chiffrement, ce mot de passe circulerait en clair dans ton repo Git — n'importe qui avec accès au code (collaborateur, CI, fuite du repo) pourrait lire la vraie valeur. Une fois chiffré, le fichier ressemble à un blob illisible ($ANSIBLE_VAULT;1.1;AES256... sur ~34 lignes, exactement ce que tu as vu), et seul quelqu'un avec le mot de passe de vault peut le déchiffrer — que ce soit toi via --ask-vault-pass, ou Ansible lui-même au moment d'exécuter le playbook. C'est l'équivalent Ansible des "GitHub Secrets" qu'on a utilisés pour Docker Hub : les vraies valeurs ne doivent jamais apparaître en clair dans le code versionné.

Le ping qui timeout
Operation timed out sur le port 22 (pas "Connection refused") est la signature typique d'un Security Group AWS qui bloque silencieusement le trafic — pas un problème réseau générique. Ton front-sg n'autorise le SSH que depuis admin_ip (102.164.185.1/32 dans ton terraform.tfvars). Si ton IP publique a changé depuis que tu l'as configurée, c'est exactement ce blocage.

Vérifie ton IP actuelle dans ton terminal (pas moi, je n'ai pas d'accès réseau sortant depuis mon environnement) :


curl ifconfig.me
Compare avec 102.164.185.1. Si c'est différent :


cd /Users/user/Documents/Projet/TD-DEVOPS/todo-backend/terraform
Édite terraform.tfvars, remplace admin_ip par ta nouvelle IP /32, puis :


terraform apply
Ça ne fait qu'une mise à jour de la règle du security group (pas de recréation d'instance) — rapide.

Si l'IP est identique, dis-le-moi et on creusera un autre angle (VPN/pare-feu réseau local qui bloque le port 22 sortant, ou instance pas encore complètement prête).