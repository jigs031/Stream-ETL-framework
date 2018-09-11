package jn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamConfig {
	
	Logger log=LoggerFactory.getLogger(StreamConfig.class);	

	public static final String INPUT_TOPIC="input.topic";
	public static final String OUTPUT_TOPIC="output.topic";
	public static final String ERROR_TOPIC="error.topic";
	public static final String PROCESSOR_CLASS_NAME = "processor.class";
	public static final String PROCESSOR_LIB_DIR = "processor.lib.dir";

	public static final String SHARED_PROCESSORS = "shared.processor";
	private Properties configs=new Properties();
	
	public StreamConfig(String filePath) throws FileNotFoundException, IOException {
		log.info("Reading config file "+ filePath);
		File f=new File(filePath);
		configs.load(new FileInputStream(f));
		log.info("Config file loaded successfully. ");
	}
	
	public Properties getStreamConfig() {
		return configs;
	}
	
	

}
