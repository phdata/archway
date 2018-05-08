pipeline {
    agent {
        kubernetes {
            cloud "kubernetes"
            label "heimdali"
            containerTemplate {
                name 'jdk'
                image 'java:8-jdk'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    parameters {
        string(name: 'sbt_params', defaultValue: '-sbt-dir /sbt/.sbt -ivy /sbt/.ivy')
    }
    stages {
        stage('setup') {
            steps {
                container("mysql") {
                    sh "mysql --password=my-secret-pw < ./src/main/resources/db/migration/V1__Create.sql"
                }
            }
        }
        stage('package') {
            steps {
                container("jdk") {
                    sh "./sbt ${params.sbt_params} assembly"
                }
            }
        }
        stage('publish') {
            when {
                branch "master"
            }
            steps {
                container("jdk") {
                    withAWS(credentials: 'jenkins-aws-user') {
                        s3Upload file: 'target/scala-2.12/heimdali-api.jar', bucket: 'heimdali-repo', path: 'heimdali-api.jar'
                    }
                }
            }
        }
    }
    post {
        always {
            junit 'target/test-reports/*.xml'
        }
        failure {
            slackSend color: "#a64f36", message: "Heimdali API, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Failed"
        }
        success {
            slackSend color: "#36a64f", message: "Heimdali API, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Succeeded"
        }
    }
}