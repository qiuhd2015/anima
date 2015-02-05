package org.hdl.anima.route;

import java.util.List;
import java.util.Random;

import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
/**
 * 
 * RouteTable
 * @author qiuhd
 *
 */
public class RouteTable extends BasicModule{

	private final LinkedListMultimap<Integer, String> routeTable;	//路由表 key:消息id value:服务器id
	private final Random random = new Random();
	
	public RouteTable(String moduleName) {
		super(moduleName);
		routeTable = LinkedListMultimap.create();
	}

	public void addRoute(int msgId,String serverId) {
		Preconditions.checkArgument(serverId != null,"server id is null!");
		routeTable.put(msgId, serverId);
	}
	
	/**
	 * 随机返回指定id的服务器
	 * @param msgId 消息id
	 * @return  服务器id，如果没有找到返回{@null}
	 */
	public String getRoute(int msgId) {
		List<String> serverIdSet = routeTable.get(msgId);
		if (serverIdSet != null) {
			if (serverIdSet.size() == 0) {
				return serverIdSet.get(0);
			}
			int randowIndex = random.nextInt(serverIdSet.size());
			return serverIdSet.get(randowIndex);
		}
		return null;
	}
	
	/**
	 * 返回指定id的所有服务器名称
	 * @param msgId 消息id
	 * @return  服务器id，如果没有找到返回{@null}
	 */
	public List<String> getServernames(int msgId) {
		return routeTable.get(msgId);
	}

	@Override
	public void start() throws IllegalStateException {
		try {
			RouteTableHelper.loadFromLocal(this);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load from the static routetable xml file");
		}
	}

	@Override
	public void initialize(Application application) {
		super.initialize(application);
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void destroy() {
		super.destroy();
	}
}
