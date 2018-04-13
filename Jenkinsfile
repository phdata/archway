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
                container('busybox') {
                    withAWS(credentials: 'jenkins-aws-user') {
                        sh '''
                           cd cloudera/parcel/
                           mkdir build
                           cp -R meta build/
                           mkdir -p /usr/lib/heimdali-api
                           mkdir -p /usr/lib/heimdali-ui
                        '''

                        s3Download file: 'build/usr/lib/heimdali-ui/', bucket: 'heimdali-repo', path: 'heimdali-ui/'
                        s3Download file: 'build/usr/lib/heimdali-api/', bucket: 'heimdali-repo', path: 'heimdali-api.jar/'

                        sh '''
                           cd cloudera/parcel/
                           mv build HEIMDALI-${VERSION}
                           tar cvf HEIMDALI-${VERSION}-el7.parcel HEIMDALI-${VERSION}
                        '''
                   }
                }
            }
        }
    }
    post {
        failure {
            slackSend color: "#a64f36", message: "Heimdali Parcel, <${env.BUILD_URL}|build #${BUILD_NUMBER}> Failed"
        }
        success {
            slackSend color: "#36a64f", message: "New Parcel Available @here: ${env.VERSION}"
        }
    }
}