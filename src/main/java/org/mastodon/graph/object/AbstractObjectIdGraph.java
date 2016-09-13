package org.mastodon.graph.object;

import java.util.Collection;

import org.mastodon.graph.GraphIdBimap;

public class AbstractObjectIdGraph< V extends AbstractObjectIdVertex< V, E >, E extends AbstractObjectIdEdge< E, V > > extends AbstractObjectGraph< V, E >
{
	class EdgeBimap extends AbstractObjectIdBimap< E >
	{
		public EdgeBimap( final Class< E > klass )
		{
			super( klass );
		}

		@Override
		public int getId( final E edge )
		{
			if ( edge.id < 0 )
				edge.id = this.createId( edge );
			return edge.id;
		}
	}

	class VertexBimap extends AbstractObjectIdBimap< V >
	{
		public VertexBimap( final Class< V > klass )
		{
			super( klass );
		}

		@Override
		public int getId( final V vertex )
		{
			if ( vertex.id < 0 )
				vertex.id = this.createId( vertex );
			return vertex.id;
		}
	}

	private final GraphIdBimap< V, E > idmap;

	protected AbstractObjectIdGraph(
			final Factory< V, E > factory,
			final Class< V > vertexClass,
			final Class< E > edgeClass,
			final Collection< V > vertices,
			final Collection< E > edges )
	{
		super( factory, vertices, edges );
		idmap = new GraphIdBimap<>( new VertexBimap( vertexClass ), new EdgeBimap( edgeClass ) );
	}

	public GraphIdBimap< V, E > getIdBimap()
	{
		return idmap;
	}
}
