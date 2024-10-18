pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-2'
        ECR_REPOSITORY = 'trms/form'
        IMAGE_TAG = "${GIT_COMMIT}" // Using the commit hash as the image tag
        KUBE_CONFIG = credentials('your-kube-config-credential-id') // Jenkins credentials ID for Kube config
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image
                    sh "docker build -t ${ECR_REPOSITORY}:${IMAGE_TAG} ."
                }
            }
        }
        stage('Login to ECR') {
            steps {
                script {
                    // Login to ECR
                    sh """
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    """
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    // Push the Docker image to ECR
                    sh "docker push ${ECR_REPOSITORY}:${IMAGE_TAG}"
                }
            }
        }
        stage('Deploy to EKS') {
            steps {
                script {
                    // Deploy to EKS using kubectl
                    sh """
                    kubectl --kubeconfig=${KUBE_CONFIG} set image deployment/your-deployment-name your-container-name=${ECR_REPOSITORY}:${IMAGE_TAG}
                    kubectl --kubeconfig=${KUBE_CONFIG} rollout status deployment/your-deployment-name
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment succeeded!'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}
