package org.mastodon.graph.object;

import org.mastodon.graph.Edge;

public abstract class AbstractObjectIdVertex< V extends AbstractObjectIdVertex< V, E >, E extends Edge< ? > > extends AbstractObjectVertex< V, E >
{
	int id;

	protected AbstractObjectIdVertex()
	{
		super();
		id = -1; // not assigned yet
	}
}
