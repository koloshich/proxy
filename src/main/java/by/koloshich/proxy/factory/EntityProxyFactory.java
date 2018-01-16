package by.koloshich.proxy.factory;

import com.meylemueller.common.dao.PersistableEntity;
import com.meylemueller.web2print2.core.common.proxy.EntityProxy;
import com.meylemueller.web2print2.core.common.proxy.cache.provider.ProxyCacheProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.hibernate.proxy.HibernateProxy;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.CollectionFactory;

/**
 *
 */
public class EntityProxyFactory
{
	private ProxyCacheProvider proxyCacheProvider;

	/**
	 * Returns proxy for entity from cache or create new proxy and caches her.
	 *
	 * @param object entity
	 * @return proxy
	 */
	public <T extends PersistableEntity> T getProxy(@NotNull T object)
	{
		T proxy = (T) findInCache(object);
		if (proxy == null)
		{
			if (object instanceof HibernateProxy)
			{
				object = (T) ( (HibernateProxy) object ).getHibernateLazyInitializer().getImplementation();
			}
			proxy = (T) Proxy.newProxyInstance(object.getClass().getClassLoader(), object.getClass().getInterfaces(),
				new EntityProxy(object, this));
			putToCache(proxy);
		}
		return proxy;
	}

	private Object findInCache(@NotNull PersistableEntity object)
	{
		return proxyCacheProvider != null ? proxyCacheProvider.getCache().get(object.getIdentity()) : null;
	}

	private void putToCache(@NotNull Object proxy)
	{
		if (proxyCacheProvider != null)
		{
			proxyCacheProvider.getCache().put(proxy);
		}
	}

	/**
	 * Returns list of proxies for entities.
	 *
	 * @param objects        collection of entities
	 * @param collectionType class using for creates returning collection
	 * @return collection of proxies
	 */
	public <T extends PersistableEntity> Collection<T> getProxies(Collection<T> objects, Class collectionType)
	{
		Collection<T> collection = CollectionFactory.createCollection(collectionType, objects.size());
		for (T object : objects)
		{
			collection.add(getProxy(object));
		}
		return collection;
	}

	public <T extends PersistableEntity> boolean merge(T entity)
	{
		T proxy = (T) findInCache(entity);
		if (proxy != null)
		{
			EntityProxy entityProxy = getEntityProxy(proxy);
			if (entityProxy != null)
			{
				entityProxy.merge(entity);
				return true;
			}
		}
		return false;
	}

	public <T extends PersistableEntity> T mergeOrCreate(T entity)
	{
		merge(entity);
		return getProxy(entity);
	}

	private EntityProxy getEntityProxy(Object object)
	{
		if (Proxy.isProxyClass(object.getClass()))
		{
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
			if (invocationHandler instanceof EntityProxy)
			{
				return (EntityProxy) invocationHandler;
			}
		}
		return null;
	}

	/**
	 * Clear cache.
	 */
	public void clear()
	{
		if (proxyCacheProvider != null)
		{
			proxyCacheProvider.getCache().clear();
		}
	}

	public void setProxyCacheProvider(ProxyCacheProvider proxyCacheProvider)
	{
		this.proxyCacheProvider = proxyCacheProvider;
	}
}
