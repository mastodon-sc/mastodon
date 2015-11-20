package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.SelectionListener;

/*
 * TODO: consider replacing/adding API to use TrackSchemeVertex instead of IDs, as discussed in email:
 *
 * ModelSelectionProperties informs TrackSchemeSelection, which translates IDs
 * it to corresponding TrackSchemeVertex and TrackSchemeEdge. This is the place
 * to change it: Instead of
 *    public void setEdgeSelected( final int id, final boolean selected )
 * TrackSchemeSelection should have
 *    public void setEdgeSelected( final TrackSchemeEdge edge, final boolean selected ) etc.
 */
public class TrackSchemeSelection
{
	private final ModelSelectionProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeSelection( final ModelSelectionProperties props, final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
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

	public void toggleEdge( final int id )
	{
		final TrackSchemeEdge ref = graph.edgeRef();
		graph.getEdgePool().getByInternalPoolIndex( id, ref );
		props.toggleEdgeSelected( ref.getModelEdgeId() );
		graph.releaseRef( ref );
	}

	public void clearSelection()
	{
		props.clearSelection();
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
