pipeline {
    agent {
        kubernetes {
            cloud "kubernetes"
            label "parcels"
            containerTemplate {
                name 'busybox'
                image 'busybox'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    environment {
        VERSION = VersionNumber({
            versionNumberString: '${BUILD_DATE_FORMATTED, "yyyy.MM"}.${BUILDS_THIS_MONTH}'
        })
    }
    stages {
        stage('build') {
            steps {
                container('node') {
                    withAWS(credentials: 'jenkins-aws-user') {
                        sh '''
                           mkdir build
                           cp -R meta build/
                           mkdir -p /usr/lib/heimdali-api
                           mkdir -p /usr/lib/heimdali-ui
                        '''

                        s3Download file: 'build/usr/lib/heimdali-ui/', bucket: 'heimdali-repo', path: 'heimdali-ui/'
                        s3Download file: 'build/usr/lib/heimdali-api/', bucket: 'heimdali-repo', path: 'heimdali-api.jar/'

                        sh '''
                           mv build HEIMDALI-${VERSION}
                           tar cvf HEIMDALI-${VERSION}-el7.parcel HEIMDALI-${VERSION}
                        '''
                   }
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