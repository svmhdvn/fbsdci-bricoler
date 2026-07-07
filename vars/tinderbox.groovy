def call(String target_opts) {
  sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url=/exws/${BRANCH_NAME}/src \
--freebsd-src-build/objdir=/exws/tinderbox/${BRANCH_NAME}/obj \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER UNIVERSE_LOGDIR=${WORKSPACE} KERNCONFS+=GENERIC ${target_opts}" \
"""
  archiveArtifacts '_.*'
  // TODO do I need a script{} here?
  if (fileExists('_.tinderbox.failed')) {
    error(readFile('_.tinderbox.failed'))
  }
}
