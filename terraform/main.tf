# =========================================================
# Infrastructure AWS - Todo App (Front / Back / DB)
# 1 VPC, 1 subnet public (Front), 1 subnet privé (Back+DB)
# 3 EC2 t2.micro, 3 Security Groups dédiés, 1 Key Pair SSH
# =========================================================

# --- AMI Amazon Linux 2023 (dernière version officielle) ---
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# --- VPC ---
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

# --- Internet Gateway ---
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-igw"
  }
}

# --- Subnet public (Front) ---
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-subnet"
  }
}

# --- Subnet privé (Back + DB) ---
resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidr
  availability_zone = var.availability_zone

  tags = {
    Name = "${var.project_name}-private-subnet"
  }
}

# --- Route table publique -> IGW ---
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# =========================================================
# Security Groups
# =========================================================

resource "aws_security_group" "front_sg" {
  name        = "${var.project_name}-front-sg"
  description = "SG pour l'instance Front (HTTP/HTTPS public + SSH admin)"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH admin"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-front-sg"
  }
}

resource "aws_security_group" "back_sg" {
  name        = "${var.project_name}-back-sg"
  description = "SG pour l'instance Back (API accessible uniquement depuis Front)"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "API depuis Front uniquement"
    from_port       = var.app_port
    to_port         = var.app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.front_sg.id]
  }

  ingress {
    description = "SSH admin"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-back-sg"
  }
}

resource "aws_security_group" "db_sg" {
  name        = "${var.project_name}-db-sg"
  description = "SG pour l'instance DB (MySQL accessible uniquement depuis Back)"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MySQL depuis Back uniquement"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.back_sg.id]
  }

  ingress {
    description = "SSH admin"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-db-sg"
  }
}

# =========================================================
# Key Pair SSH
# =========================================================

resource "aws_key_pair" "admin" {
  key_name   = var.key_pair_name
  public_key = file(var.public_key_path)
}

# =========================================================
# EC2 Instances
# =========================================================

resource "aws_instance" "front" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.front_sg.id]
  key_name                    = aws_key_pair.admin.key_name
  associate_public_ip_address = true

  user_data = templatefile("${path.module}/user_data/front_init.sh.tpl", {
    project_name = var.project_name
  })

  tags = {
    Name = "${var.project_name}-front"
  }
}

resource "aws_instance" "back" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.private.id
  vpc_security_group_ids = [aws_security_group.back_sg.id]
  key_name               = aws_key_pair.admin.key_name

  user_data = templatefile("${path.module}/user_data/back_init.sh.tpl", {
    backend_image = var.backend_image
  })

  tags = {
    Name = "${var.project_name}-back"
  }
}

resource "aws_instance" "db" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.private.id
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  key_name               = aws_key_pair.admin.key_name

  user_data = templatefile("${path.module}/user_data/db_init.sh.tpl", {
    mysql_database = var.mysql_database
  })

  tags = {
    Name = "${var.project_name}-db"
  }
}
