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
                sh 'mvn checkstyle:checkstyle site -DgenerateReports=false -Dcheckstyle.failOnViolation=false || true'
            }
        }

        stage('SpotBugs') {
            steps {
                sh 'mvn test-compile spotbugs:spotbugs -Dspotbugs.htmlOutput=true -Dcheckstyle.skip=true || true'
            }
        }

        stage('Package Application') {
            steps {
                sh 'mvn package -DskipTests -Dcheckstyle.skip=true'
            }
        }

        stage('Start App & OWASP ZAP Scan') {
            steps {
                script {
                    // 1. Kill any existing instance running on port 8081
                    sh 'fuser -k 8081/tcp || true'

                    // 2. Start Spring Boot JAR in background mode
                    sh 'nohup java -jar target/*.jar --server.port=8081 > app.log 2>&1 &'
                    
                    // 3. Wait 15 seconds for Spring Boot startup
                    sleep 15

                    // 4. Run ZAP scan against the live app
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh '''
                            chmod 777 $(pwd)
                            docker run --rm \
                              --user root \
                              -w /zap/wrk \
                              --add-host=host.docker.internal:host-gateway \
                              -v $(pwd):/zap/wrk/:rw \
                              -t ghcr.io/zaproxy/zaproxy:stable \
                              zap-baseline.py \
                              -t http://host.docker.internal:8081 \
                              -r zap-report.html \
                              -I
                        '''
                    }
                }
            }
            post {
                always {
                    // 5. Terminate the background Spring Boot process after scanning
                    sh 'fuser -k 8081/tcp || true'

                    publishHTML([
                        allowMissing: false,
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
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.sarif, target/*.html, target/site/*.html, app.log', allowEmptyArchive: true

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
