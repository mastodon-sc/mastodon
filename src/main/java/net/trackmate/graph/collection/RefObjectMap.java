package net.trackmate.graph.collection;

import java.util.Map;

/**
 * Map-like interface for maps that map possibly reusable references to plain
 * objects.
 */
public interface RefObjectMap< O, V > extends Map< O, V >
{
	public O createRef();

	public void releaseRef( final O obj );
}
