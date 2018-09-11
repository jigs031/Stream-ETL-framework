#/bin/ksh

#if [[ $# != 1 ]]; then
#   echo "config file is missing."
#   exit 1
#fi

CONFIG_FILE="$1"
PROCESSOR_LIBS_DIR="$(grep 'processor.lib.dir' ${CONFIG_FILE}|cut -d= -f2)"

echo "$(find ${PROCESSOR_LIBS_DIR} -type f -name '*.jar' | tr '\n' ':')"

export CLASSPATH="${CLASSPATH}:$(find /opt/confluent/share/java/kafka -type f -name '*.jar'  | tr '\n' ':'):$(find ${PROCESSOR_LIBS_DIR} -type f -name '*.jar' | tr '\n' ':')"
echo $CLASSPATH
#export JMX_PROP=" -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=96.66.74.61 -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.port=33335"

java -Dkafka.logs.dir=/root/KStream/logs -Dlog4j.configuration=file:/root/KStream/slf4j.properties  -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=96.66.74.61 -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.port=33335  -cp "/root/KStream/KafkaStream.jar:${CLASSPATH}" io.anlyz.main.KStreamLauncher $1
