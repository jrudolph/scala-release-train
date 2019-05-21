#/bin/bash
set -e

DIR=`dirname $0`
LOG=$DIR/run/output.log
cd $DIR/run
echo "Running at $(date) for 2.10 -> 2.11" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.10 -t 2.11 | gist -f analysis.txt -u 7a323f5e2820d8479b18 2>&1 | tee -a $LOG
echo "Running at $(date) for 2.11 -> 2.12" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.11 -t 2.12 | gist -f analysis.txt -u e058d52bf7d89d9c4e2602aed4fca6f3 2>&1 | tee -a $LOG
echo "Running at $(date) for 2.12 -> 2.13.0-RC2" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.12 -t 2.13.0-RC1 | gist -f analysis.txt -u 1b054191c235469592f62c7c61cb657d 2>&1 | tee -a $LOG
