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
                // Generate checkstyle HTML report via Maven site target
                sh 'mvn checkstyle:checkstyle site -DgenerateReports=false -Dcheckstyle.skip=false || true'
            }
        }

        stage('SpotBugs') {
            steps {
                // Force HTML report generation for Jenkins publishing
                sh 'mvn spotbugs:spotbugs -Dspotbugs.htmlOutput=true -Dspotbugs.outputDirectory=target -Dcheckstyle.skip=true || true'
            }
        }

        stage('Gitleaks Scan') {
            steps {
                // Generate BOTH JSON (for HTML rendering) and JUnit XML
                sh '''
                    gitleaks detect --source . --no-git --exit-code 0 --report-path target/gitleaks-report.json --report-format json --verbose || true
                    
                    # Convert JSON to a clean standalone HTML file
                    echo "<html><head><title>Gitleaks</title><style>body{font-family:sans-serif;padding:20px}table{width:100%;border-collapse:collapse}th,td{border:1px solid #ccc;padding:8px}th{background:#333;color:#fff}</style></head><body><h2>Gitleaks Report</h2>" > target/gitleaks-report.html
                    jq -r 'if length == 0 then "<p>No leaks detected.</p>" else "<table><tr><th>Rule</th><th>File</th><th>Line</th><th>Match</th></tr>" + (.[] | "<tr><td>\(.RuleID)</td><td>\(.File)</td><td>\(.StartLine)</td><td><code>\(.Match)</code></td></tr>") + "</table>" end' target/gitleaks-report.json >> target/gitleaks-report.html
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
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.json, target/*.html', allowEmptyArchive: true

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
                reportName: 'SpotBugs Report',
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
