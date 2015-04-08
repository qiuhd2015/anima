package org.hdl.anima.blacklist;

import java.util.ArrayList;
import java.util.List;

import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;

/**
 * Black list Manager
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class BlackListMgr extends BasicModule{
	
	private List<String> blackList;
	
	public BlackListMgr(String moduleName) {
		super(moduleName);
		blackList = new ArrayList<String>(0);
	}

	@Override
	public String getName() {
		return super.getName();
	}
	
	public boolean contains(String ipAddress) {
		
		if (blackList.size() == 0) {
			return false;
		}
		
		if (blackList.contains(ipAddress)) {
			return true;
		}
		return false;
	}

	@Override
	public void initialize(Application application) {
		super.initialize(application);
	}

	@Override
	public void start() throws IllegalStateException {
		super.start();
		try {
			List<String> address = BlackListHepler.loadFromLocal();
			if (address.size() > 0) {
				this.blackList.addAll(address);
			}
		}catch(Exception e) {
			throw new IllegalStateException("Failed to load local black list.",e);
		}
	}

	@Override
	public void destroy() {
		if (blackList != null) {
			synchronized (this) {
				blackList.clear();
				blackList = null;
			}
		}
	}
}

