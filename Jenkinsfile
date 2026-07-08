pipeline {
  agent any
  stages {
    stage('checkout') {
      agent { label 'fetcher' }
      steps {
        dir ("/exws/src/${BRANCH_NAME}") {
          git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
        }
      }
    }
    stage('build') {
      agent { label 'builder' }
      steps {
        script {
          //tinderbox ['amd64', 'arm64', 'riscv']
          tinderbox "/exws/obj/${BRANCH_NAME}/tinderbox",
            targets: ['amd64'],
            kernconfs: ['GENERIC']
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
