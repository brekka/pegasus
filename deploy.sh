#!/bin/bash
mvn clean install
rsync -av web/target/pegasus-web-*.war nostromo:/tmp/
