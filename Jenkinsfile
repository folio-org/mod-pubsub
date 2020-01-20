def dockerDeploy() {
  def config = [:]


  // Defaults if not defined.
  def dockerfile = config.dockerfile ?: 'Dockerfile'
  def buildContext = config.dockerDir ?: '.'
  def publishMaster = config.publishMaster ?: 'yes'

  try {
    dir("${env.WORKSPACE}/${buildContext}") {
      // build docker image
      sh "docker build --no-cache=true --pull=true -t ${env.name}:${env.version} ."

      // Test container using container healthcheck

        def runArgs = config.runArgs ?: ' '
        def healthChkCmd = 'curl -sS --fail -o /dev/null  http://localhost:8081/admin/health || exit 1'
        def dockerImage = "${env.name}:${env.version}"
        def health = dockerContainerHealthCheck(dockerImage,healthChkCmd,runArgs)

        if (health != 'healthy') {
          echo "Container health check failed: $health"
          sh 'exit 1'
        }
        else {
          echo "Container health check passed."
        }

      // publish image if master branch

      if ((env.BRANCH_NAME == 'master') ||
        (env.isRelease) &&
        (publishMaster ==~ /(?i)(Y|YES|T|TRUE)/)) {
        // publish images to ci docker repo
        echo "Publishing Docker images"
        docker.withRegistry('https://index.docker.io/v1/', 'DockerHubIDJenkins') {
          sh "docker tag ${env.name}:${env.version} ${env.dockerRepo}/${env.name}:${env.version}"
          sh "docker tag ${env.name}:${env.version} ${env.dockerRepo}/${env.name}:latest"
          sh "docker push ${env.dockerRepo}/${env.name}:${env.version}"
          sh "docker push ${env.dockerRepo}/${env.name}:latest"
        }
      }

    } // end dir()

  } // end try

  catch (Exception err) {
    println(err.getMessage());
    throw err
  }

  finally {
    echo "Clean up any temporary docker artifacts"
    sh "docker rmi ${env.name}:${env.version} || exit 0"
    sh "docker rmi ${env.name}:latest || exit 0"
    sh "docker rmi ${env.dockerRepo}/${env.name}:${env.version} || exit 0"
    sh "docker rmi ${env.dockerRepo}/${env.name}:latest || exit 0"
  }

}


def dockerContainerHealthCheck(String dockerImage, String checkCmd, String runArgs) {

  def timeout = '200s'
  def retries = 5
  def cidFile = "${dockerImage}-${env.BUILD_NUMBER}.cid"
  def maxStartupWait = 200
  def health = ''

  try {
    echo "Testing $dockerImage image. Starting container..."
    echo "Timeout: ${timeout}"
    echo "retries: ${retries}"
    echo "cidfile: ${cidFile}"
    echo "dockerImage: ${dockerImage}"
    echo "runArgs: ${runArgs}"

    // exit 1 since 'docker run' can return a variety of non-zero status codes.
    sh """
      docker run -d --health-interval=50s --health-timeout=${timeout} --health-retries=${retries} \
             --health-cmd='${checkCmd}' --cidfile $cidFile $dockerImage $runArgs || exit 1
     """

    def cid = readFile(cidFile)

    for (i = 0; i <maxStartupWait; i++) {

      health = sh(returnStdout: true,
        script: "docker inspect $cid | jq -r \".[].State.Health.Status\"").trim()

      echo "Current Status: $health"

      if (health == 'starting') {
        sleep 50
      }
      else {
        echo "New status: $health"
        break
      }
    }
  }
  catch(Exception err) {
    throw err
  }
  finally {
    sh "docker stop `cat $cidFile` || exit 0"
    sh "docker rm  `cat $cidFile` || exit 0"
    sh "rm -f /tmp/${cidFile} || exit 0"
    return health
  }
}

node('jenkins-slave-all') {

  def doLintRamlCop = false
  def mvnDeploy = true
  def publishModDescriptor = true
  def publishAPI = true
  def doKubeDeploy = false
  def publishPreview = false

  def foliociLib = new org.folio.foliociCommands()

  // location of Maven MD
  def modDescriptor =  'target/ModuleDescriptor.json'


  properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '',
    artifactNumToKeepStr: '15',
    daysToKeepStr: '',
    numToKeepStr: '30'))])

  timeout(60) {

    try {
      stage('Checkout') {
        deleteDir()
        currentBuild.displayName = "#${env.BUILD_NUMBER}-${env.JOB_BASE_NAME}"
        sendNotifications 'STARTED'

        checkout([
          $class: 'GitSCM',
          branches: scm.branches,
          extensions: scm.extensions + [[$class: 'SubmoduleOption',
                                         disableSubmodules: false,
                                         parentCredentials: false,
                                         recursiveSubmodules: true,
                                         reference: '',
                                         trackingSubmodules: false]],
          userRemoteConfigs: scm.userRemoteConfigs
        ])

        echo "Checked out branch: $env.BRANCH_NAME"
      }

      stage('Set Environment') {
        setEnvMvn()
      }

      if (doLintRamlCop) {
        stage('Lint raml-cop') {
          runLintRamlCop()
        }
      }
      stage('Maven Build') {
        echo "Building Maven artifact: ${env.name} Version: ${env.version}"
        withMaven(jdk: 'openjdk-8-jenkins-slave-all',
          maven: 'maven3-jenkins-slave-all',
          mavenSettingsConfig: 'folioci-maven-settings') {

          // Check to see if we have snapshot deps in release
          if (env.isRelease) {
            def snapshotDeps = foliociLib.checkMvnReleaseDeps()
            if (snapshotDeps) {
              echo "$snapshotDeps"
              error('Snapshot dependencies found in release')
            }
          }
          sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install ' \
                + 'org.jacoco:jacoco-maven-plugin:report'
          if ( fileExists(modDescriptor) ) {
            foliociLib.updateModDescriptor(modDescriptor)
          }
        }
      }

      // Run Sonarqube
      stage('SonarQube Analysis') {
        sonarqubeMvn()
      }

      if ( env.isRelease && fileExists(modDescriptor) ) {
        stage('Dependency Check') {
          okapiModDepCheck(modDescriptor)
        }
      }

      // Docker stuff
      stage('Docker Build and Health Check') {
        echo "Building Docker image for $env.name:$env.version"
        dockerDeploy()
      }

      // master branch or tagged releases
      if (( env.BRANCH_NAME == 'master' ) || ( env.isRelease )) {

        // publish MD must come before maven deploy
        if (publishModDescriptor) {
          stage('Publish Module Descriptor') {
            echo "Publishing Module Descriptor to FOLIO registry"
            postModuleDescriptor(modDescriptor)
          }
        }
        if (mvnDeploy) {
          stage('Maven Deploy') {
            echo "Deploying artifacts to Maven repository"
            withMaven(jdk: 'openjdk-8-jenkins-slave-all',
              maven: 'maven3-jenkins-slave-all',
              mavenSettingsConfig: 'folioci-maven-settings') {
              sh 'mvn -DskipTests deploy'
            }
          }
        }
        if (publishAPI) {
          stage('Publish API Docs') {
            echo "Publishing API docs"
            sh "python3 /usr/local/bin/generate_api_docs.py -r $env.projectName -l info -o folio-api-docs"
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                              accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                              credentialsId: 'jenkins-aws',
                              secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
              sh 'aws s3 sync folio-api-docs s3://foliodocs/api'
            }
          }
        }
        if (doKubeDeploy) {
          stage('Kubernetes Deploy') {
            echo "Deploying to kubernetes cluster"
            kubeDeploy('folio-default',
              "[{" +
                "\"name\" : \"${env.name}\"," +
                "\"version\" : \"${env.version}\"," +
                "\"deploy\":true" +
                "}]")
          }
        }
      } else if (env.CHANGE_ID && publishPreview) {
        stage('Publish Preview Module Descriptor') {
          echo "Publishing preview module descriptor to CI preview okapi"
          postPreviewMD()
        }
        stage('Kubernetes Deploy') {
          def previewId = "${env.bareVersion}.${env.CHANGE_ID}.${env.BUILD_NUMBER}"
          echo "Deploying to kubernetes cluster"
          kubeDeploy('folio-preview',
            "[{" +
              "\"name\" : \"${env.name}\"," +
              "\"version\" : \"${previewId}\"," +
              "\"deploy\":true" +
              "}]")
        }
      }


      if (doLintRamlCop) {
        stage('Lint raml schema') {
          runLintRamlSchema()
        }
      }
    } // end try
    catch (Exception err) {
      currentBuild.result = 'FAILED'
      println(err.getMessage());
      echo "Build Result: $currentBuild.result"
      throw err
    }
    finally {
      sendNotifications currentBuild.result
    }
  } //end timeout
} // end node







