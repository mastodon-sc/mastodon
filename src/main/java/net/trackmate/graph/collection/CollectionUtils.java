package net.trackmate.graph.collection;

import java.util.HashSet;
import java.util.Set;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class CollectionUtils
{
	public static < V extends Vertex< ? > > RefSet< V > createVertexSet( final Graph< V, ? > graph )
	{
		if ( graph instanceof CollectionCreator )
			return ( ( CollectionCreator< V, ? > ) graph ).createVertexSet();
		else
			return wrap( new HashSet< V >() );
	}

	public static < V extends Vertex< ? > > RefSet< V > createVertexSet( final Graph< V, ? > graph, final int initialCapacity )
	{
		if ( graph instanceof CollectionCreator )
			return ( ( CollectionCreator< V, ? > ) graph ).createVertexSet( initialCapacity );
		else
			return wrap( new HashSet< V >( initialCapacity ) );
	}

	public static interface CollectionCreator< V extends Vertex< E >, E extends Edge< V > > extends Graph< V, E >
	{
		public RefSet< V > createVertexSet();

		public RefSet< V > createVertexSet( final int initialCapacity );
	}

	private static < O > RefSet< O > wrap( final Set< O > set )
	{
		return new RefSetWrapper< O >( set );
	}
}
