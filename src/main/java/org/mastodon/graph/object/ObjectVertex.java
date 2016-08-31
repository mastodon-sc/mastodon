package org.mastodon.graph.object;

public class ObjectVertex< K > extends AbstractObjectVertex< ObjectVertex< K >, ObjectEdge< K > >
{
	private K content;

	ObjectVertex()
	{}

	public K getContent()
	{
		return content;
	}

	public ObjectVertex< K > init( final K content )
	{
		this.content = content;
		return this;
	}

	@Override
	public String toString()
	{
		return content.toString();
	}
}
