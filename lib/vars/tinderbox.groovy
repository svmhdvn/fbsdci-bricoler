def call(Map opts = [:], String objdir) {
  opts.toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''

  // `make tinderbox` has its own list of machine/machineArch targets, so
  // there's no need to list them explicitly.
  def targetOpts = ''
  if (opts.targets) {
    for (t in opts.targets) {
      targetOpts += " TARGETS+=${t}"
    }
  }

  // TODO I think LINT should be optional, and should be built only when tier 1 arch tests come clean.
  //opts.kernconfs = opts.kernconfs ?: ['GENERIC', 'GENERIC-KASAN', 'GENERIC-KMSAN', 'LINT']
  opts.kernconfs = opts.kernconfs ?: ['GENERIC', 'GENERIC-KASAN', 'GENERIC-KMSAN']
  def kernconfsOpts = ''
  for (k in opts.kernconfs) {
    kernconfsOpts += " KERNCONFS+=${k}"
  }

  // Always build WITH dtrace tests, but install WITHOUT dtrace tests by default
  def buildSrcOpts = '-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER -DWITH_DTRACE_TESTS'

  sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/branch= \
--freebsd-src-git-checkout/url=/exws/${BRANCH_NAME}/src \
--freebsd-src-build/objdir=${objdir} \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="UNIVERSE_LOGDIR=${WORKSPACE} ${buildSrcOpts} ${targetOpts} ${kernconfsOpts}" \
${opts.toolchain} \
"""
  archiveArtifacts "_.*"
  // TODO do I need a script{} here?
  if (fileExists('_.tinderbox.failed')) {
    error(readFile('_.tinderbox.failed'))
  }
}
