package by.koloshich.proxy.cache.provider;

import com.meylemueller.web2print2.core.common.proxy.cache.ProxyCache;
import com.meylemueller.web2print2.core.common.proxy.cache.RapProxyCache;

/**
 *
 */
public class RapProxyCacheProvider implements ProxyCacheProvider
{
	@Override
	public ProxyCache getCache()
	{
		return RapProxyCache.getInstance();
	}
}
