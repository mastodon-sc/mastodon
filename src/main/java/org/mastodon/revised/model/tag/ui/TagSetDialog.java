package org.mastodon.revised.model.tag.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SimpleSettingsPage;
import org.mastodon.app.ui.settings.SingleSettingsPanel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class TagSetDialog extends JDialog
{
	public static class TagSetManager
	{
		private final TagSetStructure tagSetStructure;

		public TagSetManager()
		{
			this.tagSetStructure = new TagSetStructure();
		}

		public TagSetStructure getTagSetStructure()
		{
			return tagSetStructure;
		}

		public void setTagSetStructure( final TagSetStructure tagSetStructure )
		{
			this.tagSetStructure.set( tagSetStructure );
		}
	}

	private static final long serialVersionUID = 1L;

	public TagSetDialog( final Frame owner, final TagSetManager manager )
	{
		super( owner, "Configure Tag Sets", false );

		final TagSetPanel tagSetPanel = new TagSetPanel();
		tagSetPanel.setTagSetStructure( manager.getTagSetStructure() );
		final SimpleSettingsPage page = new SimpleSettingsPage( "tag sets", tagSetPanel );

		tagSetPanel.addUpdateListener(() -> page.notifyModified());
		page.onApply( () -> manager.setTagSetStructure( tagSetPanel.getTagSetStructure() ) );
		page.onCancel( () -> tagSetPanel.setTagSetStructure( manager.getTagSetStructure() ) );

		final SingleSettingsPanel settingsPanel = new SingleSettingsPanel( page );
		settingsPanel.onOk( () -> setVisible( false ) );
		settingsPanel.onCancel( () -> setVisible( false ) );

		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settingsPanel.cancel();
			}
		} );

		getContentPane().add( settingsPanel, BorderLayout.CENTER );
		pack();
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

		final TagSetManager manager = new TagSetManager();
		manager.setTagSetStructure( tss );

		final TagSetDialog frame = new TagSetDialog( null, manager );
		frame.setVisible( true );
	}
}
