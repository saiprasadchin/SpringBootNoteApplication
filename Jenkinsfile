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
                // Generates report and allows pipeline to proceed even if violations exist
                sh 'mvn checkstyle:checkstyle site -DgenerateReports=false -Dcheckstyle.failOnViolation=false || true'
            }
        }

        stage('SpotBugs') {
            steps {
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true -Dcheckstyle.skip=true || true'
            }
        }

        stage('Gitleaks Scan') {
            steps {
                // Generate native SARIF format (Standard static analysis interchange format)
                sh 'gitleaks detect --source . --no-git --exit-code 0 --report-path target/gitleaks-report.sarif --report-format sarif --verbose || true'
            }
            post {
                always {
                    // Record findings using Warnings Next Generation Plugin (No script required)
                    recordIssues(
                        enabledForFailure: true,
                        tool: sarif(pattern: 'target/gitleaks-report.sarif', id: 'gitleaks', name: 'Gitleaks Security Analysis')
                    )
                }
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
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.sarif, target/*.html, target/site/*.html', allowEmptyArchive: true

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
                reportFiles: 'spotbugs.html',
                reportName: 'SpotBugs Report',
                reportTitles: 'SpotBugs Analysis'
            ])
        }
    }
}
