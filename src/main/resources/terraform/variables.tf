variable "aws_region" {
  description = "The AWS region to create things in."
  default     = "sa-east-1"
}

variable "azs" {
  description = "The AWS region to create things in."
  default     = "sa-east-1a,sa-east-1c"
}

variable "public_ip" {
  description = "The AWS region to create things in."
  default     = "18.228.34.1"
}

variable "env" {
  description = "The AWS region to create things in."
  default     = "des"
}

variable "arn_certificado_digital" {
  description = "ARN do certificado digital."
  default     = "arn:aws:acm:sa-east-1:256514981454:certificate/fe69598a-122d-4c56-8cc1-0d0700eaf938"
}

variable "domain_name" {
  description = "dominio"
  default     = "acordocerto.de"
}
