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
                    sh '''
                       if [ -d /cache/node_modules ]; then
                          mv /cache/node_modules node_modules
                       fi
                    '''
                    sh "npm install"
                }
            }
        }
        stage('package') {
            steps {
                container('node') {
                    sh "npm run-script build"
                    withAWS(credentials: 'jenkins-aws-user') {
                        s3Upload file: 'build', bucket: 'heimdali-repo', path: 'heimdali-ui/'
                    }
                }
            }
        }
    }
    post {
        always {
            container('node') {
                sh 'mv node_modules /cache/node_modules'
            }
        }
        failure {
            slackSend color: "#a64f36", message: "Heimdali UI, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Failed"
        }
        success {
            slackSend color: "#36a64f", message: "Heimdali UI, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Succeeded"
        }
    }
}