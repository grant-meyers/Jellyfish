package gm.projects.jellyfish.tentacles;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class SQLColumnToString implements Processor {
	private String fileContentsColumn;
	private String fileNameColumn;
	private String filePathColumn = "";
	private ArrayList<String> filesCreated;
	
	public SQLColumnToString() {
		filesCreated = new ArrayList<String>();
	}
	
	public void setFileContentsColumn(String fileContentsColumn) {
		this.fileContentsColumn = fileContentsColumn;
	}

	public String getFileContentsColumn() {
		return fileContentsColumn;
	}

	public void setFileNameColumn(String fileNameColumn) {
		this.fileNameColumn = fileNameColumn;
	}

	public String getFileNameColumn() {
		return fileNameColumn;
	}

	public String getFilePathColumn() {
		return filePathColumn;
	}
	
	public void setFilePathColumn(String filePath) {
		this.filePathColumn = filePath;
	}

	public ArrayList<String> getFilesCreated() {
		return filesCreated;
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		Message ret = new DefaultMessage();
		Object curIndexObject = exchange.getIn().getBody();
		String xmlContent = "";
		String fileName = "default_file.xml";
		String pathName = "";
		
		if(curIndexObject instanceof LinkedCaseInsensitiveMap<?>) {
			@SuppressWarnings("unchecked")
			LinkedCaseInsensitiveMap<Object> body = (LinkedCaseInsensitiveMap<Object>)curIndexObject;
			
			for(Entry<String, Object> entry : body.entrySet()) {				
				if(entry.getKey().equals(fileContentsColumn)) {	
					xmlContent = (String)entry.getValue();
				}
				
				if(entry.getKey().equals(fileNameColumn)) {	
					fileName = (String)entry.getValue();
				}
				
				if(entry.getKey().equals(filePathColumn)) {	
					pathName = (String)entry.getValue();
				}
			}
			InputStream returnValue = new ByteArrayInputStream(xmlContent.getBytes());
			ret.setBody(returnValue);
			ret.setHeader("CamelFileName", pathName+fileName);
			
			if(filePathColumn != null) { 
				fileName = pathName + fileName;
			}
			
			filesCreated.add(fileName);
			exchange.setOut(ret);
		}
	}
}
