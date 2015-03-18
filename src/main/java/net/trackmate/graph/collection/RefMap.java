package net.trackmate.graph.collection;

import java.util.Map;

public interface RefMap< O, V > extends Map< O, V >
{
	public O createRef();

	public void releaseRef( final O obj );
}
