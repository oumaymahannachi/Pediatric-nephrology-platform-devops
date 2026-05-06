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
            steps {
                script {
                    docker.image('maven:3.9.6-eclipse-temurin-17-alpine').inside('-v /root/.m2:/root/.m2 --network pedialink-net') {
                        dir('backend') {
                            echo "Running Unit Tests..."
                            sh 'mvn test'
                        }
                    }
                }
            }
        }

        stage('Backend Build') {
            steps {
                script {
                    docker.image('maven:3.9.6-eclipse-temurin-17-alpine').inside('-v /root/.m2:/root/.m2') {
                        dir('backend') {
                            echo "Building Java Microservices (Artifact Generation)..."
                            sh 'mvn package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Frontend Build') {
            steps {
                script {
                    docker.image('node:20-alpine').inside() {
                        dir('frontend') {
                            echo "Building Angular App..."
                            sh 'npm install'
                            sh 'npm run build -- --configuration production'
                        }
                    }
                }
            }
        }

        stage('SonarQube Scan') {
            steps {
                script {
                    docker.image('maven:3.9.6-eclipse-temurin-17-alpine').inside('-v /root/.m2:/root/.m2 --network pedialink-net') {
                        dir('backend') {
                            echo "Running SonarQube Analysis..."
                            withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                                sh "mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN} -Dsonar.projectKey=pedialink-backend"
                            }
                        }
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
                                // S'assurer que le dossier target existe avant de builder
                                sh "ls -la" 
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
                echo "Deploying to Kubernetes..."
                dir('k8s') {
                    sh "kubectl apply -f infrastructure/core-infra.yml --validate=false"
                    sh "kubectl apply -f backend/ --validate=false"
                    sh "kubectl apply -f frontend/ --validate=false"
                }
            }
        }

        stage('Monitoring') {
            steps {
                echo "Deploying Monitoring Stack..."
                dir('k8s/monitoring') {
                    sh "kubectl apply -f monitoring-stack.yml --validate=false"
                }
            }
        }
    }

    post {
        success {
            echo "Sprint 3: Pipeline completed successfully!"
        }
        failure {
            echo "Sprint 3: Pipeline failed. Check logs."
        }
    }
}
