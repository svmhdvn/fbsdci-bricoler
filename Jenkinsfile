pipeline {
  agent { label 'builder' }
  stages {
    stage('checkout') {
      steps {
        dir ("/exws/${BRANCH_NAME}/src") {
          git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
        }
      }
    }
    stage('build') {
      steps {
        script {
          //tinderbox ['amd64', 'arm64', 'riscv']
          tinderbox ['amd64'], "/exws/${BRANCH_NAME}/obj.tinderbox"
        }
      }
    }
    stage('test') {
      parallel {
        stage('amd64') {
          steps { build "test-amd64/${BRANCH_NAME}" }
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
