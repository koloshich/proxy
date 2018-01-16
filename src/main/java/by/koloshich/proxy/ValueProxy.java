package by.koloshich.proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class ValueProxy extends ObjectProxy
{
	protected static final String GET_PREFIX = "get";
	protected static final String IS_PREFIX = "is";
	protected static final String SET_PREFIX = "set";
	private Map<String, Object> values = new HashMap<>();

	public ValueProxy(Class clazz)
	{
		super(clazz);
	}

	public ValueProxy(Object object)
	{
		super(object);
	}

	/**
	 * Build {@link Property} by method.
	 * @param method
	 * @return property
	 */
	protected Property buildProperty(@NotNull Method method)
	{
		String methodName = method.getName();
		if (method.getParameterTypes().length == 0)
		{
			if (methodName.startsWith(GET_PREFIX))
			{
				return new Property(methodName.substring(GET_PREFIX.length()), PropertyType.GET);
			}
			if (methodName.startsWith(IS_PREFIX))
			{
				return new Property(methodName.substring(IS_PREFIX.length()), PropertyType.GET);
			}
		}
		if (method.getParameterTypes().length == 1 && methodName.startsWith(SET_PREFIX))
		{
			return new Property(methodName.substring(SET_PREFIX.length()), PropertyType.SET);
		}
		return new Property(methodName, PropertyType.UNDEFINED);
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
		Property property = this.buildProperty(method);
		if (property.getType() == PropertyType.GET)
		{
			return get(property.getName());
		}
		if (property.getType() == PropertyType.SET)
		{
			set(property.getName(), args[0]);
		}
		return null;
	}

	/**
	 * Returns true if map contains property, otherwise false.
	 * @param property property name
	 * @return true if map contains property, otherwise false
	 */
	public boolean contains(@NotNull String property)
	{
		return values.containsKey(property);
	}

	/**
	 * Returns value from map by property.
	 * @param property property name
	 * @return value
	 */
	public Object get(@NotNull String property)
	{
		return values.get(property);
	}

	/**
	 * Sets value to map for property.
	 * @param property property
	 * @param value value
	 */
	public void set(@NotNull String property, Object value)
	{
		values.put(property, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInvoke(Method method, Object[] args)
	{
		boolean isInvoke = super.isInvoke(method, args);
		if (!isInvoke)
		{
			Property property = buildProperty(method);
			if (property.getType() == PropertyType.GET || property.getType() == PropertyType.SET)
			{
				isInvoke = values.containsKey(property.getName());
			}
		}
		return isInvoke;
	}
}
