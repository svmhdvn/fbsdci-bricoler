def call(String target, String targetArch) {
  pipeline {
    agent any
    stages {
      stage('check build') {
        steps {
          copyArtifacts filter: '_.*', projectName: "ci/${BRANCH_NAME}", selector: upstream()
          script {
            if (fileExists('_.tinderbox.failed')) {
              def matcher = readFile('_.tinderbox.failed') =~ "${target}\\.${targetArch} (\\S*) failed, check"
              def errorLogs = matcher.collect { readFile("_.${target}.${targetArch}.${it[1]}") }.join('\n')
              error(errorLogs)
            }
          }
        }
      }
    }
  }
}

