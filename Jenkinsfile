#!/usr/bin/env groovy

pipeline {
    agent { docker 'maven:3-alpine' }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.hpi', fingerprint: true
        }
    }
}
