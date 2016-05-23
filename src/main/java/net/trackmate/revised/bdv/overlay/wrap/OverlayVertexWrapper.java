package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayVertex;

public class OverlayVertexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayVertex< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	private final int n = 3;

	private final OverlayGraphWrapper< V, E > wrapper;

	V wv;

	private final EdgesWrapper incomingEdges;

	private final EdgesWrapper outgoingEdges;

	private final EdgesWrapper edges;

	private final OverlayProperties< V, E > overlayProperties;

	OverlayVertexWrapper( final OverlayGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		wv = wrapper.wrappedGraph.vertexRef();
		incomingEdges = new EdgesWrapper( wv.incomingEdges() );
		outgoingEdges = new EdgesWrapper( wv.outgoingEdges() );
		edges = new EdgesWrapper( wv.edges() );
		overlayProperties = wrapper.overlayProperties;
	}

	@Override
	public int getInternalPoolIndex()
	{
		return wrapper.idmap.getVertexId( wv );
	}

	@Override
	public OverlayVertexWrapper< V, E > refTo( final OverlayVertexWrapper< V, E > obj )
	{
		wv = wrapper.idmap.getVertex( obj.getInternalPoolIndex(), wv );
		return this;
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getFloatPosition( d );
	}

	@Override
	public void getCovariance( final double[][] mat )
	{
		overlayProperties.getCovariance( wv, mat );
	}

	@Override
	public double getBoundingSphereRadiusSquared()
	{
		return overlayProperties.getBoundingSphereRadiusSquared( wv );
	}

	@Override
	public boolean isSelected()
	{
		return overlayProperties.isVertexSelected( wv );
	}

	@Override
	public int getTimepoint()
	{
		return overlayProperties.getTimepoint( wv );
	}

	@Override
	public Edges< OverlayEdgeWrapper< V, E > > incomingEdges()
	{
		return incomingEdges;
	}


	@Override
	public Edges< OverlayEdgeWrapper< V, E > > outgoingEdges()
	{
		return outgoingEdges;
	}


	@Override
	public Edges< OverlayEdgeWrapper< V, E > > edges()
	{
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
		return obj instanceof OverlayVertexWrapper< ?, ? > &&
				wv.equals( ( ( OverlayVertexWrapper< ?, ? > ) obj ).wv );
	}

	private class EdgesWrapper implements Edges< OverlayEdgeWrapper< V, E > >
	{
		private final Edges< E > edges;

		private OverlayEdgeIteratorWrapper< V, E > iterator = null;

		public EdgesWrapper( final Edges< E > edges )
		{
			this.edges = edges;
		}

		@Override
		public Iterator< OverlayEdgeWrapper< V, E > > iterator()
		{
			if ( iterator == null )
				iterator = new OverlayEdgeIteratorWrapper< V, E >( wrapper, wrapper.edgeRef(), edges.iterator() );
			else
				iterator.wrap( edges.iterator() );
			return iterator;
		}

		@Override
		public int size()
		{
			return edges.size();
		}

		@Override
		public boolean isEmpty()
		{
			return edges.isEmpty();
		}

		@Override
		public OverlayEdgeWrapper< V, E > get( final int i )
		{
			return get( i, wrapper.edgeRef() );
		}

		@Override
		public OverlayEdgeWrapper< V, E > get( final int i, final OverlayEdgeWrapper< V, E > edge )
		{
			edge.we = edges.get( i, edge.we );
			return edge;
		}

		@Override
		public Iterator< OverlayEdgeWrapper< V, E > > safe_iterator()
		{
			return new OverlayEdgeIteratorWrapper< V, E >( wrapper, wrapper.edgeRef(), edges.iterator() );
		}
	}

	// === RealLocalizable ===

	@Override
	public void localize( final double[] position )
	{
		overlayProperties.localize( wv, position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) overlayProperties.getDoublePosition( wv, d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return overlayProperties.getDoublePosition( wv, d );
	}

	@Override
	public int numDimensions()
	{
		return n;
	}

	// === RealPositionable ===

	@Override
	public void setPosition( final double[] position )
	{
		overlayProperties.setPosition( wv, position );
	}

	@Override
	public void setPosition( final double position, final int d )
	{
		overlayProperties.setPosition( wv, position, d );
	}

	// TODO: (almost?) all of the following should have default implementations in the RealPositionable interface
	@Override
	public void move( final float distance, final int d )
	{
		setPosition( getDoublePosition( d ) + distance, d );
	}

	@Override
	public void move( final double distance, final int d )
	{
		setPosition( getDoublePosition( d ) + distance, d );
	}

	@Override
	public void move( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( getDoublePosition( d ) + localizable.getDoublePosition( d ), d );
	}

	@Override
	public void move( final float[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( getDoublePosition( d ) + distance[ d ], d );
	}

	@Override
	public void move( final double[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( getDoublePosition( d ) + distance[ d ], d );
	}

	@Override
	public void setPosition( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( localizable.getDoublePosition( d ), d );
	}

	@Override
	public void setPosition( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( position[ d ], d );
	}

	@Override
	public void setPosition( final float position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public void setPosition( final long position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public void setPosition( final int position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		move( ( RealLocalizable ) localizable );
	}

	@Override
	public void setPosition( final int[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( ( double ) position[ d ], d );
	}

	@Override
	public void setPosition( final long[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( ( double ) position[ d ], d );
	}

	@Override
	public void fwd( final int d )
	{
		move( 1, d );
	}

	@Override
	public void bck( final int d )
	{
		move( -1, d );
	}

	@Override
	public void move( final int distance, final int d )
	{
		setPosition( getDoublePosition( d ) + distance, d );
	}

	@Override
	public void move( final long distance, final int d )
	{
		setPosition( getDoublePosition( d ) + distance, d );
	}

	@Override
	public void move( final Localizable localizable )
	{
		move( ( RealLocalizable ) localizable );
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( getDoublePosition( d ) + distance[ d ], d );
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( getDoublePosition( d ) + distance[ d ], d );
	}
}
