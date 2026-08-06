def call(String target, String targetArch) {
  pipeline {
    agent any
    stages {
      stage('check build') {
        steps {
          script {
            try {
              // TODO tmp while testing to make it faster to iterate
              //copyArtifacts filter: '_.*', projectName: "ci/${BRANCH_NAME}", selector: upstream()
              copyArtifacts filter: '_.*', projectName: "ci/${BRANCH_NAME}", selector: lastWithArtifacts()
              if (fileExists('_.tinderbox.failed')) {
                def matcher = readFile('_.tinderbox.failed') =~ "${target}\\.${targetArch}.*failed, check .*/(.*) for details"
                def errorLogs = matcher.collect { readFile(it[1]}) }.join('\n')
                error(errorLogs)
              }
            } finally {
              sh 'rm -f _.*'
            }
          }
        }
      }
    }
  }
}

