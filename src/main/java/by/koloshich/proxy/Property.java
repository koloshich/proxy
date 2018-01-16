package by.koloshich.proxy;

/**
 * Packages class provides information of property
 */
class Property
{
	private String name;
	private PropertyType type;

	/**
	 * Constructor.
	 * @param name property name
	 * @param type property type
	 */
	public Property(String name, PropertyType type)
	{
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns property name.
	 * @return property name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns property type.
	 * @return property type
	 */
	public PropertyType getType()
	{
		return type;
	}
}
