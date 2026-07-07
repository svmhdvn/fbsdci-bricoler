def call(String targetOpts) {
  // Build WITH dtrace tests, but install WITHOUT dtrace tests by default
  def buildSrcOpts = '-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER -DWITH_DTRACE_TESTS'
  def kernconfs = 'KERNCONFS+=GENERIC KERNCONFS+=GENERIC-KASAN KERNCONFS+=GENERIC-KMSAN KERNCONFS+=LINT'
  sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url=/exws/${BRANCH_NAME}/src \
--freebsd-src-build/objdir=/exws/tinderbox/${BRANCH_NAME}/obj \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="UNIVERSE_LOGDIR=${WORKSPACE} ${buildSrcOpts} ${kernconfs} ${targetOpts}" \
"""
  archiveArtifacts '_.*'
  // TODO do I need a script{} here?
  if (fileExists('_.tinderbox.failed')) {
    error(readFile('_.tinderbox.failed'))
  }
}
