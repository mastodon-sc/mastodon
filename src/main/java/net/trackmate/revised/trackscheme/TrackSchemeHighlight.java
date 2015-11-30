package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;


public class TrackSchemeHighlight
{
	private final ModelHighlightProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeHighlight( final ModelHighlightProperties props, final TrackSchemeGraph< ?, ? > graph )
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
