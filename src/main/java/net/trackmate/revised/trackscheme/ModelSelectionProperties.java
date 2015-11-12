package net.trackmate.revised.trackscheme;

import gnu.trove.set.TIntSet;
import net.trackmate.revised.ui.selection.SelectionListener;


public interface ModelSelectionProperties
{
	public TIntSet getSelectedVerticesIds();

	public TIntSet getSelectedEdgesIds();

	public void setVertexSelected( int vertexId, boolean selected );

	public void setEdgeSelected( int edgeId, boolean selected );

	public boolean isVertexSelected( int vertexId );

	public boolean isEdgeSelected( int edgeId );

	public boolean addSelectionListener( final SelectionListener l );

	public boolean removeSelectionListener( final SelectionListener l );
}
