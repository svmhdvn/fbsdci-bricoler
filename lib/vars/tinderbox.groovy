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
  // TODO currently can't do that because of bugs on aarch64 and riscv64
  //def buildSrcOpts = '-DWITHOUT_CLANG -DWITHOUT_LLD -DWITHOUT_LLDB -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITH_DTRACE_TESTS'
  def buildSrcOpts = '-DWITHOUT_SYSTEM_COMPILER -DWITHOUT_SYSTEM_LINKER -DWITHOUT_CLANG -DWITHOUT_LLD -DWITHOUT_LLDB -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS'

  def src = "${WORKSPACE}/src"
  def obj = "${WORKSPACE}/obj"
  sh """
bricoler -w ${WORKSPACE}/bricoler/tinderbox freebsd-src-build \
  --freebsd-src-git-checkout/url=${src} \
  --freebsd-src-git-checkout/branch= \
  --freebsd-src-build/objdir=${obj} \
  --freebsd-src-build/clean=True \
  --freebsd-src-build/make_targets=tinderbox \
  --freebsd-src-build/make_options="UNIVERSE_LOGDIR=${WORKSPACE} ${buildSrcOpts} ${targetOpts} ${kernconfsOpts}" \
  ${opts.toolchain} || true
"""

  // Archive the logs for public use, then remove these artifacts locally to avoid
  // future runs from picking up stale lingering logs.
  archiveArtifacts '_.*'
  if (fileExists('_.tinderbox.failed')) {
    unstable(readFile('_.tinderbox.failed'))
  }

  sh """
rm -f _.*
ls -1 '${obj}${src}' | xargs -P8 -I% tar --zstd -C ${obj}${src}/% -cf ${WORKSPACE}/obj.%.tar.zst .
scp ${WORKSPACE}/obj.*.tar.zst artifact@ftpartifacts:
"""

}
