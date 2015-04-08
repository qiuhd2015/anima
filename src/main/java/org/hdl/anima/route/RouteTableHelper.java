package org.hdl.anima.route;

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hdl.anima.common.utils.XMLFileHelper;

/**
 * RouteTableHelper
 * @author qiuhd
 * @since  2014年8月14日
 */
public final class RouteTableHelper {

	private final static String CONF_PATH = "/" ;
	
	private final static String CONF_NAME_STRING = "RouteTable.xml" ;
	
	@SuppressWarnings("unchecked")
	public static void loadFromLocal(RouteTable routeTable) throws Exception {
		Document document= XMLFileHelper.getXMLFile(CONF_PATH + CONF_NAME_STRING);
		Element root = document.getRootElement();
		Iterator<Element> rtIt =  root.elementIterator();
		while (rtIt.hasNext()) {
			Element sEle = rtIt.next();
			String serverId = sEle.attributeValue("id");
			
			Iterator<Element> sI = sEle.elementIterator();
			
			while(sI.hasNext()) {
				Element item = sI.next();
				int msgId = Integer.valueOf(item.getTextTrim());
				routeTable.addRoute(msgId, serverId);
			}
		}
	}
	
}

