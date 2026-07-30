def call(Map opts = [:]) {
  opts.toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''

  // `make tinderbox` has its own list of machine/machineArch targets, so
  // there's no need to list them explicitly.
  def targetOpts = ''
  if (opts.targets) {
    for (t in opts.targets) {
      targetOpts += " TARGETS+=${t}"
    }
  }

  opts.kernconfs = opts.kernconfs ?: ['GENERIC', 'GENERIC-KASAN', 'GENERIC-KMSAN', 'LINT']
  def kernconfsOpts = ''
  for (k in opts.kernconfs) {
    kernconfsOpts += " KERNCONFS+=${k}"
  }

  // Always build WITH dtrace tests, but install WITHOUT dtrace tests by default
  def buildSrcOpts = '-DWITHOUT_TOOLCHAIN -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITHOUT_CROSS_COMPILER -DWITH_DTRACE_TESTS'

  def src = "${WORKSPACE}/src"
  def obj = "${WORKSPACE}/obj"
  def logs = "${WORKSPACE}/universe_logs"
  sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/url=${src} \
--freebsd-src-git-checkout/branch= \
--freebsd-src-build/objdir=${obj} \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="UNIVERSE_LOGDIR=${logs} ${buildSrcOpts} ${targetOpts} ${kernconfsOpts}" \
${opts.toolchain} \
"""
  archiveArtifacts "universe_logs/_.*"
  // TODO do I need a script{} here?
  if (fileExists('universe_logs/.tinderbox.failed')) {
    error(readFile('universe_logs/.tinderbox.failed'))
  }
  sh "ls -1 '${obj}${src}' | xargs -P8 -I% tar --zstd -C ${obj} -cvf ${WORKSPACE}/obj.%.tar.zst ${obj}${src}/%"
}
