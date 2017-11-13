#! /bin/bash
# License and Copyright
# GenomicsDB Builder Docker was created while Ming Rutar worked at Intel. The code is not
# part of assignment. GenomicsDB is an open source project, see https://github.com/Intel-HLS/GenomicsDB
# The project is stored under Ming's Intel email account and subject to Intel copyright.
# Included GenomicsDB Intel copyright below.
# 
# The MIT License (MIT)
# Copyright (c) 2016-2017 Intel Corporation
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of
# this software and associated documentation files (the "Software"), to deal in
# the Software without restriction, including without limitation the rights to
# use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
# the Software, and to permit persons to whom the Software is furnished to do so,
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# The code utilized some features of RedHat pre-built container

#!/bin/bash
# this file was modified from a Red Hat sample

. /usr/share/cont-lib/cont-lib.sh

useradd -u $HOST_USER_ID -g default -o -c "host user" $HOST_USER_ID -s /bin/bash
chmod 777 /home/default

test -z "$*" && set -- bash
cd $HOME
exec "$@"
