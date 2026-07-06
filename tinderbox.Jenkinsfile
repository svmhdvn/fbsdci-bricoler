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
          tinderbox 'TARGETS+=amd64 TARGETS+=arm64 TARGETS+=riscv'
        }
      }
    }
  }
}
