package net.trackmate.undo.attributes;

import net.trackmate.undo.UndoSerializer;

public class Attribute< O >
{
	private final int id;

	private final UndoSerializer< O > serializer;

	private final String name;

	Attribute( final int id, final UndoSerializer< O > serializer, final String name )
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

	public UndoSerializer< O > getUndoSerializer()
	{
		return serializer;
	}

	@Override
	public String toString()
	{
		return "Attribute(\"" + name + "\"";
	}
}
