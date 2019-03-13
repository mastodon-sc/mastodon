package org.mastodon.tomancak;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTags;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class CopyTagDialog extends JDialog
{
	private final TagSelectionPanel tspFrom;

	private final TagSelectionPanel tspTo;

	private final Model model;

	public CopyTagDialog( final Frame owner, final Model model )
	{
		super( owner, "Copy Tag...", false );
		this.model = model;

		model.getTagSetModel().listeners().add( this::tagSetStructureChanged );
		tspFrom = new TagSelectionPanel();
		tspTo = new TagSelectionPanel();
		tagSetStructureChanged();

		final JPanel content = new JPanel();
		content.setLayout( new GridBagLayout() );
		content.setBorder( BorderFactory.createEmptyBorder( 30, 20, 20, 20 ) );

		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		content.add( new JLabel( "If has tag " ), c );
		++c.gridx;
		content.add( tspFrom, c );
		++c.gridx;
		content.add( new JLabel( " then also assign " ), c );
		++c.gridx;
		content.add( tspTo, c );

		final JPanel buttons = new JPanel();
		final JButton closeButton = new JButton( "Done" );
		closeButton.addActionListener( e -> close() );
		final JButton copyTagButton = new JButton( "Copy Tag" );
		copyTagButton.addActionListener( e -> copyTag() );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( closeButton );
		buttons.add( copyTagButton );

		getContentPane().add( content, BorderLayout.CENTER );
		getContentPane().add( buttons, BorderLayout.SOUTH );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				close();
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}

	private void tagSetStructureChanged()
	{
		final TagSetStructure tss = model.getTagSetModel().getTagSetStructure();
		tspFrom.refreshTagSets( tss );
		tspTo.refreshTagSets( tss );
		pack();
	}

	private void close()
	{
		setVisible( false );
	}

	private void copyTag()
	{
		final Tag from = tspFrom.getSelected();
		final Tag to = tspTo.getSelected();
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.readLock().lock();
		try
		{
			final TagSetModel< Spot, Link > tsm = model.getTagSetModel();
			final ObjTags< Spot > vertexTags = tsm.getVertexTags();
			for ( Spot spot : vertexTags.getTaggedWith( from ) )
				vertexTags.set( spot, to );
			final ObjTags< Link > edgeTags = tsm.getEdgeTags();
			for ( Link link : edgeTags.getTaggedWith( from ) )
				edgeTags.set( link, to );
			model.setUndoPoint();
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	static class Item< T >
	{
		private final T item;
		private final String label;

		public Item( final T item, final String label )
		{
			this.item = item;
			this.label = label;
		}

		public T get()
		{
			return item;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	static class TagSelectionPanel extends JPanel
	{
		private final JComboBox< Item< TagSet > > cbTagSets;
		private final JComboBox< Item< Tag > > cbTags;

		TagSelectionPanel( )
		{
			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );

			this.cbTagSets = new JComboBox<>();
			cbTagSets.addItemListener( e -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
					refreshTags();
			} );
			add( cbTagSets );


			add( new JLabel( "-" ) );

			cbTags = new JComboBox<>();
			add( cbTags );

			refreshTagSets( null );
		}

		private void refreshTagSets( final TagSetStructure tss )
		{
			if ( tss == null )
			{
				cbTagSets.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				final Item< TagSet >[] tagSetItems = tss.getTagSets().stream().map( tagset -> new Item<>( tagset, tagset.getName() ) ).toArray( Item[]::new );
				cbTagSets.setModel( new DefaultComboBoxModel<>( tagSetItems ) );
				cbTagSets.setSelectedIndex( 0 );
			}
			refreshTags();
		}

		private void refreshTags()
		{
			final Item< TagSet > tagSetItem = ( Item< TagSet > ) cbTagSets.getSelectedItem();
			if ( tagSetItem == null )
			{
				cbTags.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				final TagSet ts = tagSetItem.get();
				final Item< Tag >[] tagItems = ts.getTags().stream().map( tag -> new Item<>( tag, tag.label() ) ).toArray( Item[]::new );
				cbTags.setModel( new DefaultComboBoxModel<>( tagItems ) );
			}
		}

		void setTagSetStructure( final TagSetStructure tss )
		{
			refreshTagSets( tss );
		}

		Tag getSelected()
		{
			final Item< Tag > tagItem = ( Item< Tag > ) cbTags.getSelectedItem();
			return tagItem == null ? null : tagItem.get();
		}
	}


	public static final String basepath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/";

	public static final String[] paths = {
			basepath + "1.SimView2_20130315_Mastodon_Automat-segm-t0-t300",
			basepath + "2.SimView2_20130315_Mastodon_MHT",
			basepath + "3.Pavel manual",
			basepath + "4.Vlado_TrackingPlatynereis",
			basepath + "5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG"
	};

	public static void main( String[] args ) throws IOException
	{
		final String path = paths[ 0 ];
		System.out.println( "path = " + path );
		final Dataset ds = new Dataset( path );
		new CopyTagDialog( null, ds.model() ).setVisible( true );
	}
}
