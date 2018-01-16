package by.koloshich.proxy.cache;

/**
 * Provides functionality cache for proxy.
 */
public interface ProxyCache<K, V>
{
	/**
	 * Returns cached proxy by key.
	 * @param key key
	 * @return cached proxy
	 */
	V get(K key);

	/**
	 * Puts cached proxy to map.
	 * @param object proxy
	 */
	void put(V object);

	/**
	 * Clear cached proxies.
	 */
	void clear();
}
