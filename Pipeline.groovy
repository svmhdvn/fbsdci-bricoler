def exws_src = exwsAllocate 'exws_src'
def exws_obj = exwsAllocate 'exws_obj'

def withBricolerSetup(wsrc, wobj, Closure body) {
    exws (wsrc) {
        exws (wobj) {
            dir ("${WORKSPACE}") {
                withEnv(["BRICOLER_WORKDIR=${WORKSPACE}/bricoler"]) {
                    body()
                }
            }
        }
    }
}

pipeline {
    agent { label 'builder' }
    stages {
        stage('build') {
            steps {
                exws (exws_src) {
                    git url: 'ssh://siva@jailhost/home/siva/f/stable/15', branch: 'main', poll: false
                }
                withEnv(["BRICOLER_WORKDIR=${WORKSPACE}/bricoler"]) {
                    sh 'bricoler freebsd-src-build --freebsd-src-git-checkout/url=/exws/src/${JOB_NAME} --freebsd-src-build/objdir=/exws/obj/${JOB_NAME} --freebsd-src-git-checkout/branch= --freebsd-src-build/make_targets=tinderbox --freebsd-src-build/clean=True --freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER UNIVERSE_LOGDIR=/exws/obj/${JOB_NAME} TARGETS+=riscv KERNCONFS+=QEMU TARGETS+=arm64 KERNCONFS+=VIRT"'
                }
            }
        }
        stage('test') {
            parallel {
                stage('riscv64_qemu') {
                    agent { label 'qemu' }
                    steps {
                        withEnv(["BRICOLER_WORKDIR=${WORKSPACE}/bricoler"]) {
                            sh 'bricoler freebsd-regression-test-suite --freebsd-src-git-checkout/url=/exws/src/${JOB_NAME} --freebsd-src-build/objdir=/exws/obj/${JOB_NAME} --freebsd-src-git-checkout/branch= --freebsd-src-build/make_targets="installworld installkernel distribution" --freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER" --freebsd-vm-image/packages= --freebsd-regression-test-suite/tests=bin/echo --freebsd-src-build/machine=riscv/riscv64 --freebsd-regression-test-suite/hypervisor=qemu --freebsd-src-build/kernel_config=QEMU --freebsd-regression-test-suite/memory=4096'
                        }
                        sh "kyua report-junit -r ${WORKSPACE}/bricoler/freebsd-regression-test-suite/kyua.db > ${WORKSPACE}/riscv64_qemu.junit.xml"
                        junit stdioRetention: 'ALL', testResults: '*.junit.xml'
                    }
                }
                stage('aarch64_qemu') {
                    agent { label 'qemu' }
                    steps {
                        withEnv(["BRICOLER_WORKDIR=${WORKSPACE}/bricoler"]) {
                            sh 'bricoler freebsd-regression-test-suite --freebsd-src-git-checkout/url=/exws/src/${JOB_NAME} --freebsd-src-build/objdir=/exws/obj/${JOB_NAME} --freebsd-src-git-checkout/branch= --freebsd-src-build/make_targets="installworld installkernel distribution" --freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER" --freebsd-vm-image/packages= --freebsd-regression-test-suite/tests=bin/echo --freebsd-src-build/machine=arm64/aarch64 --freebsd-regression-test-suite/hypervisor=qemu --freebsd-src-build/kernel_config=VIRT --freebsd-regression-test-suite/memory=4096'
                        }
                        sh "kyua report-junit -r ${WORKSPACE}/bricoler/freebsd-regression-test-suite/kyua.db > ${WORKSPACE}/riscv64_qemu.junit.xml"
                        junit stdioRetention: 'ALL', testResults: '*.junit.xml'
                    }
                }
            }
        }
    }
}
