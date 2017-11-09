pipeline {
    agent none
    environment {
        VERSION = VersionNumber({
            versionNumberString: '${BUILD_DATE_FORMATTED, "yyyy.MM"}.${BUILDS_THIS_MONTH}'
        })
    }
    parameters {
        string(name: 'image_name', defaultValue: 'jotunn/heimdali-api')
        string(name: 'sbt_params', defaultValue: '-sbt-dir /sbt/.sbt -ivy /sbt/.ivy')
    }
    stages {
        stage('build') {
            agent {
                kubernetes {
                    cloud "kubernetes"
                    label 'main'
                    containerTemplate {
                        name 'jdk'
                        image 'java:8-jdk'
                        ttyEnabled true
                        command 'cat'
                    }
                }
            }
            steps {
                sh "./sbt ${params.sbt_params} test"
                script {
                    def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)

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
        stage('prepare') {
            agent {
                kubernetes {
                    cloud "kubernetes"
                    label 'main'
                    containerTemplate {
                        name 'jdk'
                        image 'java:8-jdk'
                        ttyEnabled true
                        command 'cat'
                    }
                }
            }
            steps {
                sh "./sbt ${params.sbt_params} \"set test in dist := {}\" dist"
                sh "unzip target/universal/heimdali-api.zip -d docker"
            }
        }
        stage('publish') {
            agent {
                kubernetes {
                    cloud "kubernetes"
                    label 'main'
                    containerTemplate {
                        name 'docker'
                        image 'docker:1.11'
                        ttyEnabled true
                        command 'cat'
                    }
                }
            }
            steps {
                sh """
                docker login -u $docker_username -p $docker_password
                docker build -t ${params.image_name} docker
                docker tag ${params.image_name} ${params.image_name}:$VERSION
                docker push ${params.image_name}:$VERSION
                docker push ${params.image_name}:latest
                """
            }
        }
        stage('deploy') {
            agent any
            steps {
                sh "kubectl set image deployment heimdali-api heimdali-api=jotunn/heimdali-api:$VERSION"
            }
        }
    }
    post {
        failure {
            slackSend([
                    [
                            title      : "Heimdali API, build #${BUILD_NUMBER}",
                            title_link : "${BUILD_URL}",
                            color      : "warning",
                            author_name: "${GIT_AUTHOR_NAME}",
                            text       : "Failed",
                            fields     : [
                                    [
                                            title: "Branch",
                                            value: "${BRANCH_NAME}",
                                            short: true
                                    ],
                                    [
                                            title: "Test Results",
                                            value: "${env.summary}",
                                            short: true
                                    ],
                                    [
                                            title: "Last Commit",
                                            value: "${LAST_MESSAGE}",
                                            short: false
                                    ]
                            ]
                    ]
            ])
        }
        always {
            junit '**/*Spec.xml'
        }
    }
}