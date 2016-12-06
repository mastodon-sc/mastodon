package org.mastodon.model;

public interface MastodonView< V, E >
		extends FocusListener
{
	void setFocusReceiver( FocusReceiver< V > focusReceiver );

	void setFocusState( FocusState< V > focusState );
}
