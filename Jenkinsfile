buildMvn {
  publishModDescriptor = 'yes'
  mvnDeploy = 'yes'
  doKubeDeploy = true
  buildNode = 'jenkins-agent-java21'

  doDocker = {
    buildJavaDocker {
      publishMaster = 'yes'
      // healthChk is in PubSubIT.java because starting the module requires a running PostgreSQL
    }
  }
}
