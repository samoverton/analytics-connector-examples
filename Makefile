
SUBDIRS=aggregates alert-sinks decoders ingesters preprocessors

.PHONY: subdirs $(SUBDIRS)

subdirs: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@
