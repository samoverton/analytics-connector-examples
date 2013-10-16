SUBDIRS=aggregates alert-sinks decoders ingesters preprocessors

all: build

# For these targets, automatically recurse into "SUBDIR" sub-directories
RECUR=clean test install build
.PHONY: $(RECUR)
clean:   TARG=clean
test:    TARG=test
install: TARG=install
build:   TARG=build
$(RECUR): $(SUBDIRS)

.PHONY: subdirs $(SUBDIRS)
subdirs: $(SUBDIRS)

.PHONY: $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ $(TARG)
