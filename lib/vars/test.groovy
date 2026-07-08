def call(Map opts = [:], String objdir) {
  opts.task = opts.task ?: 'freebsd-regression-test-suite'
  opts.memory = opts.memory ?: 4096
  opts.extraSrcOpts = opts.extraSrcOpts ?: ''
  opts.hypervisor = opts.hypervisor ?: 'qemu'
  opts.kernconf = opts.kernconf ? "--freebsd-src-build/kernel_config='${opts.kernconf}'" : ''
  opts.target = opts.target ? "--freebsd-src-build/machine='${opts.target}'" : ''

  // Only override the following parameters if they were explicitly requested.
  // Some bricoler tasks have their own specific config (e.g. dtrace or zfs tests)
  opts.tests = opts.tests ? "--${opts.task}/tests='${opts.tests}'" : ''
  opts.packages = opts.packages ? "--freebsd-vm-image/packages='${opts.packages}'" : ''

  def installSrcOpts = '-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER'

  pipeline {
    agent { label "${opts.hypervisor}" }
    stages {
      stage('test') {
        steps {
          sh """ \
bricoler -w ${WORKSPACE}/bricoler ${opts.task} \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url="/exws/src/${BRANCH_NAME}" \
--freebsd-src-build/objdir="${objdir}" \
--freebsd-src-build/make_targets="installworld installkernel distribution" \
--freebsd-src-build/make_options="${installSrcOpts} ${opts.extraSrcOpts}" \
--${opts.task}/hypervisor="${opts.hypervisor}" \
--${opts.task}/memory=${opts.memory} \
${opts.target} ${opts.kernconf} ${opts.tests} ${opts.packages} \
"""
          sh "kyua report-junit -r ${WORKSPACE}/bricoler/${opts.task}/kyua.db > ${WORKSPACE}/kyua.junit.xml"
          junit stdioRetention: 'ALL', testResults: 'kyua.junit.xml'
        }
      }
    }
  }
}
