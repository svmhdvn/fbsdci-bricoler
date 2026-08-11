def call(Map opts = [:]) {
  def toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''

  opts.targetTuples = opts.targetTuples ?: [['amd64', 'amd64'], ['arm64', 'aarch64'], ['riscv', 'riscv64']]
  def targetOpts = opts.targetTuples.collect { "TARGETS+=${it[0]} TARGET_ARCHES_${it[0]}+=${it[1]}" }.join(' ')

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

  def objdirs = opts.targetTuples.collect { "${it[0]}.${it[1]}" }.join(' ')
  def tarballsToCopy = opts.targetTuples.collect { "${WORKSPACE}/obj.${it[0]}.${it[1]}.tar.zst" }.join(' ')
  sh """
rm -f _.*
echo ${objdirs} | xargs -P8 -n1 -I% tar --zstd -C /usr/obj/usr/src/% -cf ${WORKSPACE}/obj.%.tar.zst .
scp ${tarballsToCopy} artifact@ftpartifacts:
"""
}
