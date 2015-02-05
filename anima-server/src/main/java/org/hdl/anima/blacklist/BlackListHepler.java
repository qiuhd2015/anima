package org.hdl.anima.blacklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hdl.anima.common.utils.XMLFileHelper;

/**
 * Black list  hepler
 * @author qiuhd
 *
 */
public final class BlackListHepler {

	private final static String CONF_PATH = "" ;
	
	private final static String CONF_NAME_STRING = "Blacklist.xml";
	
	@SuppressWarnings("unchecked")
	public static List<String> loadFromLocal() throws Exception {
		Document document= XMLFileHelper.getXMLFile(null, CONF_NAME_STRING);
		List<String> addressList = new ArrayList<String>();
		Element root = document.getRootElement();
		Iterator<Element> eleIt =  root.elementIterator();
		
		while (eleIt.hasNext()) {
			Element itemEle =  eleIt.next();
			String address = itemEle.attributeValue("ip");
			addressList.add(address);
		}
		
		return addressList;
	}
}
