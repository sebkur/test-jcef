#!/bin/bash

wget https://github.com/jcefbuild/jcefbuild/releases/download/v1.0.10-84.3.8%2Bgc8a556f%2Bchromium-84.0.4147.105/linux64.zip
mkdir -p native
unzip -o linux64.zip
cp -a java-cef-build-bin/bin/lib/linux64/* native/
