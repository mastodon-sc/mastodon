package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayVertex;
import net.trackmate.revised.ui.selection.Selection;

public class OverlayVertexWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayVertex< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > wrapper;

	V wv;

	private final EdgesWrapper incomingEdges;

	private final EdgesWrapper outgoingEdges;

	private final EdgesWrapper edges;

	private final OverlayProperties< V > overlayProperties;

	private final Selection< V, E > selection;

	OverlayVertexWrapper( final OverlayGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		wv = wrapper.wrappedGraph.vertexRef();
		incomingEdges = new EdgesWrapper( wv.incomingEdges() );
		outgoingEdges = new EdgesWrapper( wv.outgoingEdges() );
		edges = new EdgesWrapper( wv.edges() );
		overlayProperties = wrapper.overlayProperties;
		selection = wrapper.selection;
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
		overlayProperties.localize( wv, position );
	}

	@Override
	public void localize( final double[] position )
	{
		overlayProperties.localize( wv, position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return overlayProperties.getFloatPosition( wv, d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return overlayProperties.getDoublePosition( wv, d );
	}

	@Override
	public int numDimensions()
	{
		return overlayProperties.numDimensions( wv );
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
		return selection.isSelected( wv );
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
}
