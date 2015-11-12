package net.trackmate.revised.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.spatial.HasTimepoint;


public class TrackSchemeHighlight<
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
{
	private final ModelHighlightProperties props;

	private final TrackSchemeGraph< V, E > graph;

	public TrackSchemeHighlight( final ModelHighlightProperties props, final TrackSchemeGraph< V, E > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	/**
	 * Get internal pool index of {@link TrackSchemeVertex} that is currently
	 * highlighted. Forwards to the model {@link HighlightModel}.
	 *
	 * @return internal id of {@link TrackSchemeVertex} that is currently
	 *         highlighted.
	 */
	public int getHighlightedVertexId()
	{
		final int mid = props.getHighlightedVertexId();
		if ( mid < 0 )
			return -1;
		else
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			final TrackSchemeVertex v = graph.getTrackSchemeVertexForModelId( mid, ref );
			final int id = ( v == null ) ? -1 : v.getInternalPoolIndex();
			graph.releaseRef( ref );
			return id;
		}
	}

	/**
	 *
	 * @param trackSchemeVertexId internal pool index of TrackSchemeVertex to highlight or {@code <0} to clear highlight.
	 */
	public void highlightVertex( final int trackSchemeVertexId )
	{
		if ( trackSchemeVertexId < 0 )
		{
			props.highlightVertex( -1 );
		}
		else
		{
			final TrackSchemeVertex v = graph.vertexRef();
			graph.getVertexPool().getByInternalPoolIndex( trackSchemeVertexId, v );
			props.highlightVertex( v.getModelVertexId() );
			graph.releaseRef( v );
		}
	}

	public boolean addHighlightListener( final HighlightListener l )
	{
		return props.addHighlightListener( l );
	}

	public boolean removeHighlightListener( final HighlightListener l )
	{
		return props.removeHighlightListener( l );
	}
}
