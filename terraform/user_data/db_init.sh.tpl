#!/bin/bash
# Bootstrap minimal pour l'instance DB (Amazon Linux 2023).
# Installe Docker au boot ; le déploiement réel du conteneur MySQL
# (base ${mysql_database}) est ensuite pris en charge par Ansible.
set -eux

dnf update -y
dnf install -y docker

systemctl enable docker
systemctl start docker

usermod -aG docker ec2-user

echo "DB instance bootstrap terminé (database cible: ${mysql_database})" > /var/log/todo-user-data.log
