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
                // Perform full checkout without shallow clone depth restriction
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
                // Compiles bytecode and generates target/spotbugsXml.html
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true'
            }
        }

        stage('Gitleaks Secret Scan') {
            steps {
                // Runs scan using full history and workspace files
                sh '''
                    export NVM_DIR="/home/ec2-user/.nvm"
                    [ -s "$NVM_DIR/nvm.sh" ] && \\. "$NVM_DIR/nvm.sh"

                    gitleaks-secret-scanner --diff-mode all --html-report target/gitleaks-report.html || true
                '''
                
                // Print check to verify target/gitleaks-report.html creation
                sh 'ls -la target/gitleaks-report.html'
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
            archiveArtifacts artifacts: 'target/*.jar, target/spotbugsXml.xml, target/gitleaks-report.json', allowEmptyArchive: true

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
