def call(Map opts = [:], String machine, String machineArch) {
  opts.memory = opts.memory ?: 4096
  opts.hypervisor = opts.hypervisor ?: 'qemu'
  opts.extraSrcOpts = opts.extraSrcOpts ?: ''
  opts.kernconf = opts.kernconf ?: 'GENERIC'
  opts.task = opts.task ?: 'freebsd-regression-test-suite'

  // Only override the following parameters if they were explicitly requested.
  // Some bricoler tasks have their own specific config (e.g. dtrace or zfs tests)
  opts.tests = opts.tests ? "--${opts.task}/tests='${opts.tests}'" : ''
  opts.packages = opts.packages ? "--freebsd-vm-image/packages='${opts.packages}'" : ''

  pipeline {
    agent { label "${opts.hypervisor}" }
    stages {
      stage('test') {
        steps {
          sh """ \
bricoler -w ${WORKSPACE}/bricoler ${opts.task} \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url="/exws/${BRANCH_NAME}/src" \
--freebsd-src-build/objdir="/exws/tinderbox/${BRANCH_NAME}/obj" \
--freebsd-src-build/make_targets="installworld installkernel distribution" \
--freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER ${opts.extraSrcOpts}" \
--freebsd-src-build/machine="${machine}" \
--freebsd-src-build/kernel_config="${opts.kernconf}" \
--${opts.task}/hypervisor="${opts.hypervisor}" \
--${opts.task}/memory=${opts.memory} \
${opts.tests} ${opts.packages} \
"""
          sh "kyua report-junit -r ${WORKSPACE}/bricoler/${opts.task}/kyua.db > ${WORKSPACE}/kyua.junit.xml"
          junit stdioRetention: 'ALL', testResults: "kyua.junit.xml"
        }
      }
    }
  }
}
