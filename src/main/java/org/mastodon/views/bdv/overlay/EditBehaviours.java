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
package org.mastodon.views.bdv.overlay;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.model.FocusModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import net.imglib2.util.LinAlgHelpers;

/**
 * Behaviours for editing spots in BDV views.
 *
 * @param <V>
 *            vertex type.
 * @param <E>
 *            edge type.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class EditBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	public static final String MOVE_SPOT = "move spot";

	public static final String ADD_SPOT = "add spot";

	static final String[] MOVE_SPOT_KEYS = new String[] { "SPACE" };

	static final String[] ADD_SPOT_KEYS = new String[] { "not mapped" };


	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( MOVE_SPOT, MOVE_SPOT_KEYS, "Move spot by mouse-dragging." );
			descriptions.add( ADD_SPOT, ADD_SPOT_KEYS, "Add spot at mouse position." );
		}
	}

	public static final boolean FOCUS_EDITED_SPOT = true;

	public static final boolean SELECT_ADDED_SPOT = true;

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final MoveSpotBehaviour moveSpotBehaviour;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E, ? > renderer,
			final FocusModel< V > focus,
			final UndoPointMarker undo,
			final double minRadius )
	{
		final EditBehaviours< V, E > eb = new EditBehaviours<>( overlayGraph, renderer, focus, undo );
		behaviours.namedBehaviour( eb.moveSpotBehaviour, MOVE_SPOT_KEYS );
	}

	private final OverlayGraph< V, E > overlayGraph;

	private final ReentrantReadWriteLock lock;

	private final OverlayGraphRenderer< V, E, ? > renderer;

	private final FocusModel< V > focus;

	private final UndoPointMarker undo;

	private EditBehaviours(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E, ? > renderer,
			final FocusModel< V > focus,
			final UndoPointMarker undo )
	{
		this.overlayGraph = overlayGraph;
		this.lock = overlayGraph.getLock();
		this.renderer = renderer;
		this.focus = focus;
		this.undo = undo;

		moveSpotBehaviour = new MoveSpotBehaviour( MOVE_SPOT );
	}

	private class MoveSpotBehaviour extends AbstractNamedBehaviour implements DragBehaviour
	{

		private final double[] start;

		private final double[] pos;

		/**
		 * This is set to true in {@link #init(int, int)} if a vertex can be
		 * found at the start location. If it is false, {@link #drag(int, int)}
		 * and {@link #end(int, int)} don't do anything.
		 */
		private boolean moving;

		private final V ref;

		private V vertex;

		public MoveSpotBehaviour( final String name )
		{
			super( name );
			start = new double[ 3 ];
			pos = new double[ 3 ];
			moving = false;
			ref = overlayGraph.vertexRef();
		}

		@Override
		public void init( final int x, final int y )
		{
			lock.readLock().lock();
			vertex = renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, ref );
			if ( vertex != null )
			{
				renderer.getGlobalPosition( x, y, start );
				vertex.localize( pos );
				LinAlgHelpers.subtract( pos, start, start );
				moving = true;
			}
			else
				lock.readLock().unlock();
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( moving )
			{
				renderer.getGlobalPosition( x, y, pos );
				LinAlgHelpers.add( pos, start, pos );
				vertex.setPosition( pos );
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( moving )
			{
				undo.setUndoPoint();

				if ( FOCUS_EDITED_SPOT )
					focus.focusVertex( vertex );

				moving = false;
				lock.readLock().unlock();
			}
		}
	}
}
