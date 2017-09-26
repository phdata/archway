#!groovy
import groovy.json.JsonOutput
import hudson.tasks.junit.CaseResult
import hudson.tasks.test.AbstractTestResultAction

import java.text.SimpleDateFormat

def postgres = containerTemplate(
        name: 'postgres',
        image: 'postgres',
        envVars: [
                envVar(key: 'POSTGRES_DB', value: 'heimdali')
        ],
        ports: [
                portMapping(name: 'postgres', containerPort: 5432, hostPort: 5432)
        ],
        ttyEnabled: true,
        command: 'docker-entrypoint.sh',
        args: 'postgres')

def curl = containerTemplate(
        name: 'curl',
        image: 'spotify/alpine',
        ttyEnabled: true,
        command: 'cat')

def ldap = containerTemplate(
        name: 'ldap',
        image: 'jotunn/openldap',
        envVars: [
                envVar(key: 'LDAP_DOMAIN', value: 'jotunn.io'),
                envVar(key: 'LDAP_READONLY', value: 'true')
        ],
        ports: [
                portMapping(name: 'ldap', containerPort: 389, hostPort: 389)
        ],
        ttyEnabled: true,
        command: '/container/tool/run')

def dockerContainer = containerTemplate(
        name: 'docker',
        image: 'docker:1.11',
        ttyEnabled: true,
        command: 'cat')

def sbt = containerTemplate(
        name: 'sbt',
        image: 'hseeberger/scala-sbt',
        ttyEnabled: true,
        command: 'cat')

def author = ""
def message = ""
def testSummary = ""
def total = 0
def failed = 0
def skipped = 0

def slackSend(attachments) {
    def slackURL = 'https://hooks.slack.com/services/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'
    def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'

    def payload = JsonOutput.toJson([text       : '',
                                     icon_url   : jenkinsIcon,
                                     attachments: attachments
    ])

    container('curl') {
        sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"
    }
}

def isPublishingBranch = { ->
    return env.BRANCH_NAME == 'origin/master' || env.BRANCH_NAME =~ /release.+/
}

def isResultGoodForPublishing = { ->
    return currentBuild.result == null
}

def getGitAuthor = {
    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
    author = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
}

def getLastCommitMessage = {
    message = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
}

@NonCPS
def getTestSummary = { ->
    def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def summary = ""

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
    return summary
}

@NonCPS
def getFailedTests = { ->
    def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def failedTestsString = "```"

    if (testResultAction != null) {
        def failedTests = testResultAction.getFailedTests()

        if (failedTests.size() > 9) {
            failedTests = failedTests.subList(0, 8)
        }

        for (CaseResult cr : failedTests) {
            failedTestsString = failedTestsString + "${cr.getFullDisplayName()}:\n${cr.getErrorDetails()}\n\n"
        }
        failedTestsString = failedTestsString + "```"
    }
    return failedTestsString
}

def populateGlobalVariables = {
    getLastCommitMessage()
    getGitAuthor()
    testSummary = getTestSummary()
}


def image = "jotunn/heimdali-api"
def sbt_params = '-sbt-dir /sbt/.sbt -ivy /sbt/.ivy'
def dateFormat = new SimpleDateFormat("yyyy'.'MM'.${env.BUILD_NUMBER}'")
def version = dateFormat.format(new Date())

def deployment = """
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: heimdali-api
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: heimdali-api
    spec:
      containers:
      - image: jotunn/heimdali-api:$version
        name: heimdali-api
        ports:
        - containerPort: 9000
        env:
        - name: LDAP_BASE_DN
          value: "dc=jotunn,dc=io"
        - name: LDAP_USER_PATH
          value: "ou=edp"
        - name: LDAP_BIND_DN
          valueFrom:
            secretKeyRef:
              name: ldap-creds
              key: username
        - name: LDAP_BIND_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ldap-creds
              key: password
        - name: DB_NAME
          value: heimdali
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-creds
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-creds
              key: password
      imagePullSecrets:
      - name: myregistrykey
"""

podTemplate(
        label: 'heimdali-api',
        containers: [
                postgres,
                ldap,
                sbt,
                curl,
                dockerContainer
        ],
        imagePullSecrets: ['myregistrykey'],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                emptyDirVolume(mountPath: '/app', memory: false),
                persistentVolumeClaim(mountPath: '/sbt', claimName: 'sbt-repo', readOnly: false)
        ]) {
    node('heimdali-api') {
        try {
            stage('Checkout') {
                checkout scm
            }

            stage('Build') {
                container('sbt') {
                    sh "sbt $sbt_params test"

                    step $class: 'JUnitResultArchiver', testResults: '**/*Spec.xml'

                    populateGlobalVariables()
                }

                def buildColor = currentBuild.result == null ? "good" : "warning"
                def buildStatus = currentBuild.result == null ? "Success" : currentBuild.result
                def jobName = "${env.JOB_NAME}"

                // Strip the branch name out of the job name (ex: "Job Name/branch1" -> "Job Name")
                jobName = jobName.getAt(0..(jobName.indexOf('/') - 1))

                if (failed > 0) {
                    buildStatus = "Failed"

                    if (isPublishingBranch()) {
                        buildStatus = "MasterFailed"
                    }

                    buildColor = "danger"
                    def failedTestsString = getFailedTests()

                    slackSend([
                            [
                                    title      : "${jobName}, build #${env.BUILD_NUMBER}",
                                    title_link : "${env.BUILD_URL}",
                                    color      : "${buildColor}",
                                    text       : "${buildStatus}\n${author}",
                                    "mrkdwn_in": ["fields"],
                                    fields     : [
                                            [
                                                    title: "Branch",
                                                    value: "${env.BRANCH_NAME}",
                                                    short: true
                                            ],
                                            [
                                                    title: "Test Results",
                                                    value: "${testSummary}",
                                                    short: true
                                            ],
                                            [
                                                    title: "Last Commit",
                                                    value: "${message}",
                                                    short: false
                                            ]
                                    ]
                            ],
                            [
                                    title      : "Failed Tests",
                                    color      : "${buildColor}",
                                    text       : "${failedTestsString}",
                                    "mrkdwn_in": ["text"],
                            ]
                    ])
                } else {

                    if (isPublishingBranch() && isResultGoodForPublishing()) {
                        stage('Publish the API') {
                            container('docker') {
                                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', passwordVariable: 'docker_password', usernameVariable: 'docker_username')]) {
                                    sh """
                        unzip target/universal/heimdali-api.zip
                        docker login -u $docker_username -p $docker_password
                        docker build -t $image .
                        docker tag $image $image:latest
                        docker tag $image $image:$version
                        docker push $image:$version
                        docker push $image:latest
                        """
                                }
                                sh """kubectl update heimdali-api --image=$image:$version"""
                            }
                        }
                    }

                    slackSend([
                            [
                                    title      : "${jobName}, build #${env.BUILD_NUMBER}",
                                    title_link : "${env.BUILD_URL}",
                                    color      : "${buildColor}",
                                    author_name: "${author}",
                                    text       : "${buildStatus}\n${author}",
                                    fields     : [
                                            [
                                                    title: "Branch",
                                                    value: "${env.BRANCH_NAME}",
                                                    short: true
                                            ],
                                            [
                                                    title: "Test Results",
                                                    value: "${testSummary}",
                                                    short: true
                                            ],
                                            [
                                                    title: "Last Commit",
                                                    value: "${message}",
                                                    short: false
                                            ]
                                    ]
                            ]
                    ])
                }
            }
        } catch (hudson.AbortException ae) {
            // I ignore aborted builds, but you're welcome to notify Slack here
        } catch (e) {
            def buildStatus = "Failed"

            if (isPublishingBranch()) {
                buildStatus = "MasterFailed"
            }

            slackSend([
                    [
                            title      : "${env.JOB_NAME}, build #${env.BUILD_NUMBER}",
                            title_link : "${env.BUILD_URL}",
                            color      : "danger",
                            author_name: "${author}",
                            text       : "${buildStatus}",
                            fields     : [
                                    [
                                            title: "Branch",
                                            value: "${env.BRANCH_NAME}",
                                            short: true
                                    ],
                                    [
                                            title: "Test Results",
                                            value: "${testSummary}",
                                            short: true
                                    ],
                                    [
                                            title: "Last Commit",
                                            value: "${message}",
                                            short: false
                                    ],
                                    [
                                            title: "Error",
                                            value: "${e}",
                                            short: false
                                    ]
                            ]
                    ]
            ])

            throw e
        }
    }
}
