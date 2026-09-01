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
        stage('Checkout Code') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: scm.branches,
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'CloneOption', depth: 0, noTags: false, shallow: false]],
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }
        
        stage('Compile Project') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Checkstyle') {
            steps {
                // Generates target/site/checkstyle.html
                sh 'mvn checkstyle:checkstyle site -DgenerateReports=false || true'
            }
        }

        stage('SpotBugs') {
            steps {
                // Generates target/spotbugs.html
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true -Dcheckstyle.skip=true || true'
            }
        }

        stage('Gitleaks Scan') {
            steps {
                // Option A: Keep JUnit format (shows under Jenkins Native "Test Result" tab)
                sh 'gitleaks detect --source . --no-git --exit-code 0 --report-path target/gitleaks-report.xml --report-format junit --verbose || true'
            }
        }
        
        stage('Package Application') {
            steps {
                sh 'mvn package -DskipTests -Dcheckstyle.skip=true'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.html, target/site/*.html', allowEmptyArchive: true

            // 1. Checkstyle HTML Tab
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'checkstyle.html',
                reportName: 'Checkstyle Report',
                reportTitles: 'Checkstyle Analysis'
            ])

            // 2. SpotBugs HTML Tab (Fixed filename target to spotbugs.html)
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'spotbugs.html',
                reportName: 'SpotBugs Report',
                reportTitles: 'SpotBugs Analysis'
            ])

            // 3. Gitleaks Security Tab (Parsed natively via JUnit)
            junit allowEmptyResults: true, testResults: 'target/gitleaks-report.xml'
        }
    }
}
