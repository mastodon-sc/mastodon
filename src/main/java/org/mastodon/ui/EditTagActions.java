/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.util.NumberListeners;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.tools.bookmarks.BookmarksEditor;
import bdv.viewer.InteractiveDisplayCanvas;
import bdv.viewer.OverlayRenderer;

/**
 * <p>
 * Inspired from {@link BookmarksEditor}
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices to tag.
 * @param <E>
 *            the type of edges to tag.
 */
public class EditTagActions< V extends Vertex< E >, E extends Edge< V > >
		implements Runnable
{
	public static final String PICK_TAGS = "pick tags";

	private static final String[] PICK_TAGS_KEYS = new String[] { "Y" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( PICK_TAGS, PICK_TAGS_KEYS, "Assign a tag to the current selection." );
		}
	}

	private static final String PICK_TAGS_MAP = "pick-tags";

	private static final Font FONT = new Font( "SansSerif", Font.PLAIN, 12 );

	private final InputActionBindings actionBindings;

	private final TriggerBehaviourBindings behaviourBindings;

	private final TagSetModel< V, E > tagModel;

	private final SelectionModel< V, E > selectionModel;

	private final ReentrantReadWriteLock lock;

	private final Component panel;

	private final InteractiveDisplayCanvas renderer;

	private final UndoPointMarker undo;

	private final EditTagActions< V, E >.TagSelectionOverlay overlay;

	private final FontMetrics fontMetrics;

	private final ActionMap actionMap;

	private final InputMap inputMap;

	private final InputTriggerMap triggerMap;

	private Mode mode = Mode.INACTIVE;

	private TagSet tagSet;

	private EditTagActions(
			final InputActionBindings inputActionBindings,
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final TagSetModel< V, E > tagModel,
			final SelectionModel< V, E > selectionModel,
			final ReentrantReadWriteLock lock,
			final Component panel,
			final InteractiveDisplayCanvas renderer,
			final UndoPointMarker undoPointMarker )
	{
		this.actionBindings = inputActionBindings;
		this.behaviourBindings = triggerBehaviourBindings;
		this.tagModel = tagModel;
		this.selectionModel = selectionModel;
		this.lock = lock;
		this.panel = panel;
		this.renderer = renderer;
		this.undo = undoPointMarker;
		this.fontMetrics = panel.getFontMetrics( FONT );
		this.overlay = new TagSelectionOverlay();

		actionMap = new ActionMap();
		inputMap = new InputMap();
		triggerMap = new InputTriggerMap();
	}

	@Override
	public void run()
	{
		if ( mode != Mode.INACTIVE )
			return;

		inputMap.clear();
		actionMap.clear();

		// Abort.
		final KeyStroke abortKey = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
		final Action abortAction = new AbstractAction( "abort tags" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				abort();
			}

			private static final long serialVersionUID = 1L;
		};
		inputMap.put( abortKey, "abort tags" );
		actionMap.put( "abort tags", abortAction );

		// Clear all.
		final KeyStroke clearAllKey = KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK );
		final Action clearAllAction = new AbstractAction( "clear all labels" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				clearAllLabels();
			}

			private static final long serialVersionUID = 1L;
		};
		inputMap.put( clearAllKey, "clear all labels" );
		actionMap.put( "clear all labels", clearAllAction );

		// Do we have more than 9 tagsets?
		final boolean manyTagSets = tagModel.getTagSetStructure().getTagSets().size() > 9;

		// Prepare tag map.

		if ( manyTagSets )
		{
			NumberListeners.positiveIntegerListener( actionMap, inputMap, ( n ) -> {
				final int i = ( int ) n;
				if ( i > 0 && i <= tagModel.getTagSetStructure().getTagSets().size() )
				{
					selectTagSet( tagModel.getTagSetStructure().getTagSets().get( i - 1 ) );
				}
			} );
		}
		else
		{
			int i = 1;
			for ( final TagSet tagSet : tagModel.getTagSetStructure().getTagSets() )
			{
				final String actionName = "select tag " + i;
				final KeyStroke tagKeyStroke = KeyStroke.getKeyStroke( ( char ) ( '0' + i ) );
				final AbstractAction action = new AbstractAction( actionName )
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						selectTagSet( tagSet );
					}

					private static final long serialVersionUID = 1L;
				};

				inputMap.put( tagKeyStroke, actionName );
				actionMap.put( actionName, action );
				i++;
			}
		}
		actionBindings.addActionMap( PICK_TAGS_MAP, actionMap );
		actionBindings.addInputMap( PICK_TAGS_MAP, inputMap, "all" );
		behaviourBindings.addInputTriggerMap( PICK_TAGS_MAP, triggerMap, "all" );

		mode = Mode.PICK_TAGSET;
		renderer.overlays().add( overlay );
		panel.repaint();
	}

	private void abort()
	{
		done();
	}

	private synchronized void done()
	{
		mode = Mode.INACTIVE;
		actionBindings.removeActionMap( PICK_TAGS_MAP );
		actionBindings.removeInputMap( PICK_TAGS_MAP );
		behaviourBindings.removeInputTriggerMap( PICK_TAGS_MAP );
		renderer.overlays().remove( overlay );
		panel.repaint();
	}

	private synchronized void selectTagSet( final TagSet tagSet )
	{
		// Check if the selected tag still exists in the model.
		if ( null == tagSet || !tagModel.getTagSetStructure().getTagSets().contains( tagSet ) )
			return;

		// Prepare action and binding maps.
		inputMap.clear();
		actionMap.clear();

		// Cancel.
		final KeyStroke abortKey = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
		final Action abortAction = new AbstractAction( "abort tags" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				abort();
			}

			private static final long serialVersionUID = 1L;
		};
		inputMap.put( abortKey, "abort tags" );
		actionMap.put( "abort tags", abortAction );

		// Prepare tag map.

		// Do we have more than 9 tags?
		final boolean manyTags = tagSet.getTags().size() > 9;
		if ( manyTags )
		{
			panel.requestFocusInWindow();
			NumberListeners.positiveIntegerListener( actionMap, inputMap, ( n ) -> {
				final int i = ( int ) n;
				if ( i > 0 && i <= tagSet.getTags().size() )
				{
					selectTag( tagSet.getTags().get( i - 1 ) );
				}
				else if ( i == 0 )
				{
					removeLabels();
				}
			} );
		}
		else
		{
			// Remove labels.
			final KeyStroke removeKey = KeyStroke.getKeyStroke( KeyEvent.VK_0, 0 );
			final Action removeAction = new AbstractAction( "remove labels" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					removeLabels();
				}

				private static final long serialVersionUID = 1L;
			};
			inputMap.put( removeKey, "remove labels" );
			actionMap.put( "remove labels", removeAction );

			int i = 1;
			for ( final Tag tag : tagSet.getTags() )
			{
				final String actionName = "select label " + i;
				final KeyStroke takKeyStroke = KeyStroke.getKeyStroke( ( char ) ( '0' + i ) );
				final AbstractAction action = new AbstractAction( actionName )
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						selectTag( tag );
					}

					private static final long serialVersionUID = 1L;
				};

				inputMap.put( takKeyStroke, actionName );
				actionMap.put( actionName, action );
				i++;
			}
		}

		actionBindings.addActionMap( PICK_TAGS_MAP, actionMap );
		actionBindings.addInputMap( PICK_TAGS_MAP, inputMap, "all" );
		behaviourBindings.addInputTriggerMap( PICK_TAGS_MAP, triggerMap, "all" );

		this.tagSet = tagSet;
		mode = Mode.PICK_TAG;
		panel.repaint();
	}

	/**
	 * Removed all tags over all objects of current tag set.
	 */
	private synchronized void clearAllLabels()
	{
		final RefSet< V > vertices = selectionModel.getSelectedVertices();
		final RefSet< E > edges = selectionModel.getSelectedEdges();
		for ( final TagSet tagSet : tagModel.getTagSetStructure().getTagSets() )
		{
			final ObjTagMap< V, Tag > vertexTags = tagModel.getVertexTags().tags( tagSet );
			final ObjTagMap< E, Tag > edgeTags = tagModel.getEdgeTags().tags( tagSet );
			vertices.forEach( v -> vertexTags.set( v, null ) );
			edges.forEach( e -> edgeTags.set( e, null ) );
		}

		undo.setUndoPoint();
		done();
	}

	private synchronized void removeLabels()
	{
		if ( null == tagSet )
			return;

		setTag( selectionModel.getSelectedVertices(), selectionModel.getSelectedEdges(), null );

		undo.setUndoPoint();
		done();
	}

	private synchronized void selectTag( final Tag tag )
	{
		if ( null == tagSet || !tagSet.getTags().contains( tag ) )
			return;

		lock.readLock().lock();
		try
		{
			setTag( selectionModel.getSelectedVertices(), selectionModel.getSelectedEdges(), tag );
		}
		finally
		{
			lock.readLock().unlock();
		}

		undo.setUndoPoint();
		done();
	}

	private void setTag( final Collection< V > vertices, final Collection< E > edges, final Tag tag )
	{
		final ObjTagMap< V, Tag > vertexTags = tagModel.getVertexTags().tags( tagSet );
		final ObjTagMap< E, Tag > edgeTags = tagModel.getEdgeTags().tags( tagSet );
		vertices.forEach( v -> vertexTags.set( v, tag ) );
		edges.forEach( e -> edgeTags.set( e, tag ) );
	}

	private final static Color BACKGROUND_COLOR = new Color( 255, 255, 255, 230 );

	private static final Color LINE_COLOR = Color.ORANGE;

	private final static int INSET = 5;

	private static final int X_CORNER = 10;

	private static final int Y_CORNER = 10;

	private class TagSelectionOverlay implements OverlayRenderer
	{
		@Override
		public void drawOverlays( final Graphics g )
		{
			final String[] lines;
			final Font[] fonts;
			final Color[] colors;

			switch ( mode )
			{
			case INACTIVE:
			default:
				return;
			case PICK_TAGSET:
			{
				// Collect text to display.
				final TagSetStructure tagSetStructure = tagModel.getTagSetStructure();
				if ( tagSetStructure.getTagSets().size() == 0 )
				{
					lines = new String[ 2 ];
					fonts = new Font[ lines.length ];
					colors = new Color[ lines.length ];
					lines[ 0 ] = "No tags available.\n";
					lines[ 1 ] = "ESC: Cancel.";
					fonts[ 0 ] = FONT.deriveFont( Font.BOLD );
					fonts[ 1 ] = FONT;
				}
				else
				{
					lines = new String[ tagSetStructure.getTagSets().size() + 3 ];
					fonts = new Font[ lines.length ];
					colors = new Color[ lines.length ];
					lines[ 0 ] = "Tags available:\n";
					fonts[ 0 ] = FONT.deriveFont( Font.BOLD );
					int index = 1;
					for ( final TagSet tagSet : tagSetStructure.getTagSets() )
					{
						lines[ index ] = String.format( " - %2d: %s\n", index, tagSet.getName() );
						fonts[ index ] = FONT;
						index++;
					}
					fonts[ index ] = FONT;
					lines[ index ] = " - ESC: Cancel.";
					index++;
					fonts[ index ] = FONT;
					lines[ index ] = " - Shift DEL: Clear all";
				}
				Arrays.fill( colors, Color.BLACK );
				break;
			}
			case PICK_TAG:
			{
				// Collect text to display.
				if ( tagSet.getTags().isEmpty() )
				{
					lines = new String[ 2 ];
					fonts = new Font[ lines.length ];
					colors = new Color[ lines.length ];
					lines[ 0 ] = "No labels available.\n";
					lines[ 1 ] = "ESC: Cancel.";
					fonts[ 0 ] = FONT.deriveFont( Font.BOLD );
					fonts[ 1 ] = FONT;
					Arrays.fill( colors, Color.BLACK );
				}
				else
				{
					lines = new String[ tagSet.getTags().size() + 3 ];
					fonts = new Font[ lines.length ];
					colors = new Color[ lines.length ];
					lines[ 0 ] = "Labels available:\n";
					fonts[ 0 ] = FONT.deriveFont( Font.BOLD );
					colors[ 0 ] = Color.BLACK;
					int index = 1;
					for ( final Tag tag : tagSet.getTags() )
					{
						lines[ index ] = String.format( " - %2d: %s\n", index, tag.label() );
						colors[ index ] = new Color( tag.color(), true );
						fonts[ index ] = FONT;
						index++;
					}
					fonts[ index ] = FONT;
					lines[ index ] = " -  0: Remove labels";
					colors[ index ] = Color.BLACK;
					index++;
					fonts[ index ] = FONT;
					lines[ index ] = " - ESC: Cancel.";
					colors[ index ] = Color.BLACK;
				}
				break;
			}
			}

			// Compute string box size.
			int width = 0;
			int height = 0;
			for ( final String line : lines )
			{
				final Rectangle2D stringBounds = fontMetrics.getStringBounds( line, g );
				width = Math.max( width, ( int ) Math.ceil( stringBounds.getWidth() ) );
				height += Math.ceil( stringBounds.getHeight() );
			}

			// Paint box.
			g.setColor( BACKGROUND_COLOR );
			g.fillRect(
					X_CORNER,
					Y_CORNER,
					width + 2 * INSET,
					height + 2 * INSET );
			g.setColor( LINE_COLOR );
			g.drawRect(
					X_CORNER,
					Y_CORNER,
					width + 2 * INSET,
					height + 2 * INSET );

			// Paint strings.
			final int xs = X_CORNER + INSET;
			int ys = Y_CORNER + INSET;
			for ( int i = 0; i < lines.length; i++ )
			{
				g.setColor( colors[ i ] );
				g.setFont( fonts[ i ] );
				final String line = lines[ i ];
				final Rectangle2D stringBounds = fontMetrics.getStringBounds( line, g );
				final int sh = ( int ) Math.ceil( stringBounds.getHeight() );
				g.drawString( line, xs, ys + sh / 2 );
				ys += sh;
			}
		}

		@Override
		public void setCanvasSize( final int w, final int h )
		{}
	}

	static enum Mode
	{
		INACTIVE,
		PICK_TAGSET,
		PICK_TAG;
	}

	public static < V extends Vertex< E >, E extends Edge< V > > void install(
			final Actions actions,
			final InputActionBindings inputActionBindings,
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final TagSetModel< V, E > tagModel,
			final SelectionModel< V, E > selectionModel,
			final ReentrantReadWriteLock lock,
			final Component panel,
			final InteractiveDisplayCanvas display,
			final UndoPointMarker undo )
	{
		final EditTagActions< V, E > editTagActions = new EditTagActions<>( inputActionBindings,
				triggerBehaviourBindings, tagModel, selectionModel, lock, panel, display, undo );
		actions.runnableAction( editTagActions, PICK_TAGS, PICK_TAGS_KEYS );
	}
}
