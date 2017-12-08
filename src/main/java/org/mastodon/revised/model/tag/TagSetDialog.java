package org.mastodon.revised.model.tag;

import java.awt.Frame;

import javax.swing.JDialog;

public class TagSetDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	public TagSetDialog( final Frame owner, final TagSetModel< ?, ? > tagSetModel )
	{
		super( owner, "tag sets configuration", false );
		final TagSetPanel tagSetPanel = new TagSetPanel( tagSetModel );
		getContentPane().add( tagSetPanel );
		setSize( 300, 300 );
	}
}
