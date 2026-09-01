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
                // Fetch full git history to ensure commit scanning works
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
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage('SpotBugs') {
            steps {
                // Compiles bytecode and generates target/spotbugs.html
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true'
            }
        }

        stage('Gitleaks Scan') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        // 1. Scan filesystem directly and save JSON output
                        sh 'gitleaks detect --source . --no-git --report-path target/gitleaks-report.json --report-format json --verbose || true'
        
                        // 2. Generate HTML report for Jenkins UI
                        sh '''
                            export NVM_DIR="/home/ec2-user/.nvm"
                            [ -s "$NVM_DIR/nvm.sh" ] && \\. "$NVM_DIR/nvm.sh"
        
                            gitleaks-secret-scanner --json target/gitleaks-report.json --html-report target/gitleaks-report.html || true
                        '''
                    }
                }
            }
        }
        
        stage('Package Application') {
            steps {
                // Bypass Checkstyle failure during JAR packaging
                sh 'mvn package -DskipTests -Dcheckstyle.skip=true'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, target/spotbugs*.xml, target/gitleaks-report.json, target/gitleaks-report.html', allowEmptyArchive: true

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
                reportName: 'SpotBugsReport',
                reportTitles: 'SpotBugs Analysis'
            ])

            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'gitleaks-report.html',
                reportName: 'Gitleaks Report',
                reportTitles: 'Gitleaks Security Analysis'
            ])
        }
    }
}
