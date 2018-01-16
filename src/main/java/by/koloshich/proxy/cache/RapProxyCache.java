package by.koloshich.proxy.cache;

import com.meylemueller.common.dao.PersistableEntity;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * Cache for proxies of persistence entity. This is cache exists un UI session in RAP application.
 * @see com.meylemueller.web2print2.core.common.proxy.cache.ProxyCache
 */
public class RapProxyCache implements ProxyCache<Long, PersistableEntity>
{
	private Map<Long, PersistableEntity> map = new HashMap<>();

	/**
	 * Returns instance for current UI session.
	 * @return instance
	 */
	public static RapProxyCache getInstance()
	{
		return SingletonUtil.getSessionInstance(RapProxyCache.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistableEntity get(Long key)
	{
		return map.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(PersistableEntity object)
	{
		if (object != null && object.getIdentity() != null)
		{
			map.put(object.getIdentity(), object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		map.clear();
	}
}
