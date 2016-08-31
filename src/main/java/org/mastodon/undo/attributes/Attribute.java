package org.mastodon.undo.attributes;

public class Attribute< O >
{
	private final int id;

	private final AttributeUndoSerializer< O > serializer;

	private final String name;

	Attribute( final int id, final AttributeUndoSerializer< O > serializer, final String name )
	{
		this.id = id;
		this.serializer = serializer;
		this.name = name;
	}

	/**
	 * Returns the attribute ID, which is unique per graph and per object type
	 * {@code O}.
	 *
	 * @return unique attribute ID.
	 */
	public int getAttributeId()
	{
		return id;
	}

	public AttributeUndoSerializer< O > getUndoSerializer()
	{
		return serializer;
	}

	@Override
	public String toString()
	{
		return "Attribute(\"" + name + "\"";
	}
}
