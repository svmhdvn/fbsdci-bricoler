def call(Map opts = [:]) {
  def toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''

  opts.targets = opts.targets ?: ['amd64', 'arm64', 'riscv64']
  def targetOpts = opts.targets.collect { "TARGETS+=${it}" }.join(' ')

  opts.kernconfs = opts.kernconfs ?: ['GENERIC', 'GENERIC-KASAN', 'GENERIC-KMSAN', 'LINT']
  def kernconfsOpts = opts.kernconfs.collect { "KERNCONFS+=${it}" }.join(' ')

  def buildSrcOpts = opts.makeOptions ? opts.makeOptions.join(' ') : ''

  sh """
bricoler freebsd-src-build \
  --freebsd-src-git-checkout/url=/usr/src \
  --freebsd-src-git-checkout/branch= \
  --freebsd-src-build/objdir=/usr/obj \
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

  sh """
rm -f _.*
ls -1 /usr/obj/usr/src | grep -F '.' | xargs -P8 -I% tar --zstd -C /usr/obj/usr/src/% -cf ${WORKSPACE}/obj.%.tar.zst .
scp ${WORKSPACE}/obj.*.tar.zst artifact@ftpartifacts:
"""
}
