package org.mastodon.revised.trackscheme;

import org.mastodon.revised.ui.selection.FocusListener;
import org.mastodon.revised.ui.selection.FocusModel;


public class TrackSchemeFocus
{
	private final ModelFocusProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeFocus(
			final ModelFocusProperties props,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	/**
	 * Gets the {@link TrackSchemeVertex} that is currently focused. Forwards to
	 * the model {@link FocusModel}.
	 *
	 * @param ref
	 *            a pool reference used for retrieval.
	 *
	 * @return currently focused vertex.
	 */
	public TrackSchemeVertex getFocusedVertex( final TrackSchemeVertex ref )
	{
		return graph.getTrackSchemeVertexForModelId( props.getFocusedVertexId(), ref );
	}

	/**
	 * Focus the specified vertex.
	 *
	 * @param v
	 *            vertex to focus on, or {@code null} to clear focus.
	 */
	// TODO: rename notifyFocusVertex ?
	public void focusVertex( final TrackSchemeVertex v )
	{
		props.focusVertex( ( v == null ) ? -1 : v.getModelVertexId() );
	}

	public boolean addFocusListener( final FocusListener l )
	{
		return props.addFocusListener( l );
	}

	public boolean removeFocusListener( final FocusListener l )
	{
		return props.removeFocusListener( l );
	}
}
