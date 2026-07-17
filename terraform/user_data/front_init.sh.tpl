#!/bin/bash
# Bootstrap minimal pour l'instance Front (Amazon Linux 2023).
# Installe Docker au boot ; la configuration fine (Nginx, Certbot,
# déploiement du conteneur frontend) est ensuite prise en charge par Ansible.
set -eux

dnf update -y
dnf install -y docker

systemctl enable docker
systemctl start docker

usermod -aG docker ec2-user

echo "Front instance bootstrap terminé (projet: ${project_name})" > /var/log/todo-user-data.log
