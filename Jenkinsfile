pipeline {
    agent any

    stages {
        stage('Build App Image') {
            steps {
                // Build local Docker image using the Dockerfile
                sh 'docker build -t fundoo-app:latest .'
            }
        }

        stage('Run Container & OWASP ZAP Scan') {
            steps {
                script {
                    // Create a dedicated bridge network so containers communicate directly
                    sh 'docker network create zap-net || true'

                    // Start application container on the network
                    sh '''
                        docker run -d \
                          --name fundoo-container \
                          --network zap-net \
                          fundoo-app:latest
                    '''

                    // Healthcheck using Docker inspect / logs
                    sh '''
                        echo "Waiting for container to become healthy..."
                        sleep 10
                        docker logs fundoo-container
                    '''

                    // Run OWASP ZAP container referencing the application container name
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh '''
                            chmod 777 $(pwd)
                            docker run --rm \
                              --user root \
                              --network zap-net \
                              -v $(pwd):/zap/wrk/:rw \
                              ghcr.io/zaproxy/zaproxy:stable \
                              zap-baseline.py \
                              -t http://fundoo-container:8081 \
                              -r zap-report.html \
                              -I || true
                        '''
                    }
                }
            }
            post {
                always {
                    // Stop & remove test container and network
                    sh '''
                        docker stop fundoo-container || true
                        docker rm fundoo-container || true
                        docker network rm zap-net || true
                    '''

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
            archiveArtifacts artifacts: 'zap-report.html', allowEmptyArchive: true
        }
    }
}
