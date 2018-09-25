provider "aws" {
  access_key = "AKIAIQRNUTDGGAUIKCWQ"
  secret_key = "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"
  region     = "${var.aws_region}"
}

data "aws_eip" "nat" {
  public_ip = "${var.public_ip}"
}

data "aws_caller_identity" "current" {}

# CONFIGURAÇÃO DE REDE
module "vpc" {
  source = "github.com/terraform-aws-modules/terraform-aws-vpc.git"

  name = "ac-${var.env}"

  cidr = "10.0.0.0/16"

  azs             = "${split(",",var.azs)}"
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  assign_generated_ipv6_cidr_block = true

  enable_nat_gateway = true
  single_nat_gateway = true
  reuse_nat_ips = true
  external_nat_ip_ids = ["${data.aws_eip.nat.*.id}"]

  public_subnet_tags = {
    Name = "public-sn-${var.env}"
  }
  
  private_subnet_tags = {
    Name = "private-sn-${var.env}"
  }

  tags = {
    Environment = "${var.env}"
  }

  vpc_tags = {
    Name = "vpc-${var.env}"
  }
}

# LOAD BALANCER

resource "aws_s3_bucket" "alb-logs-bucket" {
  bucket = "ac-alb-logs-bucket"
  policy = <<POLICY
{
  "Id": "Policy1357935677554",
  "Statement": [
    {
      "Sid": "Stmt1357935647218",
      "Action": [
        "s3:ListBucket"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::ac-alb-logs-bucket",
      "Principal": {
        "AWS": [
          "${data.aws_caller_identity.current.account_id}"
        ]
      }
    },
    {
      "Sid": "Stmt1357935676138",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::ac-alb-logs-bucket/*",
      "Principal": {
        "AWS": [
          "${data.aws_caller_identity.current.account_id}"
        ]
      }
    }
  ]
}

POLICY

  tags {
    Name        = "ALB Bucket"
    Environment = "${var.env}"
  }
}


#SECURITY GROUPS
module "sg" {
  source = "github.com/terraform-aws-modules/terraform-aws-security-group.git"

  name        = "acesso-ac-default_novo"
  description = "Acesso full a partir do escritorio 7 de Abril"
  vpc_id      = "${module.vpc.vpc_id}"
  use_name_prefix = false
  
   ingress_with_cidr_blocks  = [  
    {
      rule = "all-all",
      cidr_blocks = "177.68.74.38/32",
      }     
   ]   
   
   ingress_with_self = [
    {
      rule = "all-all",
      self        = true,
    }    
   ]   
  
  egress_cidr_blocks      = ["0.0.0.0/0"]
  egress_rules            = ["all-all"]  
}

variable "list_of_maps" {
   type = "list"
   default = [
      {
          name = "claro",
          backend_protocol = "HTTP",
          backend_port = "80"
      },
      {
          name = "net",
          backend_protocol = "HTTP",
          backend_port = "80"
      }
   ]
}


  module "alb" {
  source                        = "github.com/terraform-aws-modules/terraform-aws-alb.git"
  load_balancer_name            = "alb-ac"
  security_groups               = ["${module.sg.this_security_group_id}"]
  log_bucket_name               = "${aws_s3_bucket.alb-logs-bucket.id}"
  subnets                       = ["${module.vpc.public_subnets}"]
  tags                          = "${map("Environment", var.env)}"
  vpc_id                        = "${module.vpc.vpc_id}"
  https_listeners               = "${list(map("certificate_arn", "${var.arn_certificado_digital}", "port", 443))}"
  https_listeners_count         = "1" 
  target_groups                 = "${var.list_of_maps}"
  target_groups_count           = "${length(var.list_of_maps)}"
}


resource "aws_alb_listener_rule" "listener_rule" {
  depends_on   = ["module.alb"]  
  listener_arn = "${element(module.alb.https_listener_arns, 0)}"  
  priority     = "${count.index+1}"   
  count = "${length(module.alb.target_group_arns)}"
  action {    
    type             = "forward"    
    target_group_arn = "${element(module.alb.target_group_arns, count.index)}"  
  }   
  condition {    
    field  = "host-header"    
    values = ["${element(module.alb.target_group_names, count.index)}.${var.domain_name}"]
  }
}


# REGISTROS DNS
data "aws_route53_zone" "main" {
  name    = "${var.domain_name}"
}

resource "aws_route53_record" "all" {
  zone_id = "${data.aws_route53_zone.main.zone_id}"
  name    = "*.${var.domain_name}"
  type    = "A"

  alias {
    name                   = "${module.alb.dns_name}"
    zone_id                = "${module.alb.load_balancer_zone_id}"
    evaluate_target_health = true
  }
}

