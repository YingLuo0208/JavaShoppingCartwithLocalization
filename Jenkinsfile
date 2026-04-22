pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('Docker_Hub')
        IMAGE_NAME = "luoying0208/shopping-cart"
        IMAGE_TAG  = "latest"
        SONAR_TOKEN = 'sqa_1b424562c478380dbd72ee537255aba15b810f75'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test & Package') {
            steps {
                bat 'mvn --batch-mode clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat 'mvn sonar:sonar -Dsonar.token=%SONAR_TOKEN% -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t %IMAGE_NAME%:%IMAGE_TAG% ."
            }
        }

        stage('Push to Docker Hub') {
            steps {
                bat "echo %DOCKERHUB_CREDENTIALS_PSW% | docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin"
                bat "docker push %IMAGE_NAME%:%IMAGE_TAG%"
            }
        }
    }

    post {
        always {
            node(null) {
                bat 'docker logout'
            }
        }
    }
}