def call(
  String machine,
  String machineArch,
  String extraSrcOpts = '',
  String hypervisor = 'qemu',
  String kernconf = 'GENERIC',
  int memory = 4096
) {
  pipeline {
    agent { label "${hypervisor}" }
    stages {
      stage('test') {
        steps {
          sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-regression-test-suite \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url="/exws/${BRANCH_NAME}/src" \
--freebsd-src-build/objdir="/exws/tinderbox/${BRANCH_NAME}/obj" \
--freebsd-src-build/make_targets="installworld installkernel distribution" \
--freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER -DWITHOUT_DTRACE_TESTS ${extraSrcOpts}" \
--freebsd-src-build/machine="${machine}" \
--freebsd-src-build/kernel_config="${kernconf}" \
--freebsd-vm-image/packages= \
--freebsd-regression-test-suite/hypervisor="${hypervisor}" \
--freebsd-regression-test-suite/memory=${memory} \
--freebsd-regression-test-suite/tests=bin/echo \
"""
          sh "kyua report-junit -r ${WORKSPACE}/bricoler/freebsd-regression-test-suite/kyua.db > ${WORKSPACE}/kyua.junit.xml"
          junit stdioRetention: 'ALL', testResults: "kyua.junit.xml"
        }
      }
    }
  }
}
