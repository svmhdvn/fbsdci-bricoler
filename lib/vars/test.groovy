def call(Map opts = [:], String target, String targetArch) {
  opts.task = opts.task ?: 'freebsd-regression-test-suite'
  opts.memory = opts.memory ?: 4096
  opts.hypervisor = opts.hypervisor ?: 'qemu'

  // Only override the following parameters if they were explicitly requested.
  // Some bricoler tasks have their own specific config (e.g. dtrace or zfs tests)
  opts.tests = opts.tests ? "--${opts.task}/tests='${opts.tests}'" : ''
  opts.packages = opts.packages ? "--freebsd-vm-image/packages='${opts.packages}'" : ''

  def kernelConfig = opts.kernconf ? "--freebsd-src-build/kernel_config='${opts.kernconf}'" : ''
  //def installSrcOpts = '-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER'
  def makeOptions = opts.extraSrcOpts ? "--freebsd-src-build/make_options='${opts.extraSrcOpts}'" : ''

  pipeline {
    agent { label "${opts.hypervisor}" }
    parameters {
      string(name: 'SRC_COMMIT_HASH', defaultValue: 'XXX')
    }
    stages {
      stage('test') {
        steps {
          dir ("src") {
            git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${SRC_COMMIT_HASH}", poll: false, changelog: false
          }
          def src = "${WORKSPACE}/src"
          def obj = "${WORKSPACE}/obj"
          def objRoot = "${obj}${src}/${target}.${targetArch}"

          sh """
scp artifact@ftpartifacts:obj.${target}.${targetArch}.tar.zst ${WORKSPACE}
mkdir -p ${objRoot}
tar -C ${objRoot} -xf ${WORKSPACE}/obj.${target}.${targetArch}.tar.zst

bricoler -w ${WORKSPACE}/bricoler ${opts.task} \
  --freebsd-src-git-checkout/url='${src}' \
  --freebsd-src-git-checkout/branch= \
  --freebsd-src-build/objdir='${obj}' \
  --freebsd-src-build/machine='${target}/${targetArch}' \
  --freebsd-src-build/make_targets='installworld installkernel distribution' \
  --${opts.task}/hypervisor='${opts.hypervisor}' \
  --${opts.task}/memory='${opts.memory}' \
  ${makeOptions} ${kernelConfig} ${opts.tests} ${opts.packages}

kyua report-junit -r ${WORKSPACE}/bricoler/${opts.task}/kyua.db > ${WORKSPACE}/kyua.junit.xml
"""
          junit stdioRetention: 'ALL', testResults: 'kyua.junit.xml'
        }
      }
    }
  }
}
