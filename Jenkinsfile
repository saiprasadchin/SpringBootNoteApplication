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

        stage('OWASP Dependency Check') {
            steps {
                sh '''
                    ./gradlew dependencyCheckAnalyze
                '''
                // For Maven, replace with: ./mvnw org.owasp:dependency-check-maven:check
            }
            post {
                always {
                    // Publishes the HTML report in Jenkins build artifact view
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports', // Maven path: 'target'
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Report',
                        reportTitles: 'OWASP Security Analysis'
                    ])
                }
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
            archiveArtifacts artifacts: 'target/*.jar, target/*.xml, target/*.sarif, target/*.html, target/site/*.html', allowEmptyArchive: true

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
