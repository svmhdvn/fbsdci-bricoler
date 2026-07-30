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
  def buildSrcOpts = '-DWITHOUT_CLANG -DWITHOUT_LLD -DWITHOUT_LLDB -DWITHOUT_LIB32 -DWITHOUT_ZFS_TESTS -DWITH_DTRACE_TESTS'

  def src = "${WORKSPACE}/src"
  def obj = "${WORKSPACE}/obj"
  sh """ \
bricoler -w ${WORKSPACE}/bricoler freebsd-src-build \
--freebsd-src-git-checkout/url=${src} \
--freebsd-src-git-checkout/branch= \
--freebsd-src-build/objdir=${obj} \
--freebsd-src-build/clean=True \
--freebsd-src-build/make_targets=tinderbox \
--freebsd-src-build/make_options="UNIVERSE_LOGDIR=${WORKSPACE} ${buildSrcOpts} ${targetOpts} ${kernconfsOpts}" \
${opts.toolchain} || true \
"""
  archiveArtifacts "_.*"
  if (fileExists('_.tinderbox.failed')) {
    error(readFile('_.tinderbox.failed'))
  }
  sh "ls -1 '${obj}${src}' | xargs -P8 -I% tar --zstd -C ${obj} -cvf ${WORKSPACE}/obj.%.tar.zst ${obj}${src}/%"
}
