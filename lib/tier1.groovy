def call(String branch) {
  def tinderbox = load 'lib/tinderbox.groovy'
  pipeline {
    agent { label 'builder' }
    stages {
      stage('checkout') {
        steps {
          dir ("/exws/${JOB_NAME}/src") {
            git url: 'ssh://siva@jailhost/home/siva/f/${branch}', branch: "${branch}", poll: false
          }
        }
      }
      stage('build') {
        steps {
          dir ("/exws/${JOB_NAME}/obj") {
            script {
              tinderbox ['amd64', 'arm64', 'riscv']
            }
          }
        }
      }
      stage('test') {
        parallel {
          stage('amd64') {
            steps { build "siva-${branch}-amd64-test" }
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
}

return this
