package by.koloshich.proxy;

import com.meylemueller.common.dao.PersistableEntity;
import com.meylemueller.common.preferences.factory.entry.EntryFacadeProvider;
import com.meylemueller.web2print2.core.common.proxy.factory.EntityProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.CollectionFactory;

/**
 * Proxy for hibernate entity which implements {@link PersistableEntity} interface.
 * @see com.meylemueller.web2print2.core.common.proxy.factory.EntityProxyFactory
 */
public class EntityProxy extends ValueProxy
{
	private static final String COMPARE_TO = Comparable.class.getMethods()[0].getName();
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityProxy.class);
	private static final String PROPERTY_IDENTITY = "Identity";
	private EntityProxyFactory proxyFactory;

	/**
	 * Constructor.
	 * @param entity proxying entity
	 * @param proxyFactory proxy factory
	 */
	public EntityProxy(PersistableEntity entity, EntityProxyFactory proxyFactory)
	{
		super(entity);
		this.proxyFactory = proxyFactory;
		fillProperties(entity, false);
	}

	/**
	 * Returns unique interfaces which implements this class.
	 *
	 * @param clazz any class
	 * @return list of unique interfaces
	 */
	private static Collection<Class> getInterfaces(Class clazz)
	{
		Collection<Class> interfaces = CollectionFactory.createCollection(Set.class, clazz.getInterfaces().length);
		for (Class interfaceClass : clazz.getInterfaces())
		{
			interfaces.addAll(getInterfaces(interfaceClass));
			interfaces.add(interfaceClass);
		}
		return interfaces;
	}

	/**
	 * Checks containing a entity in return type of a method.
	 *
	 * @param method method
	 * @return true if method return entity or collection of entities, otherwise false
	 */
	private static boolean isEntity(Method method)
	{
		if (PersistableEntity.class.isAssignableFrom(method.getReturnType()))
		{
			return true;
		}
		if (Collection.class.isAssignableFrom(method.getReturnType()))
		{
			return isEntityCollection(method.getGenericReturnType());
		}
		return false;
	}

	/**
	 * Checks containing a entity in generic return type.
	 * @param returnType generic return type
	 * @return true if contains entity, otherwise false
	 */
	private static boolean isEntityCollection(Type returnType)
	{
		if (returnType instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType = (ParameterizedType) returnType;
			if (parameterizedType.getActualTypeArguments().length == 1)
			{
				Type type = parameterizedType.getActualTypeArguments()[0];
				return type instanceof Class && PersistableEntity.class.isAssignableFrom((Class) type);
			}
		}
		return false;
	}

	/**
	 * Returns value from entity by invoking of method.
	 * @param entity entity
	 * @param method method
	 * @return a value from entity which return method.
	 */
	private static Object getValue(PersistableEntity entity, Method method)
	{
		try
		{
			return method.invoke(entity);
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	protected static PersistableEntity findEntity(Class objectClass, Long identity)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Find " + objectClass.getSimpleName() + " with id = " + identity);
		}
		return (PersistableEntity) EntryFacadeProvider.getInstance().getEntryFacade().getEntityManager()
			.find(objectClass, identity);
	}

	protected void fillProperties(PersistableEntity entity, boolean includeEntities)
	{
		for (Class interfaceClass : getInterfaces(getObjectClass()))
		{
			for (Method method : interfaceClass.getDeclaredMethods())
			{
				Property property = buildProperty(method);
				if (property.getType() == PropertyType.GET)
				{
					if (!isEntity(method))
					{
						set(property.getName(), getValue(entity, method));
					}
					else if (includeEntities)
					{
						set(property.getName(), findRelatedProxy(method.getReturnType(), getValue(entity, method)));
					}
				}
			}
		}
	}

	/**
	 * Builds information object for method.
	 * @param method method
	 * @return information object
	 */
	protected Property buildProperty(Method method)
	{
		Property property = super.buildProperty(method);
		if (property.getType() == PropertyType.UNDEFINED)
		{
			if (method.getName().equals(COMPARE_TO) && method.getParameterTypes().length == 1)
			{
				property = new Property(method.getName(), PropertyType.COMPARABLE);
			}
		}
		return property;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (super.isInvoke(method, args))
		{
			return super.invoke(proxy, method, args);
		}
		Property property = buildProperty(method);
		switch (property.getType())
		{
			case GET:
				Object related = findRelated(method);
				set(property.getName(), related);
				return related;
			case COMPARABLE:
				return compareTo(args[0]);
		}
		return null;
	}

	/**
	 * Find related object with proxying entity.
	 * @param method invoking method
	 * @return related object
	 */
	private Object findRelated(Method method)
	{
		PersistableEntity entity = findEntity(getObjectClass(), (Long) get(PROPERTY_IDENTITY));
		Object value = getValue(entity, method);
		return findRelatedProxy(method.getReturnType(), value);
	}

	private Object findRelatedProxy(Class<?> returnType, Object value)
	{
		if (Collection.class.isAssignableFrom(returnType))
		{
			return proxyFactory.getProxies((Collection) value, returnType);
		}
		if (value != null)
		{
			return proxyFactory.getProxy((PersistableEntity) value);
		}
		return null;
	}

	/**
	 * Compare object. Finds proxying entity and compares with object.
	 */
	private int compareTo(Object object)
	{
		Comparable comparable = (Comparable) findEntity(getObjectClass(), (Long) get(PROPERTY_IDENTITY));
		return comparable.compareTo(object);
	}

	public void merge(PersistableEntity entity)
	{
		fillProperties(entity, false);
	}

	public void mergeFull(PersistableEntity entity)
	{
		fillProperties(entity, true);
	}
}
