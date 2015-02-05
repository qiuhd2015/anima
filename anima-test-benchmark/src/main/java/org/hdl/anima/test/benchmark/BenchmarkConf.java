package org.hdl.anima.test.benchmark;

import java.io.FileInputStream;
import java.util.Properties;

import org.hdl.anima.common.utils.XMLFileHelper;

/**
 * 
 * @author qiuhd
 * @since  2014年10月8日
 * @version V1.0.0
 */
public class BenchmarkConf{

	private static final String FILE_NAME = "test.properties" ;
	
	private static final Properties PROPERTIES ;
	
	static {
		try {
			PROPERTIES = new Properties();
			FileInputStream inputStream = new FileInputStream(XMLFileHelper.getResourceFile(null,FILE_NAME));
			PROPERTIES.load(inputStream);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load test.properties");
		}
	}
	
	private BenchmarkConf() {
		
	}
	
	public static Properties getProperties() {
		return PROPERTIES;
	}
	
	public static void main(String []args) {
		BenchmarkConf.getProperties();
	}
}
