def commitHash = 'NONEXISTENTCOMMITHASH'
def targetTuples = [['amd64', 'amd64'], ['arm64', 'aarch64'], ['riscv', 'riscv64']]
def kernconfs = ['GENERIC']

// Always build WITH dtrace tests, but install WITHOUT dtrace tests by default
// TODO currently can't do that because of bugs on aarch64 and riscv64
def makeOptions = [
  '-DWITH_CCACHE_BUILD',
  '-DWITH_CLEAN',
  '-DWITHOUT_CLANG',
  '-DWITHOUT_LIB32',
  '-DWITHOUT_LLD',
  '-DWITHOUT_LLDB',
  '-DWITHOUT_SYSTEM_COMPILER',
  '-DWITHOUT_SYSTEM_LINKER',
  '-DWITHOUT_ZFS_TESTS',
]

pipeline {
  agent { label 'builder' }
  stages {
    stage('build') {
      steps {
        script {
          dir ("/usr/src") {
            def scmVars = git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
            commitHash = scmVars.GIT_COMMIT
          }
          tinderbox targetTuples: targetTuples,
            kernconfs: kernconfs,
            makeOptions: makeOptions
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
        //stage('dtrace') {
        //  steps { build "dtrace-test-amd64/${BRANCH_NAME}" }
        //}
      }
    }
  }
}
