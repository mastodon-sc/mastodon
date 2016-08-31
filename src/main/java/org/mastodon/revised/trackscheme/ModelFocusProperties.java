package org.mastodon.revised.trackscheme;

import org.mastodon.revised.ui.selection.FocusListener;

public interface ModelFocusProperties
{
	public int getFocusedVertexId();

	public void focusVertex( final int id );

	public boolean addFocusListener( final FocusListener l );

	public boolean removeFocusListener( final FocusListener l );
}
