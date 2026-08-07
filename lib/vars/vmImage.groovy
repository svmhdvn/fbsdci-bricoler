def call(Map opts = [:], String target, String targetArch, String kernconf) {
  opts.toolchain = opts.toolchain ? "--freebsd-src-build/toolchain=${opts.toolchain}" : ''
  opts.packages = opts.packages ? "--freebsd-vm-image/packages='${opts.packages.join(' ')}'" : ''
  def installSrcOpts = opts.makeOptions ? "--freebsd-src-build/make_options='${opts.makeOptions.join(' ')}'" : ''

  def src = "${WORKSPACE}/src"
  def obj = "${WORKSPACE}/obj"
  sh """
bricoler -w ${WORKSPACE}/bricoler freebsd-vm-image \
  --freebsd-src-git-checkout/url=${src} \
  --freebsd-src-git-checkout/branch= \
  --freebsd-src-build/clean=False \
  --freebsd-src-build/objdir=${obj} \
  --freebsd-src-build/machine='${target}/${targetArch}' \
  --freebsd-src-build/kernel_config='${kernconf}' \
  --freebsd-src-build/make_targets='installworld installkernel distribution' \
  --freebsd-src-build/make_options="${installSrcOpts}" \
  ${opts.toolchain} ${opts.packages}
scp ${WORKSPACE}/bricoler/freebsd-vm-image/image.${target}.${targetArch}.img artifact@ftpartifacts:
"""
}
