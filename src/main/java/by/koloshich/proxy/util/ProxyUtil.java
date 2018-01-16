package by.koloshich.proxy.util;

import com.meylemueller.common.dao.PersistableEntity;
import com.meylemueller.web2print2.core.common.proxy.ValueProxy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Util class for proxy.
 */
public final class ProxyUtil
{
	private ProxyUtil()
	{
	}

	/**
	 * Creates simple proxy by class.
	 * @param clazz class for proxy
	 * @param <T> generic type of proxying object
	 * @return proxy
	 */
	public static <T> T create(Class<T> clazz)
	{
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new ValueProxy(clazz));
	}

	public static boolean isEntity(Class clazz)
	{
		return PersistableEntity.class.isAssignableFrom(clazz);
	}

	public static boolean isEntityCollection(Class clazz)
	{
		if (Collection.class.isAssignableFrom(clazz))
		{
			for (Type type : clazz.getGenericInterfaces())
			{
				if (type instanceof ParameterizedType)
				{
					Type[] actualTypeArguments = ( (ParameterizedType) type ).getActualTypeArguments();
					if (actualTypeArguments.length > 0 && isEntity((Class) actualTypeArguments[0]))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
