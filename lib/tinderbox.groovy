// must be called inside the obj directory
def call(String[] targets) {
  def target_opts = targets.collect { "TARGETS+=$it" }.join(' ')
  sh ''' \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url=${PWD}/../src \
--freebsd-src-build/objdir=${PWD} \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER UNIVERSE_LOGDIR=${PWD} KERNCONFS+=GENERIC ${target_opts}" \
'''
  archiveArtifacts '_.*'
  // TODO do I need a script{} here?
  if (fileExists('_.tinderbox.failed')) {
    error(readFile('_.tinderbox.failed'))
  }
}

return this
