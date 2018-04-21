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
            versionNumberString: '${BUILD_DATE_FORMATTED, "yyyy.MM"}.${BUILDS_THIS_MONTH, XX}'
        })
    }
    stages {
        stage('build') {
            steps {
                container('busybox') {
                    withAWS(credentials: 'jenkins-aws-user') {
                        sh '''
                           mkdir cloudera/parcel/build
                           cp -R cloudera/parcel/meta cloudera/parcel/build/
                           sed -i "s/0.1.5/${VERSION}/g" cloudera/parcel/build/meta/parcel.json
                           mkdir -p cloudera/parcel/build/usr/lib/heimdali-api
                           chown -R 10000:10000 cloudera/parcel/build
                        '''

                        s3Download file: 'cloudera/parcel/build/usr/lib/', force: true, bucket: 'heimdali-repo', path: 'heimdali-ui/'

                        s3Download file: 'cloudera/parcel/build/usr/lib/heimdali-api/heimdali-api.jar', bucket: 'heimdali-repo', path: 'heimdali-api.jar'

                        sh '''
                           mv cloudera/parcel/build cloudera/parcel/HEIMDALI-${VERSION}
                           cd cloudera/parcel/ && tar cvf HEIMDALI-${VERSION}-el7.parcel HEIMDALI-${VERSION}
                        '''

                        s3Upload file: "cloudera/parcel/HEIMDALI-${env.VERSION}-el7.parcel", bucket: 'heimdali-repo', path: 'parcels/'
                   }
                }
            }
        }
        stage('deploy') {
            steps {
                container('busybox') {
                    sh """
                    curl -X PATCH -H 'Content-Type: application/strategic-merge-patch+json' \\
                    --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt \\
                    -H "Authorization: Bearer \$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \\
                    --data  '{"spec":{"template":{"metadata":{"annotations":{"date":"`date +'%s'`"}}}}}' \\
                    https://kubernetes/apis/apps/v1beta1/namespaces/default/deployments/parcels
                    """
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