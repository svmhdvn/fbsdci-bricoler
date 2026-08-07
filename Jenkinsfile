def commitHash = 'NONEXISTENTCOMMITHASH'

pipeline {
  agent { label 'builder' }
  stages {
    stage('build') {
      steps {
        script {
          dir ("src") {
            def scmVars = git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
            commitHash = scmVars.GIT_COMMIT
          }
          tinderbox targets: ['amd64'],
            kernconfs: ['GENERIC']
          //tinderbox targets: ['amd64', 'arm64', 'riscv'],
          //  kernconfs: ['GENERIC']
        }
      }
    }
    stage('test') {
      parallel {
        stage('amd64') {
          steps {
            build "build-amd64/${BRANCH_NAME}"
            build job: "test-amd64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        //stage('aarch64') {
        //  steps { build "test-aarch64/${BRANCH_NAME}" }
        //}
        //stage('riscv64') {
        //  steps { build "test-riscv64/${BRANCH_NAME}" }
        //}
        //stage('dtrace') {
        //  steps { build "dtrace-test-amd64/${BRANCH_NAME}" }
        //}
      }
    }
  }
}
