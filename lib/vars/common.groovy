// Always build WITH dtrace tests...
// TODO we need to build a toolchain for dtrace tests to work, do that later
def buildMakeOptions = [
  '-DWITH_CCACHE_BUILD',
  '-DWITH_CLEAN',
  '-DWITH_DTRACE_TESTS',
  '-DWITHOUT_TOOLCHAIN',
  '-DWITHOUT_LIB32',
  '-DWITHOUT_SYSTEM_COMPILER',
  '-DWITHOUT_SYSTEM_LINKER',
  '-DWITHOUT_ZFS_TESTS',
]

// ...but install WITHOUT dtrace tests by default
def defaultInstallMakeOptions = [
  '-DWITHOUT_DTRACE_TESTS',
  '-DWITHOUT_TOOLCHAIN',
  '-DWITHOUT_LIB32',
  '-DWITHOUT_ZFS_TESTS',
]

def dtraceInstallMakeOptions = [
  '-DWITH_DTRACE_TESTS',
  '-DWITHOUT_TOOLCHAIN',
  '-DWITHOUT_LIB32',
  '-DWITHOUT_ZFS_TESTS',
]
