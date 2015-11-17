package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlaySelection;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class OverlaySelectionWrapper< V extends Vertex< E >, E extends Edge< V > >
		implements OverlaySelection< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	private final Selection< V, E > wrappedSelectionModel;

	public OverlaySelectionWrapper(	final Selection< V, E > selection )
	{
		this.wrappedSelectionModel = selection;
	}

	@Override
	public void setSelected( final OverlayVertexWrapper< V, E > vertex, final boolean selected )
	{
		wrappedSelectionModel.setSelected( vertex.wv, selected );
	}

	@Override
	public void setSelected( final OverlayEdgeWrapper< V, E > edge, final boolean selected )
	{
		wrappedSelectionModel.setSelected( edge.we, selected );
	}

	@Override
	public void toggleSelected( final OverlayVertexWrapper< V, E > vertex )
	{
		wrappedSelectionModel.toggle( vertex.wv );
	}

	@Override
	public void toggleSelected( final OverlayEdgeWrapper< V, E > edge )
	{
		wrappedSelectionModel.toggle( edge.we );
	}

	@Override
	public boolean isVertexSelected( final OverlayVertexWrapper< V, E > vertex )
	{
		return wrappedSelectionModel.isSelected( vertex.wv );
	}

	@Override
	public boolean isEdgeSelected( final OverlayEdgeWrapper< V, E > edge )
	{
		return wrappedSelectionModel.isSelected( edge.we );
	}

	@Override
	public void clearSelection()
	{
		wrappedSelectionModel.clearSelection();
	}

	@Override
	public boolean addSelectionListener( final SelectionListener l )
	{
		return wrappedSelectionModel.addSelectionListener( l );
	}

	@Override
	public boolean removeSelectionListener( final SelectionListener l )
	{
		return wrappedSelectionModel.removeSelectionListener( l );
	}
}
