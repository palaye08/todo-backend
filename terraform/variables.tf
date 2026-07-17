variable "aws_region" {
  description = "Région AWS où déployer l'infrastructure"
  type        = string
  default     = "eu-west-3"
}

variable "vpc_cidr" {
  description = "CIDR block du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR du sous-réseau public (Front)"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR du sous-réseau privé (Back + DB)"
  type        = string
  default     = "10.0.2.0/24"
}

variable "availability_zone" {
  description = "Zone de disponibilité utilisée pour les sous-réseaux"
  type        = string
  default     = "eu-west-3a"
}

variable "instance_type" {
  description = "Type d'instance EC2 utilisé pour les 3 machines"
  type        = string
  default     = "t2.micro"
}

variable "admin_ip" {
  description = "IP publique de l'administrateur autorisée en SSH (format CIDR, ex: 1.2.3.4/32)"
  type        = string
}

variable "key_pair_name" {
  description = "Nom de la paire de clés SSH créée sur AWS"
  type        = string
  default     = "todo-key"
}

variable "public_key_path" {
  description = "Chemin local vers la clé publique SSH à importer"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "mysql_root_password" {
  description = "Mot de passe root MySQL (à ne jamais committer en clair sur un repo réel)"
  type        = string
  sensitive   = true
}

variable "mysql_database" {
  description = "Nom de la base de données MySQL"
  type        = string
  default     = "todo_db"
}

variable "backend_image" {
  description = "Image Docker Hub du backend"
  type        = string
  default     = "palaye769/todo-backend:latest"
}

variable "frontend_image" {
  description = "Image Docker Hub du frontend"
  type        = string
  default     = "palaye769/todo-frontend:latest"
}

variable "app_port" {
  description = "Port exposé par le backend"
  type        = number
  default     = 8080
}

variable "front_domain" {
  description = "Nom de domaine (ou hostname local) utilisé par Nginx sur le Front"
  type        = string
  default     = "m1.local"
}

variable "project_name" {
  description = "Préfixe utilisé pour nommer les ressources AWS"
  type        = string
  default     = "todo"
}
