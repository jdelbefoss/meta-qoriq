FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

require recipes-devtools/qemu/qemu.inc

inherit ptest

RDEPENDS_${PN}-ptest = "bash make"

LIC_FILES_CHKSUM = "file://COPYING;md5=441c28d2cf86e15a37fa47e15a72fbac \
                    file://COPYING.LIB;endline=24;md5=c04def7ae38850e7d3ef548588159913"

SRC_URI = "git://source.codeaurora.org/external/qoriq/qoriq-components/qemu;nobranch=1 \
           file://powerpc_rom.bin \
           file://run-ptest \
           file://memfd.patch \
           "
UPSTREAM_CHECK_REGEX = "qemu-(?P<pver>\d+\..*)\.tar"

SRCREV = "6c297a7ddd355d499ddd31353a763d57a092f851"

S = "${WORKDIR}/git"

COMPATIBLE_HOST_mipsarchn32 = "null"
COMPATIBLE_HOST_mipsarchn64 = "null"

EXTRA_OECONF  = "--target-list=aarch64-softmmu --enable-fdt --enable-kvm --with-system-pixman --disable-werror"

DISABLE_STATIC = ""

do_install_append() {
    # Prevent QA warnings about installed ${localstatedir}/run
    if [ -d ${D}${localstatedir}/run ]; then rmdir ${D}${localstatedir}/run; fi
    install -Dm 0755 ${WORKDIR}/powerpc_rom.bin ${D}${datadir}/qemu
}

do_compile_ptest() {
	make buildtest-TESTS
}

do_install_ptest() {
	cp -rL ${B}/tests ${D}${PTEST_PATH}
	find ${D}${PTEST_PATH}/tests -type f -name "*.[Sshcod]" | xargs -i rm -rf {}

	cp ${S}/tests/Makefile.include ${D}${PTEST_PATH}/tests
	# Don't check the file genreated by configure
	sed -i -e '/wildcard config-host.mak/d' \
	       -e '$ {/endif/d}' ${D}${PTEST_PATH}/tests/Makefile.include
}

INSANE_SKIP_${PN} += "already-stripped"
FILES_${PN} += "/usr/local/bin/* /usr/local/share/*  /usr/local/libexec/* /usr/local/var/* /usr/share/*"

# FIXME: Avoid WARNING due missing patch for native/nativesdk
BBCLASSEXTEND = ""
