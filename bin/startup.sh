APPLICATION="autojob"

APPLICATION_JAR="${APPLICATION}-0.0.1-SNAPSHOT.jar"

cd `dirname $0`
cd ./
BASE_PATH=`pwd`

pids=$(ps -aux | grep "${APPLICATION_JAR}" | grep -v grep)
for pid in $pids;do
    kill -9 $pid
done

nohup java -Xms512m -Xmx512m -jar ${BASE_PATH}/${APPLICATION_JAR} >/dev/null &
