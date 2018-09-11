package jn.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jn.util.EventProcessor;
import jn.util.StreamConfig;

public class KStreamLauncher {
	Logger log=LoggerFactory.getLogger(KStreamLauncher.class);	
	
	Properties prop;
	StreamsBuilder builder;
	KStream<String, String> kStream ;	
	KafkaStreams streams ;
	EventProcessor processor;
	public KStreamLauncher(String propFile) throws FileNotFoundException, IOException {
		log.info("Parsing configuration file {}.",propFile);
		prop=new StreamConfig(propFile).getStreamConfig();
		log.info("Parsed Properties: {}",prop.toString());
	}

	void build() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		builder = new StreamsBuilder();
		
		kStream = builder.stream(Arrays.asList(prop.get(StreamConfig.INPUT_TOPIC).toString().split(",")));
		
		KStream<String, String> enrichedStream = null;
		
		if(Boolean.parseBoolean(prop.getOrDefault(StreamConfig.SHARED_PROCESSORS, "true").toString()))
		{
			log.info("Creating stream with shared processor");
			processor=(EventProcessor)Class.forName(prop.get(StreamConfig.PROCESSOR_CLASS_NAME).toString()).newInstance();
			enrichedStream = kStream.mapValues(value -> processor.process(value)).filter((key,value)->value!=null);
		}
		else {
			log.info("Creating stream with unique processor for each event");
			enrichedStream = kStream.mapValues(value -> {
				try {
					return ((EventProcessor)Class.forName(prop.get(StreamConfig.PROCESSOR_CLASS_NAME).toString()).newInstance()).process(value);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					log.error(e.toString());
				}
				return null;
			}).filter((key,value)->value!=null);
		}
				
		if(prop.getOrDefault(StreamConfig.ERROR_TOPIC,null)!=null) {
			//Create 2 branches for Success and failure events

			if(Boolean.parseBoolean(prop.getOrDefault(StreamConfig.SHARED_PROCESSORS, "true").toString())) {
				//Capture Successfully parsed Events
				enrichedStream.filter((key,value)-> !processor.isErrorMessage((String) value)).to(prop.getProperty(StreamConfig.OUTPUT_TOPIC));	
				
				//Capture Error events
				enrichedStream.filter((key,value)-> processor.isErrorMessage((String) value)).to(prop.getProperty(StreamConfig.ERROR_TOPIC));
			}
			else
			{
				//Capture Successfully parsed Events
				enrichedStream.filter((key,value)-> {
					try {
						return !((EventProcessor)Class.forName(prop.get(StreamConfig.PROCESSOR_CLASS_NAME).toString()).newInstance()).isErrorMessage((String) value);
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}).to(prop.getProperty(StreamConfig.OUTPUT_TOPIC));	
				
				//Capture Error events
				enrichedStream.filter((key,value)-> {
					try {
						return ((EventProcessor)Class.forName(prop.get(StreamConfig.PROCESSOR_CLASS_NAME).toString()).newInstance()).isErrorMessage((String) value);
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}).to(prop.getProperty(StreamConfig.ERROR_TOPIC));
			}
		}
		else {
			//Ignore Failed events. Assumption: Failed event returns null
			enrichedStream.to(prop.getProperty(StreamConfig.OUTPUT_TOPIC));	
		}
		
			
		log.info("Topology: {}",builder.build().describe());
		streams = new KafkaStreams(builder.build(), prop);
		log.info("Topology build succcessfully.");
	}
	
	void start() {
		log.info("Starting the Stream....");
		streams.cleanUp();
		streams.start();
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	
	}
	
	public static void main(String[] args) {

		if(args.length!=1) {
			System.err.println("***** [ERROR] Config file is missing. Please provide config file.*****");
			System.exit(2);
		}

		try {
		KStreamLauncher streaming=new KStreamLauncher(args[0]);
		streaming.build();
		streaming.start();
		
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(3);
		}		
	}

}
