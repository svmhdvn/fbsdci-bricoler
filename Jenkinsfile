pipeline {
  agent { label 'builder' }
  stages {
    stage('checkout') {
      steps {
        dir ("/exws/${JOB_NAME}/src") {
          git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
        }
      }
    }
    stage('build') {
      steps {
        dir ("/exws/${JOB_NAME}") {
          script {
            tinderbox 'TARGETS+=amd64 TARGETS+=arm64 TARGETS+=riscv'
          }
        }
      }
    }
    stage('test') {
      parallel {
        stage('amd64') {
          agent { label 'bhyve' }
          steps {
            build "${branch}-test-amd64"
          }
        }
        stage('aarch64') {
          agent { label 'qemu' }
          steps {
            build "${branch}-test-aarch64"
          }
        }
        stage('riscv64') {
          agent { label 'qemu' }
          steps {
            build "${branch}-test-riscv64"
          }
        }
        //stage('aarch64') {
        //  steps { build "siva-${branch}-aarch64-test" }
        //}
        //stage('riscv64') {
        //  steps { build "siva-${branch}-riscv64-test" }
        //}
      }
    }
  }
}
