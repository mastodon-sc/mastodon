package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.SelectionListener;

public class TrackSchemeSelection
{
	private final ModelSelectionProperties props;

	public TrackSchemeSelection( final ModelSelectionProperties props )
	{
		this.props = props;
	}

	public void setSelected( final TrackSchemeVertex v, final boolean selected )
	{
		props.setVertexSelected( v.getModelVertexId(), selected );
	}

	public void setSelected( final TrackSchemeEdge e, final boolean selected )
	{
		props.setEdgeSelected( e.getModelEdgeId(), selected );
	}

	public void toggleSelected( final TrackSchemeVertex v )
	{
		props.toggleVertexSelected( v.getModelVertexId() );
	}

	public void toggleSelected( final TrackSchemeEdge e )
	{
		props.toggleEdgeSelected( e.getModelEdgeId() );
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

	@Override
	public String toString()
	{
		return super.toString() + "\n:" + props.toString();
	}

	public void resumeListeners()
	{
		props.resumeListeners();
	}

	public void pauseListeners()
	{
		props.pauseListeners();
	}
}
