def call(Map opts = [:]) {
  def toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''

  // `make tinderbox` has its own list of machine/machineArch targets, so
  // there's no need to list them explicitly.
  def targetOpts = ''
  if (opts.targets) {
    for (t in opts.targets) {
      targetOpts += " TARGETS+=${t}"
    }
  }

  def kernconfs = opts.kernconfs ?: ['GENERIC', 'GENERIC-KASAN', 'GENERIC-KMSAN', 'LINT']
  def kernconfsOpts = ''
  for (k in kernconfs) {
    kernconfsOpts += " KERNCONFS+=${k}"
  }

  def buildSrcOpts = opts.makeOptions ? opts.makeOptions.join(' ') : ''

  def src = "${WORKSPACE}/src"
  def obj = "${WORKSPACE}/obj"
  sh """
bricoler --workdir ${WORKSPACE}/bricoler freebsd-src-build \
  --freebsd-src-git-checkout/url='${src}' \
  --freebsd-src-git-checkout/branch= \
  --freebsd-src-build/objdir='${obj}' \
  --freebsd-src-build/clean=True \
  --freebsd-src-build/make_targets=tinderbox \
  --freebsd-src-build/make_options='UNIVERSE_LOGDIR=${WORKSPACE} ${buildSrcOpts} ${targetOpts} ${kernconfsOpts}' \
  ${toolchain} || true
"""

  // Archive the logs for public use, then remove these artifacts locally to avoid
  // future runs from picking up stale lingering logs.
  archiveArtifacts '_.*'
  if (fileExists('_.tinderbox.failed')) {
    unstable(readFile('_.tinderbox.failed'))
  }
  sh 'rm -f _.*'
}
