#/bin/bash
set -e

DIR=`dirname $0`
LOG=$DIR/run/output.log
cd $DIR/run
echo "Running at $(date) for 2.10 -> 2.11" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.10 -t 2.11 | gist -f analysis.txt -u 7a323f5e2820d8479b18 2>&1 | tee -a $LOG
echo "Running at $(date) for 2.11 -> 2.12.0-M3" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.11 -t 2.12.0-M3 | gist -f analysis.txt -u d73062db6044fa0a8933 2>&1 | tee -a $LOG
echo "Running at $(date) for 2.11 -> 2.12.0-M5" | tee -a $LOG
java -jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.11 -t 2.12.0-M5 | gist -f analysis.txt -u d0e20c4a20af44459de1 2>&1 | tee -a $LOG
