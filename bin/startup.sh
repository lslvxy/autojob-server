APPLICATION="autojob"

APPLICATION_JAR="${APPLICATION}.jar"

cd `dirname $0`
BASE_PATH=`pwd`

pids=$(ps -aux | grep "${APPLICATION_JAR}" | grep -v grep)
for pid in $pids;do
    kill -9 $pid
done

nohup java -jar ../target/${APPLICATION_JAR} >/dev/null &
