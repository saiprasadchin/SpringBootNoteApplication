pipeline {
    agent any

    tools {
        jdk 'Java-11'
        maven 'Maven-3'
    }

    environment {
        MAVEN_OPTS = '-Xmx512m'
    }

    stages {
        stage('Compile Project') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Checkstyle') {
            steps {
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage('SpotBugs') {
            steps {
                // Compiles bytecode and generates target/spotbugsXml.html
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true'
            }
        }

        stage('Package Application') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, target/spotbugsXml.xml', allowEmptyArchive: true

            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'checkstyle.html',
                reportName: 'Checkstyle Report',
                reportTitles: 'Checkstyle Analysis'
            ])

            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'spotbugsXml.html',
                reportName: 'SpotBugsReport',
                reportTitles: 'SpotBugs Analysis'
            ])
        }
    }
}
