def commitHash = 'NONEXISTENTCOMMITHASH'
//def targets = ['amd64', 'arm64', 'riscv']
def targets = ['amd64']
def kernconfs = ['GENERIC']

// Always build WITH dtrace tests, but install WITHOUT dtrace tests by default
// TODO currently can't do that because of bugs on aarch64 and riscv64
def makeOptions = [
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
          dir ("src") {
            def scmVars = git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false
            commitHash = scmVars.GIT_COMMIT
          }
          tinderbox targets: targets,
            kernconfs: kernconfs,
            makeOptions: makeOptions
        }
      }
    }
    stage('VM image') {
      steps {
        script {
          build "build-amd64/${BRANCH_NAME}"
          vmImage 'amd64', 'amd64', 'GENERIC',
            packages: [],
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
