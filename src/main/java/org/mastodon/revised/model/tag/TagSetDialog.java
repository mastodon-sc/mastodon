package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.awt.Frame;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class TagSetDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private final TagSetPanel tagSetPanel;

	public TagSetDialog( final Frame owner, final TagSetStructure tss )
	{
		super( owner, "tag sets configuration", false );
		this.tagSetPanel = new TagSetPanel( tss );
		getContentPane().add( tagSetPanel );
		setSize( 600, 300 );
	}

	public TagSetPanel getPanel()
	{
		return tagSetPanel;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final TagSetStructure tss = new TagSetStructure();

		final Random ran = new Random( 0l );
		final TagSet reviewedByTag = tss.createTagSet( "Reviewed by" );
		reviewedByTag.createTag( "Pavel", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "Mette", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "Tobias", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "JY", new Color( ran.nextInt() ) );
		final TagSet locationTag = tss.createTagSet( "Location" );
		locationTag.createTag( "Anterior", new Color( ran.nextInt() ) );
		locationTag.createTag( "Posterior", new Color( ran.nextInt() ) );

		final TagSetDialog frame = new TagSetDialog( null, tss );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLocationByPlatform( true );
		frame.getPanel().addUpdateListener( () -> System.out.println( tss ) );
		frame.setVisible( true );
	}
}
