pipeline {
    agent {
        kubernetes {
            cloud "kubernetes"
            label 'altered-mule-jenkins-slave'
            containerTemplate {
                name 'jdk'
                image 'java:8-jdk'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    environment {
        VERSION = VersionNumber({
            versionNumberString: '${BUILD_DATE_FORMATTED, "yyyy.MM"}.${BUILDS_THIS_MONTH}'
        })
        DOCKER_CRED = credentials('docker')
    }
    parameters {
        string(name: 'image_name', defaultValue: 'jotunn/heimdali-api')
        string(name: 'sbt_params', defaultValue: '-sbt-dir /sbt/.sbt -ivy /sbt/.ivy')
    }
    stages {
        stage('build') {
            steps {
                container("jdk") {
                    sh "./sbt ${params.sbt_params} test"
                    script {
                        def testResultAction = currentBuild.rawBuild.getAction(hudson.tasks.test.AbstractTestResultAction.class)

                        if (testResultAction != null) {
                            total = testResultAction.getTotalCount()
                            failed = testResultAction.getFailCount()
                            skipped = testResultAction.getSkipCount()

                            summary = "Passed: " + (total - failed - skipped)
                            summary = summary + (", Failed: " + failed)
                            summary = summary + (", Skipped: " + skipped)
                        } else {
                            summary = "No tests found"
                        }
                        env.summary = summary
                    }
                }
            }
        }
        stage('prepare') {
            steps {
                container("jdk") {
                    sh "./sbt ${params.sbt_params} \"set test in dist := {}\" dist"
                    sh "unzip target/universal/heimdali-api.zip -d docker"
                }
            }
        }
        stage('publish') {
            steps {
                container("docker") {
                    sh """
                    docker login -u ${DOCKER_CRED_USR} -p ${DOCKER_CRED_PSW}
                    docker build -t ${params.image_name} docker
                    docker tag ${params.image_name} ${params.image_name}:$VERSION
                    docker push ${params.image_name}:$VERSION
                    docker push ${params.image_name}:latest
                    """
                }
            }
        }
        stage('deploy') {
            steps {
                container("jdk") {
                    sh """
                    curl -X PATCH -H 'Content-Type: application/strategic-merge-patch+json' \\
                    --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt \\
                    -H "Authorization: Bearer \$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \\
                    --data '{"spec":{"template":{"spec":{"containers":[{"name":"heimdali-api","image":"${
                        params.image_name
                    }:$VERSION"}]}}}}' \\
                    https://kubernetes/apis/apps/v1beta1/namespaces/default/deployments/heimdali-api
                    """
                }
            }
        }
    }
    post {
        always {
            junit 'target/test-reports/*.xml'
        }
        failure {
            slackSend(message: "Heimdali API, build #${BUILD_NUMBER}", attachments: """[{"title":"Heimdali API, build #${BUILD_NUMBER}","title_link":"${BUILD_URL}","color":"#a64f36","author_name":"${GIT_AUTHOR_NAME}","author_link":"https://github.com/${GIT_AUTHOR_NAME}","author_url":"https://github.com/${GIT_AUTHOR_NAME}.png","pretext":"Build failed","fields":[{"title":"Last Commit","value":"${LAST_MESSAGE}","short":false},{"title":"Branch","value":"${BRANCH_NAME}","short":true},{"title":"Test Results","value":"${env.summary}","short":true}]}]""")
        }
        success {
            slackSend(message: "Heimdali API, build #${BUILD_NUMBER}", attachments: """[{"title":"Heimdali API, build #${BUILD_NUMBER}","title_link":"${BUILD_URL}","color":"#36a64f","author_name":"${GIT_AUTHOR_NAME}","author_link":"https://github.com/${GIT_AUTHOR_NAME}","author_url":"https://github.com/${GIT_AUTHOR_NAME}.png","pretext":"Build succeeded","fields":[{"title":"Last Commit","value":"${LAST_MESSAGE}","short":false},{"title":"Branch","value":"${BRANCH_NAME}","short":true},{"title":"Test Results","value":"${env.summary}","short":true}]}]""")
        }
    }
}