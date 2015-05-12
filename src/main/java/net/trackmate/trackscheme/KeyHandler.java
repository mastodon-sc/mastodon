package net.trackmate.trackscheme;

import javax.swing.KeyStroke;

public class KeyHandler
{
	private final ShowTrackScheme trackscheme;

	public KeyHandler( ShowTrackScheme trackscheme )
	{
		this.trackscheme = trackscheme;
		install();
	}

	private void install()
	{
		final KeyStroke ks = KeyStroke.getKeyStroke( 'd' );
		registerAction( ks, ActionBank.getDeleteSelectionAction( trackscheme ) );
	}

	private void registerAction( KeyStroke ks, AbstractNamedAction action )
	{
		trackscheme.canvas.getInputMap().put( ks, action.name() );
		AbstractNamedAction.put( trackscheme.canvas.getActionMap(), action );
	}


}
