package org.mastodon.revised.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.HighlightListener;
import org.mastodon.revised.ui.selection.HighlightModel;
import org.mastodon.spatial.HasTimepoint;


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
		final V ref = graph.vertexRef();
		final V highlighted = highlight.getHighlightedVertex( ref );
		final int id = ( highlighted == null ) ? -1 : idmap.getVertexId( highlighted );
		graph.releaseRef( ref );
		return id;
	}

	@Override
	public int getHighlightedEdgeId()
	{
		final E ref = graph.edgeRef();
		final E highlighted = highlight.getHighlightedEdge( ref );
		final int id = ( highlighted == null ) ? -1 : idmap.getEdgeId( highlighted );
		graph.releaseRef( ref );
		return id;
	}

	@Override
	public void highlightVertex( final int id )
	{
		if ( id < 0 )
			highlight.clearHighlight();
		else
		{
			final V ref = graph.vertexRef();
			highlight.highlightVertex( idmap.getVertex( id, ref ) );
			graph.releaseRef( ref );
		}
	}

	@Override
	public void highlightEdge( final int id )
	{
		if ( id < 0 )
			highlight.clearHighlight();
		else
		{
			final E ref = graph.edgeRef();
			highlight.highlightEdge( idmap.getEdge( id, ref ) );
			graph.releaseRef( ref );
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
