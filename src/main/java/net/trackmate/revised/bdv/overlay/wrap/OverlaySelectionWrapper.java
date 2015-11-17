package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.bdv.overlay.OverlaySelection;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class OverlaySelectionWrapper< V extends Vertex< E >, E extends Edge< V > >
		implements OverlaySelection< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{

	private final GraphIdBimap< V, E > idmap;

	private final Selection< V, E > wrappedSelectionModel;

	private final OverlayGraphWrapper< V, E > graph;

	public OverlaySelectionWrapper(
			final OverlayGraphWrapper< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final Selection< V, E > selection )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.wrappedSelectionModel = selection;
	}

	@Override
	public RefSet< OverlayVertexWrapper< V, E >> getSelectedVertices(final OverlayVertexWrapper< V, E > ref)
	{
		final RefSet< V > vertices = wrappedSelectionModel.getSelectedVertices();
		final RefSet< OverlayVertexWrapper< V, E >> svs = CollectionUtils.createVertexSet( graph, vertices.size() );
		for ( final V v : vertices )
		{
			ref.wv = idmap.getVertex( idmap.getVertexId( v ), ref.wv );
			svs.add( ref );
		}
		return svs;
	}

	@Override
	public RefSet< OverlayEdgeWrapper< V, E >> getSelectedEdges( final OverlayEdgeWrapper< V, E > ref )
	{
		final RefSet< E > edges = wrappedSelectionModel.getSelectedEdges();
		final RefSet< OverlayEdgeWrapper< V, E >> ses = CollectionUtils.createEdgeSet( graph, edges.size() );
		for ( final E e : edges )
		{
			ref.we = idmap.getEdge( idmap.getEdgeId( e ), ref.we );
			ses.add( ref );
		}
		return ses;
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
