package org.mastodon.views.dbvv;

import org.mastodon.Ref;
import org.mastodon.adapter.RefBimap;
import org.mastodon.app.ViewGraph;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

public class IdentityViewGraph< G extends ReadOnlyGraph< V, E >, V extends Vertex< E > & Ref< V >, E extends Edge< V > & Ref< E > >
		implements ViewGraph< V, E, V, E >
{
	private final G graph;

	private final RefBimap< V, V > vertexMap;

	private final RefBimap< E, E > edgeMap;

	public IdentityViewGraph( final G graph )
	{
		this.graph = graph;
		vertexMap = new RefBimap< V, V >()
		{
			@Override
			public V getLeft( final V right )
			{
				return right;
			}

			@Override
			public V getRight( final V left, final V ref )
			{
				return left == null ? null : ref.refTo( left );
			}

			@Override
			public V reusableLeftRef( final V ref )
			{
				return ref;
			}

			@Override
			public V reusableRightRef()
			{
				return graph.vertexRef();
			}

			@Override
			public void releaseRef( final V ref )
			{
				graph.releaseRef( ref );
			}
		};
		edgeMap = new RefBimap< E, E >()
		{
			@Override
			public E getLeft( final E right )
			{
				return right;
			}

			@Override
			public E getRight( final E left, final E ref )
			{
				return left == null ? null : ref.refTo( left );
			}

			@Override
			public E reusableLeftRef( final E ref )
			{
				return ref;
			}

			@Override
			public E reusableRightRef()
			{
				return graph.edgeRef();
			}

			@Override
			public void releaseRef( final E ref )
			{
				graph.releaseRef( ref );
			}
		};
	}

	public G getGraph()
	{
		return graph;
	}

	@Override
	public RefBimap< V, V > getVertexMap()
	{
		return vertexMap;
	}

	@Override
	public RefBimap< E, E > getEdgeMap()
	{
		return edgeMap;
	}

	@Override
	public E getEdge( final V source, final V target )
	{
		return graph.getEdge( source, target );
	}

	@Override
	public E getEdge( final V source, final V target, final E ref )
	{
		return graph.getEdge( source, target, ref );
	}

	@Override
	public Edges< E > getEdges( final V source, final V target )
	{
		return graph.getEdges( source, target );
	}

	@Override
	public Edges< E > getEdges( final V source, final V target, final V ref )
	{
		return graph.getEdges( source, target, ref );
	}

	@Override
	public V vertexRef()
	{
		return graph.vertexRef();
	}

	@Override
	public E edgeRef()
	{
		return graph.edgeRef();
	}

	@Override
	public void releaseRef( final V ref )
	{
		graph.releaseRef( ref );
	}

	@Override
	public void releaseRef( final E ref )
	{
		graph.releaseRef( ref );
	}

	@Override
	public RefCollection< V > vertices()
	{
		return graph.vertices();
	}

	@Override
	public RefCollection< E > edges()
	{
		return graph.edges();
	}
}
