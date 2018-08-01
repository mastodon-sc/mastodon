package org.mastodon.revised.bvv.wrap;

import java.util.Iterator;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bvv.BvvVertex;

public class BvvVertexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements BvvVertex< BvvVertexWrapper< V, E >, BvvEdgeWrapper< V, E > >
{
	private final BvvGraphWrapper< V, E > wrapper;

	final V ref;

	V wv;

	private final EdgesWrapper incomingEdges;

	private final EdgesWrapper outgoingEdges;

	final EdgesWrapper edges;

	private final BvvModelGraphProperties< V, E > modelGraphProperties;

	BvvVertexWrapper( final BvvGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		ref = wrapper.modelGraph.vertexRef();
		incomingEdges = new EdgesWrapper();
		outgoingEdges = new EdgesWrapper();
		edges = new EdgesWrapper();
		modelGraphProperties = wrapper.modelGraphProperties;
	}

	@Override
	public int getInternalPoolIndex()
	{
		return wrapper.idmap.getVertexId( wv );
	}

	@Override
	public void getCovariance( final double[][] mat )
	{
		modelGraphProperties.getCovariance( wv, mat );
	}

	@Override
	public float x()
	{
		return ( float ) modelGraphProperties.getDoublePosition( wv, 0 );
	}

	@Override
	public float y()
	{
		return ( float ) modelGraphProperties.getDoublePosition( wv, 0 );
	}

	@Override
	public float z()
	{
		return ( float ) modelGraphProperties.getDoublePosition( wv, 0 );
	}

	@Override
	public BvvVertexWrapper< V, E > refTo( final BvvVertexWrapper< V, E > obj )
	{
		wv = wrapper.idmap.getVertex( obj.getInternalPoolIndex(), ref );
		return this;
	}

	@Override
	public int getTimepoint()
	{
		return modelGraphProperties.getTimepoint( wv );
	}

	@Override
	public Edges< BvvEdgeWrapper< V, E > > incomingEdges()
	{
		incomingEdges.wrap( wv.incomingEdges() );
		return incomingEdges;
	}

	@Override
	public Edges< BvvEdgeWrapper< V, E > > outgoingEdges()
	{
		outgoingEdges.wrap( wv.outgoingEdges() );
		return outgoingEdges;
	}

	@Override
	public Edges< BvvEdgeWrapper< V, E > > edges()
	{
		edges.wrap( wv.edges() );
		return edges;
	}

	@Override
	public int hashCode()
	{
		return wv.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof BvvVertexWrapper< ?, ? > &&
				wv.equals( ( ( BvvVertexWrapper< ?, ? > ) obj ).wv );
	}

	/**
	 * Returns {@code this} if this {@link BvvVertexWrapper} currently wraps
	 * a {@code V}, or null otherwise.
	 *
	 * @return {@code this} if this {@link BvvVertexWrapper} currently wraps
	 *         a {@code V}, or null otherwise.
	 */
	BvvVertexWrapper< V, E > orNull()
	{
		return wv == null ? null : this;
	}

	/**
	 * If called with a non-null {@link BvvVertexWrapper} returns the
	 * currently wrapped {@code V}, otherwise null.
	 *
	 * @return {@code null} if {@code wrapper == null}, otherwise the {@code V}
	 *         wrapped by {@code wrapper}.
	 */
	static < V extends Vertex< ? > > V wrappedOrNull( final BvvVertexWrapper< V, ? > wrapper )
	{
		return wrapper == null ? null : wrapper.wv;
	}

	class EdgesWrapper implements Edges< BvvEdgeWrapper< V, E > >
	{
		private Edges< E > wrappedEdges;

		private BvvEdgeIteratorWrapper< V, E > iterator = null;

		void wrap( final Edges< E > edges )
		{
			wrappedEdges = edges;
		}

		@Override
		public Iterator< BvvEdgeWrapper< V, E > > iterator()
		{
			if ( iterator == null )
				iterator = new BvvEdgeIteratorWrapper<>( wrapper, wrapper.edgeRef(), wrappedEdges.iterator() );
			else
				iterator.wrap( wrappedEdges.iterator() );
			return iterator;
		}

		@Override
		public int size()
		{
			return wrappedEdges.size();
		}

		@Override
		public boolean isEmpty()
		{
			return wrappedEdges.isEmpty();
		}

		@Override
		public BvvEdgeWrapper< V, E > get( final int i )
		{
			return get( i, wrapper.edgeRef() );
		}

		@Override
		public BvvEdgeWrapper< V, E > get( final int i, final BvvEdgeWrapper< V, E > edge )
		{
			edge.we = wrappedEdges.get( i, edge.ref );
			return edge;
		}

		@Override
		public Iterator< BvvEdgeWrapper< V, E > > safe_iterator()
		{
			return new BvvEdgeIteratorWrapper<>( wrapper, wrapper.edgeRef(), wrappedEdges.iterator() );
		}
	}
}
