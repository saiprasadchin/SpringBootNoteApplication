pipeline {
    agent any

    tools {
        // Matches the tool names configured in Manage Jenkins > Tools
        jdk 'Java-11'
        maven 'Maven-3'
    }

    environment {
        // Restricts JVM heap usage to prevent crashes on t3.small instances
        MAVEN_OPTS = '-Xmx512m'
    }

    stages {
        stage('Compile Project') {
            steps {
                // Compiles your Spring Boot 2.6.3 source code
                sh 'mvn clean compile'
            }
        }

        stage('Run Unit Tests') {
            steps {
                // Executes application tests
                sh 'mvn test'
            }
        }

        stage('Package Application') {
            steps {
                // Generates the final fundoo-0.0.1-SNAPSHOT.jar file
                sh 'mvn package -DskipTests'
            }
        }
    }

    post {
        always {
            // Saves the build artifact inside Jenkins history
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
    }
}
