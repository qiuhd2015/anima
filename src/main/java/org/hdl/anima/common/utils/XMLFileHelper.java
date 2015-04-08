package org.hdl.anima.common.utils;

import java.io.File;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
/**
 * 系统读取资源配置文件
 * @author qiu.hd
 *
 */
public class XMLFileHelper {
	
    public XMLFileHelper() {}
    
    private static ClassLoader DEFAULT_ClassLoader ;
    
    static{
    	DEFAULT_ClassLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取class path下的资源
     * @param location 路径
     * @return 路径文件，如果指定资源不存在返回null
     */
    public static File getResourceFile(ClassLoader loader,String location) {
		File file = null;
		
		if (location.startsWith("/")) {
			location = location.substring(1);
		}
		
		URL url = null;
		if (loader == null) {
			url = DEFAULT_ClassLoader.getResource(location);
		} else {
			url = loader.getResource(location);
		}
		
		System.out.println(url);

        file = url == null ? null : new File(url.getPath());
        return file;
    }
    
	/**
	 * 获取指定文件 的 xml 文档
	 * 
	 * @param location
	 *            路径
	 * @return 路径文件，如果指定资源不存在返回null
	 */
	public static Document getXMLFile(ClassLoader loader, String location)
			throws DocumentException {
		File file = getResourceFile(loader, location);
		if (file == null) {
			throw new IllegalStateException("The file " + location + " not exsit!");
		} else {
			SAXReader saxReader = new SAXReader();
			saxReader.setEncoding("UTF-8");
			return saxReader.read(file);
		}
	}
	
	/**
	 * 获取指定文件 的 xml 文档
	 * 
	 * @param location
	 *            路径
	 * @return 路径文件，如果指定资源不存在返回null
	 */
	public static Document getXMLFile(String location)
			throws DocumentException {
		File file = getResourceFile(DEFAULT_ClassLoader, location);
		if (file == null) {
			throw new IllegalStateException("Failed to get xml file,cause: file " + location + " not exsit!");
		} else {
			SAXReader saxReader = new SAXReader();
			saxReader.setEncoding("UTF-8");
			return saxReader.read(file);
		}
	}
}
