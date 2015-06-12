#/bin/bash
DIR=`dirname $0`
LOG=/home/user/output.log
cd $DIR
echo "Running at $(date) for 2.10 -> 2.11" >> $LOG
java -jar release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.10 -t 2.11 | gist -f analysis.txt -u 7a323f5e2820d8479b18 2>&1 >> $LOG
echo "Running at $(date) for 2.11 -> 2.12.0-M1" >> $LOG
java -jar release-train-main-assembly-0.1-SNAPSHOT.jar -q -l 2.11 -t 2.12.0-M1 | gist -f analysis.txt -u d73062db6044fa0a8933 2>&1 >> $LOG

# installed in crontab with this line
# 26 21 * * * /home/user/dev/release-train/update.sh
