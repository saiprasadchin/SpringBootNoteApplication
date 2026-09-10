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
                // Generates report and allows pipeline to proceed even if violations exist
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

                    // publishHTML([
                    //     allowMissing: true,
                    //     alwaysLinkToLastBuild: true,
                    //     keepAll: true,
                    //     reportDir: '.',
                    //     reportFiles: 'zap-report.html',
                    //     reportName: 'OWASP ZAP Security Report',
                    //     reportTitles: 'ZAP DAST Baseline Analysis'
                    // ])
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.sarif, target/*.html, target/site/*.html, zap-report.html', allowEmptyArchive: true

            // Publish Checkstyle Report
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'checkstyle.html',
                reportName: 'Checkstyle Report',
                reportTitles: 'Checkstyle Analysis'
            ])

            // Publish SpotBugs Report
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'spotbugs.html',
                reportName: 'SpotBugs Report',
                reportTitles: 'SpotBugs Analysis'
            ])

            // Publish OWASP ZAP Security Report
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
