.SECONDARY:
.SUFIXES:
.PHONY: all src tests clean lint

UNAME:=$(shell uname)

ifneq ($(filter $(UNAME),Linux Darwin),)
MAKE_FLAGS := -j8
endif

default: all

src:
	$(MAKE) $(MAKE_FLAGS) -C src
tests: src
	$(MAKE) $(MAKE_FLAGS) -C tests
check: tests
	$(MAKE) $(MAKE_FLAGS) -C tests check

all: src

clean:
	$(MAKE) $(MAKE_FLAGS) -C src clean
	$(MAKE) $(MAKE_FLAGS) -C tests clean

lint:
	cppcheck --enable=all --std=c++17 \
	         --suppress=missingIncludeSystem \
	         src/
