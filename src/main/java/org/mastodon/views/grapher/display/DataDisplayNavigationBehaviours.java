/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.collection.RefSet;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.OffsetAxes.OffsetAxesListener;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.InteractiveDisplayCanvas;
import bdv.viewer.OverlayRenderer;
import bdv.viewer.TransformListener;

/**
 * Focus and selection behaviours in Data.
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class DataDisplayNavigationBehaviours implements TransformListener< ScreenTransform >, OffsetAxesListener
{
	public static final String FOCUS_VERTEX = "data click focus vertex";
	public static final String NAVIGATE_TO_VERTEX = "data click navigate to vertex";
	public static final String SELECT = "data click select";
	public static final String ADD_SELECT = "data click add to selection";
	public static final String BOX_SELECT = "data box selection";
	public static final String BOX_ADD_SELECT = "data box add to selection";

	private static final String[] FOCUS_VERTEX_KEYS = new String[] { "button1", "shift button1" };
	private static final String[] NAVIGATE_TO_VERTEX_KEYS = new String[] { "double-click button1", "shift double-click button1" };
	private static final String[] SELECT_KEYS = new String[] { "button1"};
	private static final String[] ADD_SELECT_KEYS = new String[] { "shift button1"};
	private static final String[] BOX_SELECT_KEYS = new String[] { "button1"};
	private static final String[] BOX_ADD_SELECT_KEYS = new String[] { "shift button1"};

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( FOCUS_VERTEX, FOCUS_VERTEX_KEYS, "Focus spot (spot gets keyboard focus)." );
			descriptions.add( NAVIGATE_TO_VERTEX, NAVIGATE_TO_VERTEX_KEYS, "Navigate to spot (in all linked views)." );
			descriptions.add( SELECT, SELECT_KEYS, "Select spot or link." );
			descriptions.add( ADD_SELECT, ADD_SELECT_KEYS, "Add spot or link to selection." );
			descriptions.add( BOX_SELECT, BOX_SELECT_KEYS, "Drag a box to select spots and links." );
			descriptions.add( BOX_ADD_SELECT, BOX_ADD_SELECT_KEYS, "Drag a box to add spots and links to selection." );
		}
	}

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final InteractiveDisplayCanvas display;

	private final DataGraph< ?, ? > graph;

	private final ReentrantReadWriteLock lock;

	private final NavigationHandler< DataVertex, DataEdge > navigation;

	private final SelectionModel< DataVertex, DataEdge > selection;

	private final DataDisplayOverlay graphOverlay;

	private final FocusModel< DataVertex, DataEdge > focus;

	private final ScreenTransform screenTransform;

	/**
	 * Current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * Current height of horizontal header.
	 */
	private int headerHeight;

	private final ClickFocusBehaviour focusVertexBehaviour;

	private final ClickNavigateBehaviour navigateToVertexBehaviour;

	private final ClickSelectionBehaviour selectBehaviour;

	private final ClickSelectionBehaviour addSelectBehaviour;

	private final BoxSelectionBehaviour boxSelectBehaviour;

	private final BoxSelectionBehaviour boxAddSelectBehaviour;

	private final DataGraphLayout< ?, ? > layout;

	public DataDisplayNavigationBehaviours(
			final InteractiveDisplayCanvas display,
			final DataGraph< ?, ? > graph,
			final DataGraphLayout< ?, ? > layout,
			final DataDisplayOverlay graphOverlay,
			final FocusModel< DataVertex, DataEdge > focus,
			final NavigationHandler< DataVertex, DataEdge > navigation,
			final SelectionModel< DataVertex, DataEdge > selection )
	{
		this.display = display;
		this.graph = graph;
		this.layout = layout;
		this.lock = graph.getLock();
		this.graphOverlay = graphOverlay;
		this.focus = focus;
		this.navigation = navigation;
		this.selection = selection;

		screenTransform = new ScreenTransform();

		focusVertexBehaviour = new ClickFocusBehaviour();
		navigateToVertexBehaviour = new ClickNavigateBehaviour();
		selectBehaviour = new ClickSelectionBehaviour( SELECT, false );
		addSelectBehaviour = new ClickSelectionBehaviour( ADD_SELECT, true );
		boxSelectBehaviour = new BoxSelectionBehaviour( BOX_SELECT, false );
		boxAddSelectBehaviour = new BoxSelectionBehaviour( BOX_ADD_SELECT, true );
	}

	public void install( final Behaviours behaviours )
	{
		behaviours.namedBehaviour( focusVertexBehaviour, FOCUS_VERTEX_KEYS );
		behaviours.namedBehaviour( navigateToVertexBehaviour, NAVIGATE_TO_VERTEX_KEYS );
		behaviours.namedBehaviour( selectBehaviour, SELECT_KEYS );
		behaviours.namedBehaviour( addSelectBehaviour, ADD_SELECT_KEYS );
		behaviours.namedBehaviour( boxSelectBehaviour, BOX_SELECT_KEYS );
		behaviours.namedBehaviour( boxAddSelectBehaviour, BOX_ADD_SELECT_KEYS );
	}

	/*
	 * PRIVATE METHODS
	 */

	private void selectWithin( final int x1, final int y1, final int x2, final int y2, final boolean addToSelection )
	{
		selection.pauseListeners();

		if ( !addToSelection )
			selection.clearSelection();

		final RefSet< DataVertex > vs = layout.getDataVerticesWithin( x1, y1, x2, y2 );
		final DataVertex vertexRef = graph.vertexRef();
		for ( final DataVertex v : vs )
		{
			selection.setSelected( v, true );
			for ( final DataEdge e : v.outgoingEdges() )
			{
				final DataVertex t = e.getTarget( vertexRef );
				if ( vs.contains( t ) )
					selection.setSelected( e, true );
			}
		}

		final Iterator< DataVertex > it = vs.iterator();
		if ( it.hasNext() )
			focus.focusVertex( it.next() );

		graph.releaseRef( vertexRef );

		selection.resumeListeners();
	}

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();

		final DataVertex vertex = graph.vertexRef();
		final DataEdge edge = graph.edgeRef();

		// See if we can select a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			final boolean selected = selection.isSelected( vertex );
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = selection.isSelected( edge );
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( edge, !selected );
		}
		// Nothing found. clear selection if addToSelection == false
		else if ( !addToSelection )
			selection.clearSelection();

		graph.releaseRef( vertex );
		graph.releaseRef( edge );

		selection.resumeListeners();
	}

	private void navigate( final int x, final int y )
	{
		final DataVertex vertex = graph.vertexRef();
		final DataEdge edge = graph.edgeRef();

		// See if we can find a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			navigation.notifyNavigateToVertex( vertex );
		}
		// See if we can find an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			navigation.notifyNavigateToEdge( edge );
		}

		graph.releaseRef( vertex );
		graph.releaseRef( edge );
	}

	private void focus( final int x, final int y )
	{
		final DataVertex ref = graph.vertexRef();

		focus.focusVertex( graphOverlay.getVertexAt( x, y, ref ) ); // if clicked outside, getVertexAt == null, clears the focus.

		graph.releaseRef( ref );
	}

	/*
	 * BEHAVIOURS
	 */

	/**
	 * Behaviour to focus a vertex with a mouse click. If the click happens
	 * outside of a vertex, the focus is cleared.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickFocusBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickFocusBehaviour()
		{
			super( FOCUS_VERTEX );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			lock.readLock().lock();
			try
			{
				focus( x, y );
			}
			finally
			{
				lock.readLock().unlock();
			}
		}
	}

	/**
	 * Behaviour to navigate to a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickNavigateBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickNavigateBehaviour()
		{
			super( NAVIGATE_TO_VERTEX );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			lock.readLock().lock();
			try
			{
				navigate( x, y );
			}
			finally
			{
				lock.readLock().unlock();
			}
		}
	}

	/**
	 * Behaviour to select a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickSelectionBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final boolean addToSelection;

		public ClickSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			lock.readLock().lock();
			try
			{
				select( x, y, addToSelection );
			}
			finally
			{
				lock.readLock().unlock();
			}
		}
	}

	/**
	 * Behaviour to select vertices and edges inside a bounding box with a mouse
	 * drag.
	 * <p>
	 * The selection happens in layout space, so it also selects vertices inside
	 * dense ranges. A vertex is inside the bounding box if its layout
	 * coordinate is inside the bounding box.
	 */
	private class BoxSelectionBehaviour extends AbstractNamedBehaviour implements DragBehaviour, OverlayRenderer
	{
		/**
		 * Coordinates where mouse dragging started.
		 */
		private int oX, oY;

		/**
		 * Coordinates where mouse dragging currently is.
		 */
		private int eX, eY;

		private boolean dragging = false;

		private boolean ignore = false;

		private final boolean addToSelection;

		public BoxSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void init( final int x, final int y )
		{
			oX = x;
			oY = y;
			dragging = false;
			ignore = x < headerWidth || y < headerHeight;
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( ignore )
				return;

			eX = x;
			eY = y;
			if ( !dragging )
			{
				dragging = true;
				display.overlays().add( this );
			}
			display.repaint();
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( ignore )
				return;

			if ( dragging )
			{
				dragging = false;
				display.overlays().remove( this );
				display.repaint();
				lock.readLock().lock();
				try
				{
					selectWithin( oX, oY, eX, eY, addToSelection );
				}
				finally
				{
					lock.readLock().unlock();
				}
			}
		}

		/**
		 * Draws the selection box, if there is one.
		 */
		@Override
		public void drawOverlays( final Graphics g )
		{
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
		}
	}

	@Override
	public void updateAxesSize( final int width, final int height )
	{
		headerWidth = width;
		headerHeight = height;
	}
}
