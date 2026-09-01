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
        stage('Package Application') {
            steps {
                sh 'mvn clean package -DskipTests -Dcheckstyle.skip=true'
            }
        }

        stage('Start App & OWASP ZAP Scan') {
            steps {
                script {
                    // 1. Clear port 8081
                    sh 'fuser -k 8081/tcp || true'

                    // 2. Launch Spring Boot application in background with H2 in-memory DB
                    sh '''
                        BUILD_ID=dontKillMe nohup java -jar target/fundoo-0.0.1-SNAPSHOT.jar \
                          --server.port=8081 \
                          --spring.datasource.url=jdbc:h2:mem:testdb \
                          --spring.datasource.driver-class-name=org.h2.Driver \
                          --spring.jpa.database-platform=org.hibernate.dialect.H2Dialect > app.log 2>&1 &
                    '''

                    // 3. Health check loop (30-second timeout)
                    sh '''
                        echo "Waiting for Spring Boot app to start on port 8081..."
                        timeout 30 bash -c 'until curl -s http://127.0.0.1:8081 > /dev/null; do sleep 2; done' || {
                            echo "App failed to start! Printing app.log:"
                            cat app.log
                            exit 1
                        }
                    '''

                    // 4. Execute OWASP ZAP Baseline Scan
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh '''
                            chmod 777 $(pwd)
                            docker run --rm \
                              --user root \
                              --network="host" \
                              -v $(pwd):/zap/wrk/:rw \
                              ghcr.io/zaproxy/zaproxy:stable \
                              zap-baseline.py \
                              -t http://127.0.0.1:8081 \
                              -r zap-report.html \
                              -I || true
                        '''
                    }
                }
            }
            post {
                always {
                    // 5. Cleanup running process
                    sh 'fuser -k 8081/tcp || true'

                    publishHTML([
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
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, app.log, zap-report.html', allowEmptyArchive: true
        }
    }
}
