package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;


public class TrackSchemeHighlight
{
	private final ModelHighlightProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeHighlight(
			final ModelHighlightProperties props,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	/**
	 * Get the {@link TrackSchemeVertex} that is currently highlighted. Forwards
	 * to the model {@link HighlightModel}.
	 *
	 * @return currently highlighted vertex.
	 */
	public TrackSchemeVertex getHighlightedVertex( final TrackSchemeVertex ref )
	{
		return graph.getTrackSchemeVertexForModelId( props.getHighlightedVertexId(), ref );
	}

	/**
	 * highlight vertex.
	 *
	 * @param v
	 *            vertex to highlight, or {@code null} to clear highlight.
	 */
	// TODO: rename notifyHighlightVertex ?
	public void highlightVertex( final TrackSchemeVertex v )
	{
		props.highlightVertex( ( v == null ) ? -1 : v.getModelVertexId() );
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
