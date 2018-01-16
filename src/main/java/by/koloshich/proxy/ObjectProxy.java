package by.koloshich.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Proxy for any object.
 */
public class ObjectProxy implements InvocationHandler
{
	private static final String GET_CLASS = "getClass";
	private static final String HASHCODE = "hashCode";
	private static final String TO_STRING = "toString";
	private static final String EQUALS = "equals";
	private static final String NOTIFY = "notify";
	private static final String NOTIFY_ALL = "notifyAll";
	private static final String WAIT = "wait";
	private Class objectClass;
	private int hashCode;

	public ObjectProxy(Class clazz)
	{
		objectClass = clazz;
		hashCode = hashCode();
	}

	/**
	 * Constructor.
	 * @param object any object
	 */
	public ObjectProxy(Object object)
	{
		objectClass = object.getClass();
		hashCode = object.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		String methodName = method.getName();
		switch (methodName)
		{
			case GET_CLASS:
				return this.getClass();
			case HASHCODE:
				return this.hashCode();
			case TO_STRING:
				return this.toString();
			case EQUALS:
				return proxy == args[0];
			case NOTIFY:
				this.notify();
				break;
			case NOTIFY_ALL:
				this.notifyAll();
				break;
			case WAIT:
				thisWait(args);
				break;
		}
		return null;
	}

	/**
	 * Checks method on linking to {@link Object}.
	 * @param method method
	 * @param args arguments
	 * @return true if method from {@link Object}, otherwise false.
	 */
	public boolean isInvoke(Method method, Object[] args)
	{
		String name = method.getName();
		switch (ArrayUtils.getLength(args))
		{
			case 0:
				return name.equals(GET_CLASS) || name.equals(HASHCODE) || name.equals(TO_STRING) || name.equals(NOTIFY) || name
					.equals(NOTIFY_ALL) || name.equals(WAIT);
			case 1:
				return name.equals(EQUALS) || name.equals(WAIT);
			case 2:
				return name.equals(WAIT);
			default:
				return false;
		}
	}

	private void thisWait(Object[] args) throws InterruptedException
	{
		switch (args.length)
		{
			case 0:
				wait();
				break;
			case 1:
				wait((long) args[0]);
				break;
			case 2:
				wait((long) args[0], (int) args[1]);
				break;
			default:
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return super.toString() + "{" + objectClass.getName() + "@" + hashCode + "}";
	}

	/**
	 * Returns class of proxying object.
	 * @return class of proxying object
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}
}
