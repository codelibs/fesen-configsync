Fesen Config Sync Plugin
[![Java CI with Maven](https://github.com/codelibs/fesen-configsync/actions/workflows/maven.yml/badge.svg)](https://github.com/codelibs/fesen-configsync/actions/workflows/maven.yml)
=======================

## Overview

Config Sync Plugin provides a feature to distribute files, such as script or dictionary file, to nodes in your cluster.
These files are managed in .configsync index, and each node sync up with them.

## Version

[Versions in Maven Repository](https://repo1.maven.org/maven2/org/codelibs/fesen-configsync/)

### Issues/Questions

Please file an [issue](https://github.com/codelibs/fesen-configsync/issues "issue").
(Japanese forum is [here](https://github.com/codelibs/codelibs-ja-forum "here").)

## Installation

    $ $ES_HOME/bin/fesen-plugin install org.codelibs:fesen-configsync:7.6.0

## Getting Started

### Register File

    $ curl -XPOST -H 'Content-Type:application/json' localhost:9200/_configsync/file?path=user-dict.txt --data-binary @user-dict.txt

The above request is to add file info to .configsync index.
path parameter is a synced file location under $ES_CONF directory(ex. /etc/fesen/user-dict.txt).

### Get File List

Send GET request without path parameter:

    $ curl -XGET -H 'Content-Type:application/json' localhost:9200/_configsync/file
    {"acknowledged":true,"path":["user-dict.txt"]}

### Get File

Send GET request with path parameter:

    $ curl -XGET -H 'Content-Type:application/json' localhost:9200/_configsync/file?path=user-dict.txt

### Delete File

Send DELETE request with path parameter:

    $ curl -XDELETE -H 'Content-Type:application/json' localhost:9200/_configsync/file?path=user-dict.txt

### Sync

Each node copies a file from .configsync index periodically if the file is updated.
The interval time is specified by configsync.flush_interval in /etc/elasticserch/fesen.yml.
The default value is 1m.

    configsync.flush_interval: 1m

### Reset

To restart a scheduler for checking .configsync index, send POST request as below:

    $ curl -XPOST -H 'Content-Type:application/json' localhost:9200/_configsync/reset
