pipeline {
    agent {
        kubernetes {
            cloud "kubernetes"
            label "heimdali-ui"
            containerTemplate {
                name 'node'
                image 'node'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    stages {
        stage('build') {
            steps {
                container('node') {
                    sh "npm install"
                }
            }
        }
        stage('package') {
            steps {
                container('node') {
                    sh "npm run-script build"
                    withAWS(credentials: 'jenkins-aws-user') {
                        s3Upload file: 'build', bucket: 'heimdali-repo', path: '/'
                    }
                }
            }
        }
    }
    post {
        failure {
            slackSend color: "#a64f36", message: "Heimdali UI, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Failed"
        }
        success {
            slackSend color: "#36a64f", message: "Heimdali UI, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Succeeded"
        }
    }
}