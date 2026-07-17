#!/bin/bash
# Bootstrap minimal pour l'instance Back (Amazon Linux 2023).
# Installe Docker au boot ; le déploiement réel du conteneur backend
# (image ${backend_image}) est ensuite pris en charge par Ansible / CI-CD.
set -eux

dnf update -y
dnf install -y docker

systemctl enable docker
systemctl start docker

usermod -aG docker ec2-user

echo "Back instance bootstrap terminé (image cible: ${backend_image})" > /var/log/todo-user-data.log
