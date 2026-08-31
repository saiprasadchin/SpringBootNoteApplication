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
                // Generates the report at target/site/checkstyle.html
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage('SpotBugs') {
            steps {
                // Inspects bytecode for Java bugs and generates HTML report at target/spotbugsXml.html
                sh 'mvn spotbugs:spotbugs'
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
            // Archives build artifacts
            archiveArtifacts artifacts: 'target/*.jar, target/spotbugsXml.xml', allowEmptyArchive: true

            // Publishes the HTML report in Jenkins UI
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
                reportName: 'SpotBugs_Report',
                reportTitles: 'SpotBugs_Analysis'
            ])
        }
    }
}
