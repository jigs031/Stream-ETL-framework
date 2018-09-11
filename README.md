# Streaming ETL Framework

## Introduction

 Streaming ETL framework facilitates easy and rapid way of implementing and deploying Kafka processing component with very basic/no knowledge of Kafka/Kafka Stream. Streaming ETL framework is metadata driven framework that generates stream topology based on metadata and  start processing events with user-defined code. Framework provides the stream processing platform where user only needs to focus on business logic, Kafka infrastructure/streaming complexity is taken  care by framework.

## Features

 Following features of Streaming ETL framework make it efficient general purpose streaming platform.

  Metadata-driven framework.

  Leveraged efficient Kafka Stream API for Stream Extract, Transform and Load operation.

  Rapid development cycle as streaming complexity is hidden from the user. User only needs to focus on event processing logic only.

  A configurable number of processing threads to process Kafka messages.

  Optional User Defined message segregation into Success and Error messages.

  Enables redirection of error message to error topic.

  Dynamic loading and instantiating processing components at runtime.

  Configurable approach on number of processing instances

           o Shared processing object amongst threads (Thread unsafe)

  Single processing instance is created and thread group shares this single processing object for processing.

           o An Object per event (Thread safe).

  A separate instance is created to process an event. post processing object will be cleaned up.

## Command

 CLASSPATH should include dir path of at least below jars.

  StreamingETLFramework jar

  Kafka Streaming Client Jar

  Processor Jar & it dependency

 Note: You could also use a wrapper script to invoke the Framework.

 ### java –cp ${CLASSPATH} jn.main.KStreamLauncher


## Properties

###  application.id

 Unique application ID of User application. Application ID is used to generate consumer.group and client.id while communicating to Kafka servers. It is recommended to change application.id whenever new enhancement require to reprocess all the dataset.

###  bootstrap.servers

 List of comma-separated Kafka servers in <host>:<port> format

###  default.value.serde

 Default value serializer and Deserializer for message value. This setting will be applied to all the stream read/write.

###  default.key.serde

 Default key Serializer and Deserializer for message key. This setting will be applied to all the stream read/write.

###  num.stream.threads

 This number of stream threads will be created under application instance. This should always be equal/less than #Partitions of topic.

### Recommendation : Run at least 2 (let say ‘m’) application instances each with N/m threads to provide fault tolerance and efficient load balancing.

 Default : 1

###  input.topics

 Comma-separated list of input topic names.

###  output.topic

 Name of topic where enriched messages are stored

###  error.topic

 Name of topic where error/unserialized messages are stored. This feature expects user to  override public boolean isErrorMessage(String processedMesage) method in processing class.

### Default : null

 ###  shared.processor

 if true, single instance of processor object is shared by all the threads. Otherwise instance is created for each message.

###  processor.class

 Fully qualified classname of user defined processor.

###  processor.lib.dir

 Directory where all the dependent jars are store along with Processor’s jar


## Sample Property File

## Create Custom Event Processor

 Creating custom Event processor is quite simple and fast. It is nothing more than extending the Base class and overriding the processing class.

 Follow below steps to create processor

 1. Create New Java class and extends jn.util.EventProcessor as parent class. Note:

 jn.util.EventProcessor class is available in StreamingETLFramework.jar.

 2. Override the process() and isErrorMessage() as per business logic

 3. Compile class and generate Jar file.

 4. Create config file along with the processor Fully Qualified Class Name and jar directory path.

 5. Run the wrapper script with config file

 application.id=stream-processorv1.

 bootstrap.servers=localhost:

 default.value.serde=org.apache.kafka.common.serialization.Serdes$StringSerde

 num.stream.threads=

 default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde

 input.topic=testtopic

 output.topic=jmx_test

 error.topic=testtopicrep

 shared.processor=true

 processor.class=jn.parsers.DemoEventProcessorImpl

 processor.lib.dir=/root/KStream/


## Event Processor Code

### Parent Abstract Class: EventProcessor

```
package jn.util;
public abstract class EventProcessor {
```
```
/**
* Method tranforms the input data.
* @param message: Input Kafka Message
* @return enriched message
*/
public abstract String process(String message);
```
```
/**
* [[ Optional ]]
* Method returns true if enriched message is Error Message
* @param message : Enriched/Failed Message
* @return returns true if error message else false
*/
public boolean isErrorMessage(String message) {
return false ;
}
}
```
## Custom Processor Class

**import** jn.util.EventProcessor;
**public class** DemoEventProcessorImpl **extends** EventProcessor {

@Override
**public** String process(String event) {
System. **_out_** .println("Demo EventProcessorImpl:" + event);
JSONObject obj;
JSONObject result= **new** JSONObject();
**try** {
obj = **new** JSONObject(event);
**if** (obj.opt("connector-name")!= **null** ) {
result.put("connector-name", obj.opt("connector-
name").toString());
result.put("length", obj.opt("connector-
name").toString().length()+"");
}
**else** {
//Mark Message as error Message
result.put("ErrorMessage", event);
}
} **catch** (JSONException e) {
**try** {
result.put("ErrorMessage", event);
} **catch** (JSONException e1) {
// **TODO** Auto-generated catch block
e1.printStackTrace();
}


e.printStackTrace();
}
**return** result.toString();
}

@Override
**public boolean** isErrorMessage(String event){
**try** {
**return new**
JSONObject(event).opt("ErrorMessage")== **null**? **false** : **true** ;
} **catch** (JSONException e) {
// **TODO** Auto-generated catch block
e.printStackTrace();
**return true** ;
}
}

## }

## Wrapper Script

#/bin/ksh

CONFIG_FILE="$1"
if [[ "${CONFIG_FILE}" == "" ]];then
echo "USAGE: Config file is missing. Please pass config file path as parameter."
exit 2;
fi

#Dir where kafka client jars are available
KAFKA_SETUP_DIR="/opt/confluent/share/java/kafka"
#Dir where processor libs are available
PROCESSOR_LIBS_DIR="$(grep 'processor.lib.dir' ${CONFIG_FILE}|cut -d= -f2)"

export CLASSPATH="${CLASSPATH}:$(find "${KAFKA_SETUP_DIR}" -type f -name '*.jar' | tr '\n'
':'):$(find ${PROCESSOR_LIBS_DIR} -type f -name '*.jar' | tr '\n' ':')"

echo $CLASSPATH

java -Dlog4j.configuration=file:/root/KStream/slf4j.properties -cp
"/root/KStream/StreamingETLFramework.jar:${CLASSPATH}" jn.main.KStreamLauncher $


