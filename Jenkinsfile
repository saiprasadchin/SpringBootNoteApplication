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
        stage('Debug ZAP Scan') {
            steps {
                script {
                    sh '''
                        echo "=== 1. SYSTEM & ENVIRONMENT INFO ==="
                        pwd
                        whoami
                        docker --version
                        
                        echo "=== 2. CHECK IF PORT 8081 IS RESPONDING ==="
                        curl -Iv http://host.docker.internal:8081 || echo "CRITICAL: Cannot connect to port 8081 from host!"

                        echo "=== 3. PREPARE WORKSPACE PERMISSIONS ==="
                        chmod 777 $(pwd)
                        touch $(pwd)/zap-report.html
                        chmod 666 $(pwd)/zap-report.html

                        echo "=== 4. RUNNING OWASP ZAP SCAN ==="
                        docker run --rm \
                          --user root \
                          -w /zap/wrk \
                          --add-host=host.docker.internal:host-gateway \
                          -v $(pwd):/zap/wrk/:rw \
                          ghcr.io/zaproxy/zaproxy:stable \
                          zap-baseline.py \
                          -t http://host.docker.internal:8081 \
                          -r zap-report.html \
                          -I || echo "ZAP scan finished with non-zero exit code (Expected if issues found)"

                        echo "=== 5. VERIFY REPORT CREATION ==="
                        ls -la $(pwd)/zap-report.html
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'zap-report.html, app.log', allowEmptyArchive: true

            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: '.',
                reportFiles: 'zap-report.html',
                reportName: 'OWASP ZAP Security Report',
                reportTitles: 'ZAP DAST Baseline Analysis'
            ])
        }
    }
}
