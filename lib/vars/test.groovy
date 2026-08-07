def call(Map opts = [:], String target, String targetArch) {
  opts.task = opts.task ?: 'freebsd-regression-test-suite'
  opts.memory = opts.memory ?: 4096
  opts.hypervisor = opts.hypervisor ?: 'qemu'

  // Only override the following parameters if they were explicitly requested.
  // Some bricoler tasks have their own specific config (e.g. dtrace or zfs tests)
  opts.tests = opts.tests ? "--${opts.task}/tests='${opts.tests}'" : ''

  pipeline {
    agent { label "${opts.hypervisor}" }
    parameters {
      string(name: 'SRC_COMMIT_HASH', defaultValue: 'XXX')
    }
    stages {
      stage('test') {
        steps {
          script {
            sh """
mkdir -p ${WORKSPACE}/bricoler/freebsd-vm-image
scp artifact@ftpartifacts:image.${target}.${targetArch}.img ${WORKSPACE}/bricoler/freebsd-vm-image/

bricoler --workdir ${WORKSPACE}/bricoler --skip ${opts.task} \
  --freebsd-src-build/machine='${target}/${targetArch}' \
  --${opts.task}/hypervisor='${opts.hypervisor}' \
  --${opts.task}/memory='${opts.memory}' \
  ${opts.tests}

kyua report-junit -r ${WORKSPACE}/bricoler/${opts.task}/kyua.db > ${WORKSPACE}/kyua.junit.xml
"""
          }

          junit stdioRetention: 'ALL', testResults: 'kyua.junit.xml'
        }
      }
    }
  }
}
