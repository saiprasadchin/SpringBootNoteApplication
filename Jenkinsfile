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

        stage('Gitleaks Secret Scan') {
            steps {
                // 1. Run Gitleaks scan to generate JSON report
                sh 'gitleaks detect --source . --report-path target/gitleaks-report.json --report-format json --verbose || true'
        
                // 2. Convert JSON report into HTML UI using npx
                sh '''
                    export NVM_DIR="/home/ec2-user/.nvm"
                    [ -s "$NVM_DIR/nvm.sh" ] && \\. "$NVM_DIR/nvm.sh"
                    npx --yes gitleaks2html -i target/gitleaks-report.json -o target/gitleaks-report.html || true
                '''
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
