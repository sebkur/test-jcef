# jcef example

To run this example, first build the project:

    ./gradlew clean create

Then modify the script at `scripts/test-jcef.sh` and change this line:

    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/jvm/java-11-openjdk-amd64/lib/

such that it points to your JRE path instead of the hardcoded one.

Next, run this to download and unpack a working build of JCEF:

    ./unpack-jcefbuild.sh

Then you can run the testing application:

    ./scripts/test-jcef
