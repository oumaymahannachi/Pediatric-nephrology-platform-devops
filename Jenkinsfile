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

        stage('Backend Test') {
            agent {
                docker { 
                    image 'maven:3.9.6-eclipse-temurin-17-alpine'
                    args '-v /root/.m2:/root/.m2 --network pedialink-net'
                }
            }
            steps {
                dir('backend') {
                    echo "Running Unit Tests..."
                    sh 'mvn test'
                }
            }
        }

        stage('Backend Build') {
            agent {
                docker { 
                    image 'maven:3.9.6-eclipse-temurin-17-alpine'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                dir('backend') {
                    echo "Building Java Microservices (Artifact Generation)..."
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
                docker { 
                    image 'maven:3.9.6-eclipse-temurin-17-alpine'
                    args '-v /root/.m2:/root/.m2 --network pedialink-net'
                }
            }
            steps {
                dir('backend') {
                    echo "Running SonarQube Analysis..."
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh "mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN} -Dsonar.projectKey=pedialink-backend"
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
                        'config-server': 'backend/config-server',
                        'prescription-service': 'backend/prescription-service',
                        'treatment-service': 'backend/treatment-service',
                        'treatment-monitoring-service': 'backend/treatment-monitoring-service',
                        'dossiermedical-service': 'backend/dossiermedical-service',
                        'eureka-server': 'backend/eureka-server',
                        'lab-results-service': 'backend/lab-results-service',
                        'messaging-service': 'backend/messaging-service',
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
