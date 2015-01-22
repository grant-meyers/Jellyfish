package gm.projects.jellyfish;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import gm.projects.jellyfish.tentacles.MutexLock;
import gm.projects.jellyfish.tentacles.SQLColumnToString;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Main {
	private static final String DEFAULT_STARTUP_CONFIG_FILE = "StartupConfig.xml";

	public static void main(String[] args) {
		ArrayList<String> springConfigFileNames;
		ArrayList<String> verifiedSpringConfigs = new ArrayList<String>();
		
		System.out.println("JellyFish Main executed.");
		
		//Load Spring & camel context to download configuration files for running context
		ApplicationContext startupContext = new ClassPathXmlApplicationContext(DEFAULT_STARTUP_CONFIG_FILE);
		((AbstractApplicationContext)startupContext).registerShutdownHook();
		
		//Get created XML context settings filenames
		SQLColumnToString springConfigs = (SQLColumnToString)startupContext.getBean("sqlRowRetriever");
		springConfigFileNames = springConfigs.getFilesCreated();
				
		//Wait for startup route to complete
		MutexLock mutexLock = (MutexLock)startupContext.getBean("configDownloadComplete");
		try {
			mutexLock.getSemaphore().acquire();
		} catch (InterruptedException e1) {	}
		
		//Get the camel context & a producer for logging purposes
		CamelContext camelContext = (CamelContext)startupContext.getBean("camel-startup");
		ProducerTemplate logInterface = camelContext.createProducerTemplate();
		
		//Create new Spring context with downloaded XML configs
		for(String curSpringConfigFileName : springConfigFileNames) {
			Path filePath = Paths.get(curSpringConfigFileName);
			
			if(filePath != null && Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
				verifiedSpringConfigs.add(filePath.toAbsolutePath().toString());
				logInterface.sendBody("log:MainApp?level=INFO", "Adding Spring file to list for runtime context: " + filePath.toString());
			} else {
				logInterface.sendBody("log:MainApp?level=ERROR", "Failed to load file: " + filePath.toString());
			}
		}
		
		//Load Spring XML config into new context
		String[] configs = verifiedSpringConfigs.toArray(new String[verifiedSpringConfigs.size()]);
		FileSystemXmlApplicationContext runtimeContext = new FileSystemXmlApplicationContext(configs, true);
		((AbstractApplicationContext)runtimeContext).registerShutdownHook();
		
		//Testing
		logInterface.sendBody("activemq:queue:jellyfish", "A Message");
		//Testing
		
		
		//Close startup Spring context
		//((ConfigurableApplicationContext)startupContext).close();
				
		//Run context till stop time
		
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(runtimeContext != null) {
			((ConfigurableApplicationContext)runtimeContext).close();
		}
	}

}
