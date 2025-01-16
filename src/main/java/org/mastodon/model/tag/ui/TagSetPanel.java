/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.model.tag.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.MatteBorder;

import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.scijava.listeners.Listeners;

public class TagSetPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final TagTable< TagSetStructure, TagSet > tagSetTable;

	private final ColorTagTable< TagSet, Tag > tagTable;

	public interface UpdateListener
	{
		void modelUpdated();
	}

	private final TagSetStructure tagSetStructure;

	private final Listeners.List< UpdateListener > updateListeners;

	public TagSetPanel()
	{
		this( new TagSetStructure() );
	}

	public TagSetPanel( final TagSetStructure tagSetStructure )
	{
		super( new BorderLayout( 0, 0 ) );
		setPreferredSize( new Dimension( 500, 200 ) );

		this.tagSetStructure = tagSetStructure;
		updateListeners = new Listeners.SynchronizedList<>();

		tagSetTable = new TagTable<>( tagSetStructure,
				tss -> tss.createTagSet( makeNewName( "Tag set" ) ),
				tss -> tss.getTagSets().size(),
				TagSetStructure::remove,
				( tss, i ) -> tss.getTagSets().get( i ),
				TagSet::setName,
				TagSet::getName );

		tagTable = new ColorTagTable<>( null,
				ts -> ts.createTag( "Tag", colorGenerator.next() ),
				ts -> ts.getTags().size(),
				TagSet::removeTag,
				( ts, i ) -> ts.getTags().get( i ),
				Tag::setLabel,
				Tag::label,
				( t, color ) -> t.setColor( color.getRGB() ),
				t -> new Color( t.color(), true ) );

		tagSetTable.updateListeners().add( this::notifyListeners );
		tagTable.updateListeners().add( this::notifyListeners );
		tagSetTable.selectionListeners().add( tagTable::setElements );

		final JSplitPane splitPane =
				new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, tagSetTable.getTable(), tagTable.getTable() );
		splitPane.setResizeWeight( 0 );
		splitPane.setContinuousLayout( true );
		splitPane.setDividerSize( 10 );
		splitPane.setDividerLocation( 220 );
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

	/**
	 * If somthing is currently being edited, cancel editing.
	 */
	public void cancelEditing()
	{
		tagSetTable.cancelEditing();
		tagTable.cancelEditing();
	}

	/**
	 * If somthing is currently being edited, stop editing.
	 */
	public void stopEditing()
	{
		tagSetTable.stopEditing();
		tagTable.stopEditing();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	private void notifyListeners()
	{
		updateListeners.list.forEach( UpdateListener::modelUpdated );
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

	private final Iterator< Integer > colorGenerator = new Iterator< Integer >()
	{
		private final Random ran = new Random();

		@Override
		public boolean hasNext()
		{
			return true;
		}

		@Override
		public Integer next()
		{
			return ran.nextInt() | 0xFF000000;
		}
	};
}
