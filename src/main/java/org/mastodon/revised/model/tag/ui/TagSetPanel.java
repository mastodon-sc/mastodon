package org.mastodon.revised.model.tag.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.MatteBorder;

import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class TagSetPanel extends JPanel
{
	private final TagTable< TagSetStructure, TagSet > tagSetTable;

	public interface UpdateListener
	{
		void modelUpdated();
	}

	private final TagSetStructure tagSetStructure;

	private final ArrayList< UpdateListener > updateListeners;

	public TagSetPanel()
	{
		this( new TagSetStructure() );
	}

	public TagSetPanel( final TagSetStructure tagSetStructure )
	{
		super( new BorderLayout( 0, 0 ) );

		this.tagSetStructure = tagSetStructure;
		updateListeners = new ArrayList<>();

		tagSetTable = new TagTable<>( tagSetStructure,
				tss -> tss.createTagSet( makeNewName( "Tag set" ) ),
				tss -> tss.getTagSets().size(),
				TagSetStructure::remove,
				(tss, i) -> tss.getTagSets().get( i ),
				TagSet::setName,
				TagSet::getName );

		final ColorTagTable< TagSet, Tag > tagTable = new ColorTagTable<>( null,
				ts -> ts.createTag( "Tag", colorGenerator.next() ),
				ts -> ts.getTags().size(),
				TagSet::removeTag,
				(ts, i) -> ts.getTags().get( i ),
				Tag::setLabel,
				Tag::label,
				Tag::setColor,
				Tag::color );

		tagSetTable.addUpdateListener( this::notifyListeners );
		tagTable.addUpdateListener( this::notifyListeners );
		tagSetTable.addSelectionListener( tagTable::setElements );

		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, tagSetTable.getTable(), tagTable.getTable() );
		splitPane.setResizeWeight( 0 );
		splitPane.setContinuousLayout( true );
		splitPane.setDividerSize( 10 );
		splitPane.setDividerLocation( 300 );
		splitPane.setBorder( new MatteBorder( 0, 0, 1, 0, Color.LIGHT_GRAY ) );
//		splitPane.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		this.add( splitPane, BorderLayout.CENTER );
	}

	public TagSetStructure getTagSetStructure()
	{
		return tagSetStructure;
	}

	public void setTagSetStructure( final TagSetStructure tss )
	{
		tagSetStructure.set( tss );
		tagSetTable.setElements( tagSetStructure );
	}

	public synchronized boolean addUpdateListener( final UpdateListener l )
	{
		if ( !updateListeners.contains( l ) )
		{
			updateListeners.add( l );
			return true;
		}
		return false;
	}

	public synchronized boolean removeUpdateListener( final UpdateListener l )
	{
		return updateListeners.remove( l );
	}

	private void notifyListeners()
	{
		updateListeners.forEach( UpdateListener::modelUpdated );
	}

	private final String makeNewName( final String prefix )
	{
		int n = 0;
		String newName;
		INCREMENT: while ( true )
		{
			newName = prefix + " (" + ( ++n ) + ")";
			for ( int j = 0; j < tagSetStructure.getTagSets().size(); j++ )
			{
				if ( tagSetStructure.getTagSets().get( j ).getName().equals( newName ) )
					continue INCREMENT;
			}
			break;
		}
		return newName;
	}

	private final Iterator< Color > colorGenerator = new Iterator< Color >()
	{
		private final Random ran = new Random();

		@Override
		public boolean hasNext()
		{
			return true;
		}

		@Override
		public Color next()
		{
			return new Color( ran.nextInt() );
		}
	};
}
