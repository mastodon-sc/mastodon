package net.trackmate.graph;

import java.util.HashSet;
import java.util.Set;

public class CollectionUtils
{
	public static < V extends Vertex< ? > > Set< V > createVertexSet( final Graph< V, ? > graph )
	{
		if ( graph instanceof CollectionCreator )
			return ( ( CollectionCreator< V, ? > ) graph ).createVertexSet();
		else
			return new HashSet< V >();
	}

	public static < V extends Vertex< ? > > Set< V > createVertexSet( final Graph< V, ? > graph, final int initialCapacity )
	{
		if ( graph instanceof CollectionCreator )
			return ( ( CollectionCreator< V, ? > ) graph ).createVertexSet( initialCapacity );
		else
			return new HashSet< V >( initialCapacity );
	}

	public static interface CollectionCreator< V extends Vertex< E >, E extends Edge< V > > extends Graph< V, E >
	{
		public Set< V > createVertexSet();

		public Set< V > createVertexSet( final int initialCapacity );
	}
}
