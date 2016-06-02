package net.trackmate.revised.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.spatial.HasTimepoint;


public class DefaultModelHighlightProperties<
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
	implements ModelHighlightProperties
{
	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final HighlightModel< V, E > highlight;

	public DefaultModelHighlightProperties(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final HighlightModel< V, E > highlight )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.highlight = highlight;
	}

	@Override
	public int getHighlightedVertexId()
	{
		final V v = graph.vertexRef();
		final V h = highlight.getHighlightedVertex( v );
		final int id = ( h == null ) ? -1 : idmap.getVertexId( h );
		graph.releaseRef( v );
		return id;
	}

	@Override
	public int getHighlightedEdgeId()
	{
		final E e = graph.edgeRef();
		final E h = highlight.getHighlightedEdge( e );
		final int id = ( h == null ) ? -1 : idmap.getEdgeId( h );
		graph.releaseRef( e );
		return id;
	}

	@Override
	public void highlightVertex( final int id )
	{
		if ( id < 0 )
			highlight.clearHighlight();
		else
		{
			final V v = graph.vertexRef();
			highlight.highlightVertex( idmap.getVertex( id, v ) );
			graph.releaseRef( v );
		}
	}

	@Override
	public void highlightEdge( final int id )
	{
		if ( id < 0 )
			highlight.clearHighlight();
		else
		{
			final E e = graph.edgeRef();
			highlight.highlightEdge( idmap.getEdge( id, e ) );
			graph.releaseRef( e );
		}
	}

	@Override
	public void clearHighlight()
	{
		highlight.clearHighlight();
	}

	@Override
	public boolean addHighlightListener( final HighlightListener l )
	{
		return highlight.addHighlightListener( l );
	}

	@Override
	public boolean removeHighlightListener( final HighlightListener l )
	{
		return highlight.removeHighlightListener( l );
	}
}
