#/bin/bash
DIR=`dirname $0`
LOG=/home/user/output.log
echo "Running at $(date)" >> $LOG
cd $DIR
java -jar release-train-main-assembly-0.1-SNAPSHOT.jar -q | gist -f analysis.txt -u 7a323f5e2820d8479b18 2>&1 >> $LOG