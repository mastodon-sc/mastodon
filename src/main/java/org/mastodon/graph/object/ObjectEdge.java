package org.mastodon.graph.object;

public class ObjectEdge< K > extends AbstractObjectEdge< ObjectEdge< K >, ObjectVertex< K > >
{
	protected ObjectEdge( final ObjectVertex< K > source, final ObjectVertex< K > target )
	{
		super( source, target );
	}
}
