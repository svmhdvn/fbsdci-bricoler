def commitHash = 'NONEXISTENTCOMMITHASH'
def targetTuples = [['amd64', 'amd64'], ['arm64', 'aarch64'], ['riscv', 'riscv64']]
//def targetTuples = [['amd64', 'amd64']]
def kernconfs = ['GENERIC']

// Always build WITH dtrace tests, but install WITHOUT dtrace tests by default
def makeOptions = [
  '-DWITH_CCACHE_BUILD',
  '-DWITH_CLEAN',
  '-DWITH_DTRACE_TESTS',
  '-DWITHOUT_TOOLCHAIN',
  '-DWITHOUT_LIB32',
  '-DWITHOUT_SYSTEM_COMPILER',
  '-DWITHOUT_SYSTEM_LINKER',
  '-DWITHOUT_ZFS_TESTS',
]

pipeline {
  stages {
    stage('build') {
      agent { label 'builder' }
      steps {
        script {
          dir ("/usr/src") {
            def scmVars = git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
            commitHash = scmVars.GIT_COMMIT
          }
          tinderbox targetTuples: targetTuples,
            kernconfs: kernconfs,
            makeOptions: makeOptions,
            toolchain: 'llvm21' // TODO TMP for improving speed
        }
      }
    }
    stage('test') {
      parallel {
        stage('amd64') {
          steps {
            build job: "test-amd64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        stage('aarch64') {
          steps {
            build job: "test-aarch64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        stage('riscv64') {
          steps {
            build job: "test-riscv64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        stage('dtrace amd64') {
          steps {
            build job: "dtrace-test-amd64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        stage('dtrace aarch64') {
          steps {
            build job: "dtrace-test-aarch64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
        stage('dtrace riscv64') {
          steps {
            build job: "dtrace-test-riscv64/${BRANCH_NAME}",
              parameters: [
                string(name: 'SRC_COMMIT_HASH', value: commitHash)
              ]
          }
        }
      }
    }
  }
}
