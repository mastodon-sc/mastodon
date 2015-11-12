package net.trackmate.revised.trackscheme;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import net.trackmate.graph.Edge;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.ui.selection.SelectionListener;
import net.trackmate.spatial.HasTimepoint;

public class TrackSchemeSelection< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
{
	private final ModelSelectionProperties props;

	private final TrackSchemeGraph< V, E > graph;

	public TrackSchemeSelection( final ModelSelectionProperties props, final TrackSchemeGraph< V, E > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	public RefSet< TrackSchemeVertex > getSelectedVertices()
	{
		final TIntSet verticesIds = props.getSelectedVerticesIds();
		final PoolObjectSet< TrackSchemeVertex > vertexSet = graph.createVertexSet( verticesIds.size() );

		final TrackSchemeVertex ref = graph.vertexRef();
		final TIntIterator it = verticesIds.iterator();
		while ( it.hasNext() )
		{
			final int id = it.next();
			final TrackSchemeVertex tsv = graph.getTrackSchemeVertexForModelId( id, ref );
			vertexSet.add( tsv );
		}
		graph.releaseRef( ref );
		return vertexSet;
	}

	public RefSet< TrackSchemeEdge > getSelectedEdges()
	{
		final TIntSet edgesIds = props.getSelectedEdgesIds();
		final PoolObjectSet< TrackSchemeEdge > edgeSet = graph.createEdgeSet( edgesIds.size() );

		final TrackSchemeEdge ref = graph.edgeRef();
		final TIntIterator it = edgesIds.iterator();
		while ( it.hasNext() )
		{
			final int id = it.next();
			final TrackSchemeEdge tse = graph.getTrackSchemeEdgeForModelId( id, ref );
			edgeSet.add( tse );
		}
		graph.releaseRef( ref );
		return edgeSet;
	}

	public void setSelected( final TrackSchemeVertex vertex, final boolean selected )
	{
		final int id = vertex.getModelVertexId();
		props.setVertexSelected( id, selected );
	}

	public void setSelected( final TrackSchemeEdge edge, final boolean selected )
	{
		final int id = edge.getModelEdgeId();
		props.setEdgeSelected( id, selected );
	}

	public boolean addSelectionListener( final SelectionListener l )
	{
		return props.addSelectionListener( l );
	}

	public boolean removeSelectionListener( final SelectionListener l )
	{
		return props.removeSelectionListener( l );
	}
}
