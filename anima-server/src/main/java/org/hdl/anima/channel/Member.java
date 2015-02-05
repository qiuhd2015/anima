package org.hdl.anima.channel;

import com.google.common.base.Objects;

/**
 * Member
 * @author qiuhd
 * @since 2014年10月30日
 * @version V1.0.0
 */
public class Member {
	
	private final int uid; 			// userId
	private final String fronentId; // 所在服务器id

	public Member(int uid, String fronentId) {
		this.uid = uid;
		this.fronentId = fronentId;
	}

	public int getUid() {
		return uid;
	}

	public String getFronentId() {
		return fronentId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (uid != other.uid)
			return false;
		return true;
	}
}
