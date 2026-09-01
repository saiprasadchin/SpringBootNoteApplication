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
                // Outputs JSON and converts it to target/gitleaks-report.html for dedicated tab rendering
                sh '''
                    gitleaks detect --source . --no-git --exit-code 0 --report-path target/gitleaks-report.json --report-format json --verbose || true
                    
                    echo "<html><head><title>Gitleaks Security Report</title><style>body{font-family:sans-serif;padding:20px}table{width:100%;border-collapse:collapse;margin-top:15px}th,td{border:1px solid #ddd;padding:10px;text-align:left}th{background:#232f3e;color:#fff}tr:nth-child(even){background-color:#f9f9f9}code{background:#f4f4f4;padding:2px 4px;border-radius:4px;color:#d9534f}</style></head><body><h2>Gitleaks Security Analysis</h2>" > target/gitleaks-report.html
                    jq -r 'if length == 0 then "<p style=\"color:green;font-weight:bold;\">No secrets or sensitive leaks detected!</p>" else "<table><tr><th>Rule ID</th><th>File Path</th><th>Line Number</th><th>Exposed Secret / Match</th></tr>" + (.[] | "<tr><td>\(.RuleID)</td><td>\(.File)</td><td>\(.StartLine)</td><td><code>\(.Match)</code></td></tr>") + "</table>" end' target/gitleaks-report.json >> target/gitleaks-report.html
                    echo "</body></html>" >> target/gitleaks-report.html
                '''
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
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.json, target/*.html, target/site/*.html', allowEmptyArchive: true

            // Tab 1: Checkstyle Report
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'checkstyle.html',
                reportName: 'Checkstyle Report',
                reportTitles: 'Checkstyle Analysis'
            ])

            // Tab 2: SpotBugs Report
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'spotbugs.html',
                reportName: 'SpotBugs Report',
                reportTitles: 'SpotBugs Analysis'
            ])

            // Tab 3: Dedicated Gitleaks Report
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
