SHELL=/bin/bash

api-jar: .make.api-jar
.make.api-jar:
	sbt "set every test in assembly := {}" api/assembly
	touch $@

ui: .make.ui
.make.ui:
	npm run-script prepare
	touch $@

test-jar: .make.test-jar
.make.test-jar:
	sbt -mem 4096 "set every test in assembly := {}" integration-test/assembly
	sbt -mem 4096 integration-test/test:package
	sbt -mem 4096 common/test:package
	touch $@

test: 
	sbt test

itest-init:
	ln -sf $(shell pwd)/itest-config/application.itest.conf api/src/main/resources/application.conf

itest-config:
	git clone git@bitbucket.org:phdata/heimdali-itest.git itest-config

serve-api:
	echo "using TRUST_STORE: $${TRUST_STORE:?}"
	sbt -Djavax.net.ssl.trustStore=$$TRUST_STORE "api/runMain io.phdata.Server"

serve-ui:
	npm start

init-ui: .make.init-ui
.make.init-ui:
	npm install -g typescript
	npm install
	touch $@

validate: .make.validate
.make.validate:
	build-support/bin/validator -p cloudera-integration/parcel/archway-meta/parcel.json
	build-support/bin/validator -r cloudera-integration/parcel/archway-meta/permissions.json
	build-support/bin/validator -s cloudera-integration/csd/descriptor/service.sdl
	touch $@

.PHONY: dist

parcel: validate-version .make.parcel
.make.parcel:
	echo "using ARCHWAY_VERSION: $${ARCHWAY_VERSION:?}"
	./publish.sh parcel archway
	./publish.sh manifest
	touch $@

csd: validate-version .make.csd
.make.csd:
	echo "using ARCHWAY_VERSION: $${ARCHWAY_VERSION:?}"
	./publish.sh csd
	touch $@

app: api-jar test-jar ui

dist: app parcel csd

# TODO invalidate this of version changes, maybe like https://www.cmcrossroads.com/article/rebuilding-when-files-checksum-changes
validate-version: .make.validate-version
.make.validate-version:
	echo "using ARCHWAY_VERSION: $${ARCHWAY_VERSION:?}"
	echo "$$ARCHWAY_VERSION" > $@

ship: validate-version
	./publish.sh ship

dependencies:
	sbt dependencyUpdates

clean:
	sbt clean 
	rm -rf dist
	rm -rf cloudera-integration/build
	rm -rf .make.*

python-deps:
	pip install -r requirements.txt --extra-index https://$$ARTIFACTORY_USER:$$ARTIFACTORY_TOKEN@repository.phdata.io/artifactory/api/pypi/python-internal/simple

docs: service.md
service.md:
	json_wrangler cloudera-integration/csd/descriptor/service.sdl docs/service.md build-support/csd-docs-template.j2
