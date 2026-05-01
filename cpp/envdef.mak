ROOT_DIR:=$(dir $(abspath $(lastword $(MAKEFILE_LIST))))
OBJ_DIR:=$(ROOT_DIR)obj
BIN_DIR:=$(ROOT_DIR)bin

UNAME:=$(shell uname)
PREFIX?=$(HOME)/local
CXXFLAGS = -std=c++17 -O2 -Wall -Wextra -Wpedantic
CCFLAGS_DLL += -fPIC -fvisibility=default
AR=ar
LIBS +=$(BIN_DIR)/libtersedecompress.a

ifneq ($(filter $(UNAME),Linux Darwin),)
INSTALL_CMD:=install -m 644
INSTALL_CMD_BIN:=install -m 755
CC:=clang
CXX:=clang++
LD:=clang++
LIBS +=-lz
else
INSTALL_CMD:=cp
INSTALL_CMD_BIN:=cp
CC:=ibm-clang
CXX:=ibm-clang++
LD:=ibm-clang++
CXXFLAGS +=-m32 -mzos-float-kind=ieee -D_UNIX03_SOURCE -D_UNIX03_THREADS -D_POSIX_SOURCE -D_POSIX_THREADS \
	-D_OPEN_SYS_SOCK_IPV6=1 -D_XOPEN_SOURCE_EXTENDED=1 -DOE_SOCKETS -D_OPEN_SYS_IF_EXT=1
LDFLAGS +=-m32
LIBS +=/lib/libzzX.x
endif

GIT_TAG := $(shell git rev-parse --short HEAD)
