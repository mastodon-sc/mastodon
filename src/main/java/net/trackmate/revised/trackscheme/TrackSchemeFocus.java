package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.FocusListener;


public class TrackSchemeFocus
{
	private final ModelFocusProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeFocus( final ModelFocusProperties props, final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	/**
	 * Get internal pool index of {@link TrackSchemeVertex} that is currently
	 * highlighted. Forwards to the model {@link FocusModel}.
	 *
	 * @return internal id of {@link TrackSchemeVertex} that is currently
	 *         focused.
	 */
	public int getFocusedVertexId()
	{
		final int mid = props.getFocusedVertexId();
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
	 * @param trackSchemeVertexId
	 *            internal pool index of TrackSchemeVertex to focus on or
	 *            {@code <0} to clear focus.
	 */
	public void focusVertex( final int trackSchemeVertexId )
	{
		if ( trackSchemeVertexId < 0 )
		{
			props.focusVertex( -1 );
		}
		else
		{
			final TrackSchemeVertex v = graph.vertexRef();
			graph.getVertexPool().getByInternalPoolIndex( trackSchemeVertexId, v );
			props.focusVertex( v.getModelVertexId() );
			graph.releaseRef( v );
		}
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
