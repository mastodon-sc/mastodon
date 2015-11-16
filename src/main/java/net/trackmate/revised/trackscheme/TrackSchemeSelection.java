package net.trackmate.revised.trackscheme;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.ui.selection.SelectionListener;

public class TrackSchemeSelection
{
	private final ModelSelectionProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeSelection( final ModelSelectionProperties props, final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	// TODO unused. remove?
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

	// TODO unused. remove?
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

	// TODO unused. remove?
	public void setSelected( final TrackSchemeVertex vertex, final boolean selected )
	{
		final int id = vertex.getModelVertexId();
		props.setVertexSelected( id, selected );
	}

	public void setVertexSelected( final int id, final boolean selected )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		props.setVertexSelected( ref.getModelVertexId(), selected );
		graph.releaseRef( ref );
	}

	public void setEdgeSelected( final int id, final boolean selected )
	{
		final TrackSchemeEdge ref = graph.edgeRef();
		graph.getEdgePool().getByInternalPoolIndex( id, ref );
		props.setEdgeSelected( ref.getModelEdgeId(), selected );
		graph.releaseRef( ref );
	}

	public void toggleVertex( final int id )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		props.toggleVertexSelected( ref.getModelVertexId() );
		graph.releaseRef( ref );
	}

	// TODO unused. remove?
	public boolean isVertexSelected( final int id )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final int modelVertexId = ref.getModelVertexId();
		graph.releaseRef( ref );
		return props.isVertexSelected( modelVertexId );
	}

	// TODO unused. remove?
	public boolean isEdgeSelected( final int id )
	{
		final TrackSchemeEdge ref = graph.edgeRef();
		graph.getEdgePool().getByInternalPoolIndex( id, ref );
		final int modelEdgeId = ref.getModelEdgeId();
		graph.releaseRef( ref );
		return props.isEdgeSelected( modelEdgeId );
	}

	public void clearSelection()
	{
		props.clearSelection();
	}

	// TODO unused. remove?
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
