package net.trackmate.graph;

import java.util.HashSet;
import java.util.Set;

public class CollectionUtils
{
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < T > Set< T > createSet( final T type )
	{
		if ( type instanceof PoolObject )
			return ( Set< T > ) new PoolObjectSet( ( ( PoolObject ) type ).creatingPool );
		else
			return new HashSet< T >();
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < T > Set< T > createSet( final T type, final int initialCapacity )
	{
		if ( type instanceof PoolObject )
			return ( Set< T > ) new PoolObjectSet( ( ( PoolObject ) type ).creatingPool, initialCapacity );
		else
			return new HashSet< T >( initialCapacity );
	}

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
