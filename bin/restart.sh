APPLICATION="autojob"

cd `dirname $0`
cd ./
BASE_PATH=`pwd`

echo stop ${APPLICATION} Application...
sh ${BASE_PATH}/shutdown.sh

echo start ${APPLICATION} Application...
sh ${BASE_PATH}/startup.sh