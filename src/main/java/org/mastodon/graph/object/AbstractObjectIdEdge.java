package org.mastodon.graph.object;

import org.mastodon.graph.Vertex;

public abstract class AbstractObjectIdEdge< E extends AbstractObjectIdEdge< E, V >, V extends Vertex< ? > > extends AbstractObjectEdge< E, V >
{
	int id;

	protected AbstractObjectIdEdge( final V source, final V target )
	{
		super( source, target );
		id = -1; // not assigned yet
	}
}
