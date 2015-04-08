package org.hdl.anima.handler;
/**
 * MappedInterceptor.
 * @author qiuhd
 * @since  2014年11月4日
 * @version V1.0.0
 */
public class MappedInterceptor {

	private final int[] includePatterns;

	private final int[] excludePatterns;

	private final HandlerInterceptor interceptor;

	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map with a {@code null} value matching to all paths
	 * @param interceptor the HandlerInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(int[] includePatterns, HandlerInterceptor interceptor) {
		this(includePatterns, null, interceptor);
	}

	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map with a {@code null} value matching to all paths
	 * @param excludePatterns the path patterns to exclude
	 * @param interceptor the HandlerInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(int[] includePatterns, int[] excludePatterns, HandlerInterceptor interceptor) {
		this.includePatterns = includePatterns;
		this.excludePatterns = excludePatterns;
		this.interceptor = interceptor;
	}

	/**
	 * The path into the application the interceptor is mapped to.
	 */
	public int[] getPathPatterns() {
		return this.includePatterns;
	}

	/**
	 * The actual Interceptor reference.
	 */
	public HandlerInterceptor getInterceptor() {
		return this.interceptor;
	}

	/**
	 * Returns {@code true} if the interceptor applies to the given request path.
	 * @param lookupPath the current request path
	 * @param pathMatcher a path matcher for path pattern matching
	 */
	public boolean matches(int lookupPath) {
		if (this.excludePatterns != null) {
			for (int path : this.excludePatterns) {
				if (path == lookupPath) {
					return false;
				}
			}
		}
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (int path : this.includePatterns) {
				if (path == lookupPath) {
					return true;
				}
			}
			return false;
		}
	}
}
