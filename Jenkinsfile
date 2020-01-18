buildMvn {
  publishModDescriptor = 'yes'
  publishAPI = 'yes'
  mvnDeploy = 'yes'
  runLintRamlCop = 'yes'

  doDocker = {
    buildJavaDocker {
      publishMaster = 'yes'
      healthChk = 'yes'
      healthChkCmd = '-interval=5m --timeout=3s curl -sS --fail -o /dev/null  http://localhost:8081/apidocs || exit 1'
    }
  }
}
