package org.hdl.anima.route;

import java.util.List;

import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.surrogate.ServerSurrogateMgr;

/**
 * Router
 * @author qiuhd
 * @since  2014年8月5日
 */
public class Router extends BasicModule {

	private ServerSurrogateMgr serverSurrogateMgr;
	private RouteTable routeTable;
	
	public Router(String moduleName) {
		super(moduleName);
	}

	@Override
	public void initialize(Application application) {
		super.initialize(application);
		this.routeTable = application.getMoulde(RouteTable.class);
		this.serverSurrogateMgr = application.getMoulde(ServerSurrogateMgr.class);
	}
	
	public void route(AbstractMessage message) {
		int mid = message.getId();
//		String serverName = routeTable.getRoute(mid);
		List<String> servernames = routeTable.getServernames(mid);
		if (servernames == null || servernames.size() == 0) {
			throw new IllegalStateException("Failed to route this message,message id :"+ mid + " Cause: Counld not found the mapping server name in routetable.xml");
		}
		serverSurrogateMgr.send(servernames, message);
	}
}

