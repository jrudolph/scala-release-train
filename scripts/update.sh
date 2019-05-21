#/bin/bash
set -e

DIR=`dirname $0`
LOG=$DIR/run/output.log
cd $DIR/run
echo "Running at $(date) for 2.12 -> $LATEST_VERSION" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.12 -t $LATEST_VERSION | gist -f analysis.txt -u 1b054191c235469592f62c7c61cb657d 2>&1 | tee -a $LOG
