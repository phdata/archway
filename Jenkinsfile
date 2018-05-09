pipeline {
    agent {
        kubernetes {
            cloud "kubernetes"
            label "csd"
            containerTemplate {
                name 'jdk'
                image 'jdk:8'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    environment {
        VERSION = VersionNumber({
            versionNumberString: '${BUILD_DATE_FORMATTED, "yyyy.MM"}.${BUILDS_THIS_MONTH, XX}'
        })
    }
    stages {
        stage('build') {
            steps {
                container('jdk') {
                    withAWS(credentials: 'jenkins-aws-user') {
                        sh '''
                           sed -i "s/0.1.0/${VERSION}/g" descriptor/service.sdl
                           jar cvf HEIMDALI-${VERSION}.jar `find . -mindepth 1 -not -path "./.git*"`
                        '''

                        s3Upload file: "HEIMDALI-${env.VERSION}-el7.parcel", bucket: 'heimdali-repo', path: 'csd/'
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