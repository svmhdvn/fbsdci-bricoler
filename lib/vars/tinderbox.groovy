def call(Map opts = [:], String commitHash) {
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
  recordIssues quiet: true, sourceCodeRetention: 'LAST_BUILD', tools: [clang(pattern: '_.*')]
  if (fileExists('_.tinderbox.failed')) {
    unstable(readFile('_.tinderbox.failed'))
  }

  def objdirs = opts.targetTuples.collect { "${it[0]}.${it[1]}" }.join(' ')
  //def tarballsToCopy = opts.targetTuples.collect { "${WORKSPACE}/obj.${it[0]}.${it[1]}.tar.zst" }.join(' ')
  def ftpUrl = 'ftp://ftpuser@ftpartifacts'
  sh """
echo mkdir ${commitHash} | ftp ${ftpUrl}
echo ${objdirs} | xargs -n1 -I% sh -c 'tar -C /usr/obj/usr/src/% -cf - . | zstd -c | ftp -u ${ftpUrl}/${commitHash}/obj.%.tar.zst -'
ftp -u ${ftpUrl}/${commitHash}/ _.*
echo dir ${commitHash} | ftp ${ftpUrl}
rm -f _.*
"""
}
