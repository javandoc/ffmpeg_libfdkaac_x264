#!/bin/bash
#此脚本用于构建ffmpeg静态库，使用linux版本的ndk，构建系统推荐使用ubuntu14.4 64bit TLS，不要使用cygwin
#set -x


export TMPDIR=$FFMPEG_TMPDIR

SYSROOT_ARCH_DIRNAME=arch-arm
TOOLCHAIN_PREFIX=${NDK_ROOT_PATH}/toolchains/arm-linux-androideabi-${TOOLCHAIN_VERSION}/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-
TARGET_ARCH_OPTION="--arch=arm --cpu=armv7-a --enable-neon" 


PREFIX=${PWD}/../out/${LIBMEDIA_TARGET_ARCH_ABI}
SYSROOT=${NDK_ROOT_PATH}/platforms/${LIBMEDIA_TARGET_PLATFORM}/${SYSROOT_ARCH_DIRNAME}



echo "********support X264,fdk-aac codec******"
./configure --prefix=$PREFIX --enable-pic \
--enable-gpl --enable-nonfree \
--enable-cross-compile \
--cross-prefix=${TOOLCHAIN_PREFIX} \
--sysroot=${SYSROOT} \
--target-os=android \
${TARGET_ARCH_OPTION} \
--enable-memalign-hack \
--disable-programs \
--disable-everything \
--disable-encoders \
--disable-decoders \
--enable-libx264 \
--enable-libfdk-aac \
--enable-encoder=aac \
--enable-encoder=libfdk_aac \
--enable-encoder=libx264 \
--enable-decoder=libfdk_aac \
--enable-decoder=aac \
--enable-decoder=aac_latm \
--enable-decoder=h264 \
--disable-demuxers \
--enable-demuxer=aac  \
--enable-demuxer=asf  \
--enable-demuxer=ffmetadata  \
--enable-demuxer=h264  \
--enable-demuxer=mp3  \
--disable-muxers \
--enable-muxer=adts  \
--enable-muxer=h264  \
--enable-muxer=mp4  \
--enable-muxer=pcm_s16le \
--enable-parser=aac \
--enable-parser=aac_latm \
--enable-parser=h264  \
--disable-zlib \
--disable-debug \
--extra-cflags="-DANDROID -I${PREFIX}/include" \
--extra-ldflags="-L${PREFIX}/lib -lx264 -lfdk-aac -Wl,-dynamic-linker=/system/bin/linker -lc -lm -llog" 

#make clean and disclean can't delete the bellow 4 files,so delete them manual
rm -f compat/strtod.o compat/strtod.d  compat/msvcrt/snprintf.o compat/msvcrt/snprintf.d
make clean
make

#编译的静态库(.a)放在$PREFIX/lib目录下,模块对外头文件存放在$PREFIX/lib目录下
make install

