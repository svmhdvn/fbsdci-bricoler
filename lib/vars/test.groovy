def call(Map opts = [:], String target, String targetArch) {
  opts.task = opts.task ?: 'freebsd-regression-test-suite'
  opts.memory = opts.memory ?: 4096
  opts.hypervisor = opts.hypervisor ?: 'qemu'

  // Only override the following parameters if they were explicitly requested.
  // Some bricoler tasks have their own specific config (e.g. dtrace or zfs tests)
  def tests = opts.tests ? "--${opts.task}/tests='${opts.tests.join(' ')}'" : ''
  def packages = opts.packages ? "--freebsd-vm-image/packages='${opts.packages.join(' ')}'" : ''
  def kernelConfig = opts.kernconf ? "--freebsd-src-build/kernel_config='${opts.kernconf}'" : ''
  def objRoot = "/usr/obj/usr/src/${target}.${targetArch}"
  def objTarball = "obj.${target}.${targetArch}.tar.zst"

  // TODO FIGURE THIS OUT
  // --freebsd-src-build/make_options='${installSrcOpts} ${makeOptions}'
  pipeline {
    agent { label "${opts.hypervisor}" }
    parameters {
      string(name: 'SRC_COMMIT_HASH', defaultValue: 'XXX')
    }
    stages {
      stage('test') {
        steps {
          dir ("/usr/src") {
            git url: "ssh://siva@jailhost/home/siva/f/${BRANCH_NAME}", branch: "${BRANCH_NAME}", poll: false, changelog: false
          }
          // TODO convert this to a tarfs mount
          script {
            sh """
scp artifact@ftpartifacts:${objTarball} .
rm -rf ${objRoot}
mkdir -p ${objRoot}
tar -C ${objRoot} -xf ${WORKSPACE}/${objTarball}

bricoler --workdir ${WORKSPACE}/bricoler ${opts.task} \
  --freebsd-src-git-checkout/url=/usr/src \
  --freebsd-src-git-checkout/branch='${SRC_COMMIT_HASH}' \
  --freebsd-src-build/objdir=/usr/obj \
  --freebsd-src-build/machine='${target}/${targetArch}' \
  --freebsd-src-build/make_targets='installworld installkernel distribution' \
  --${opts.task}/hypervisor='${opts.hypervisor}' \
  --${opts.task}/memory='${opts.memory}' \
  ${kernelConfig} ${tests} ${packages}

kyua report-junit -r ${WORKSPACE}/bricoler/${opts.task}/kyua.db > ${WORKSPACE}/kyua.junit.xml
"""
          }

          junit stdioRetention: 'ALL', testResults: 'kyua.junit.xml'
        }
      }
    }
  }
}
