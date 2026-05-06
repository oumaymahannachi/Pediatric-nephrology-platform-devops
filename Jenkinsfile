pipeline {
    agent any
    
    environment {
        REGISTRY = "oumaymahannachi" 
        SONAR_URL = "http://pedialink-sonarqube:9000"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Cloning repository..."
                checkout scm
            }
        }

        stage('Backend Build') {
            agent {
                docker { 
                    image 'maven:3.9.6-eclipse-temurin-17-alpine'
                    args '-v /root/.m2:/root/.m2' // Cache Maven
                }
            }
            steps {
                dir('backend') {
                    echo "Building Java Microservices..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Frontend Build') {
            agent {
                docker { 
                    image 'node:20-alpine'
                }
            }
            steps {
                dir('frontend') {
                    echo "Building Angular App..."
                    sh 'npm install'
                    sh 'npm run build -- --configuration production'
                }
            }
        }

        stage('SonarQube Scan') {
            agent {
                docker { image 'maven:3.9.6-eclipse-temurin-17-alpine' }
            }
            steps {
                dir('backend') {
                    echo "Running SonarQube Analysis..."
                    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                        sh "mvn sonar:sonar \
                            -Dsonar.host.url=${SONAR_URL} \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.projectKey=pedialink-backend"
                    }
                }
            }
        }

        stage('Docker - Build & Push') {
            steps {
                script {
                    def images = [
                        'auth-service': 'backend/auth-service',
                        'api-gateway': 'backend/api-gateway',
                        'dossiermedical-service': 'backend/dossiermedical-service',
                        'eureka-server': 'backend/eureka-server',
                        'pedialink-frontend': 'frontend'
                    ]
                    
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        
                        images.each { name, path ->
                            echo "Processing ${name}..."
                            dir(path) {
                                sh "docker build -t ${REGISTRY}/${name}:latest ."
                                sh "docker push ${REGISTRY}/${name}:latest"
                            }
                        }
                    }
                }
            }
        }

        stage('Kubernetes - Deploy') {
            steps {
                echo "Deploying to Kubernetes Cluster (KubeAdm)..."
                sh "kubectl apply -f k8s/infrastructure/core-infra.yml"
                sh "kubectl apply -f k8s/backend/auth-deployment.yml"
                sh "kubectl apply -f k8s/backend/gateway-deployment.yml"
                sh "kubectl apply -f k8s/backend/dossiermedical-deployment.yml"
                sh "kubectl apply -f k8s/frontend/frontend-deployment.yml"
            }
        }
    }

    post {
        success {
            echo "Sprint 3: Pipeline executed successfully!"
        }
        failure {
            echo "Sprint 3: Pipeline failed. Check logs."
        }
    }
}
