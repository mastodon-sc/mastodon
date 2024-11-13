/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.views.bdv.overlay.EditBehaviours.FOCUS_EDITED_SPOT;
import static org.mastodon.views.bdv.overlay.EditBehaviours.POINT_SELECT_DISTANCE_TOLERANCE;
import static org.mastodon.views.bdv.overlay.EditBehaviours.SELECT_ADDED_SPOT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.collection.RefSet;
import org.mastodon.model.FocusModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.bdv.overlay.ScreenVertexMath.Ellipse;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.OverlayRenderer;
import bdv.viewer.TransformListener;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

public class EditSpecialBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{

	private static final String ADD_OR_LINK_SPOT_FORWARD = "add or link spot";

	private static final String ADD_OR_LINK_SPOT_BACKWARD = "add or link spot backward";

	private static final String[] ADD_OR_LINK_SPOT_FORWARD_KEYS = new String[] { "A" };

	private static final String[] ADD_OR_LINK_SPOT_BACKWARD_KEYS = new String[] { "C" };

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
			descriptions.add( ADD_OR_LINK_SPOT_FORWARD, ADD_OR_LINK_SPOT_FORWARD_KEYS,
					"Main action to add or link to a new spot."
							+ "\n"
							+ "If there are no spots where the mouse is clicked, a new spot is created, which "
							+ "can be positionned by holding the key and moving the mouse. "
							+ "\n"
							+ "If the key is pressed from within a source spot, the viewer moves to the *next* "
							+ "time point and offers to create a new spot linked to this source. "
							+ "Hold and drag to position the new spot. A new spot will be created "
							+ "where the mouse is released, and linked to the source spot. "
							+ "If a target spot is found near  the mouse location when the key is "
							+ "released, it is linked to the source spot. If a link already exists "
							+ "between the two, it is removed." );
			descriptions.add( ADD_OR_LINK_SPOT_BACKWARD, ADD_OR_LINK_SPOT_BACKWARD_KEYS,
					"Main action to add or link to a new spot."
							+ "\n"
							+ "If there are no spots where the mouse is clicked, a new spot is created, which "
							+ "can be positionned by holding the key and moving the mouse. "
							+ "\n"
							+ "If the key is pressed from within a source spot, the viewer moves to the *previous* "
							+ "time point and offers to create a new spot linked to this source. "
							+ "Hold and drag to position the new spot. A new spot will be created "
							+ "where the mouse is released, and linked to the source spot. "
							+ "If a target spot is found near  the mouse location when the key is "
							+ "released, it is linked to the source spot. If a link already exists "
							+ "between the two, it is removed." );
		}
	}

	public static final Color EDIT_GRAPH_OVERLAY_COLOR = Color.WHITE;

	public static final BasicStroke EDIT_GRAPH_OVERLAY_NORMAL_STROKE = new BasicStroke( 2f );

	public static final BasicStroke EDIT_GRAPH_OVERLAY_BIG_STROKE = new BasicStroke( 4f );

	public static final BasicStroke EDIT_GRAPH_OVERLAY_REMOVAL_STROKE = new BasicStroke( 4f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			0, new float[] { 6 }, 0 );

	public static final BasicStroke EDIT_GRAPH_OVERLAY_GHOST_STROKE = new BasicStroke(
			1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			1.0f, new float[] { 4f, 10f }, 0f );

	private final AddOrLinkSpot addOrLinkSpotForward;

	private final AddOrLinkSpot addOrLinkSpotBackward;

	private final EditSpecialBehavioursOverlay overlay;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final FocusModel< V > focus,
			final UndoPointMarker undo )
	{
		final EditSpecialBehaviours< V, E > eb =
				new EditSpecialBehaviours<>( viewer, overlayGraph, renderer, selection, focus, undo );

		behaviours.namedBehaviour( eb.addOrLinkSpotForward, ADD_OR_LINK_SPOT_FORWARD_KEYS );
		behaviours.namedBehaviour( eb.addOrLinkSpotBackward, ADD_OR_LINK_SPOT_BACKWARD_KEYS );
	}

	private final ViewerPanel viewer;

	private final OverlayGraph< V, E > overlayGraph;

	private final ReentrantReadWriteLock lock;

	private final OverlayGraphRenderer< V, E > renderer;

	private final SelectionModel< V, E > selection;

	private final FocusModel< V > focus;

	private final UndoPointMarker undo;

	private EditSpecialBehaviours(
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final FocusModel< V > focus,
			final UndoPointMarker undo )
	{
		this.viewer = viewer;
		this.overlayGraph = overlayGraph;
		this.lock = overlayGraph.getLock();
		this.renderer = renderer;
		this.selection = selection;
		this.focus = focus;
		this.undo = undo;

		// Create and register overlay.
		overlay = new EditSpecialBehavioursOverlay();
		overlay.transformChanged( viewer.state().getViewerTransform() );
		viewer.getDisplay().overlays().add( overlay );
		viewer.renderTransformListeners().add( overlay );

		// Behaviours.
		addOrLinkSpotForward = new AddOrLinkSpot( ADD_OR_LINK_SPOT_FORWARD, true );
		addOrLinkSpotBackward = new AddOrLinkSpot( ADD_OR_LINK_SPOT_BACKWARD, false );
	}

	private class EditSpecialBehavioursOverlay implements OverlayRenderer, TransformListener< AffineTransform3D >
	{

		/** The global coordinates to paint the link from. */
		private final double[] from;

		/** The global coordinates to paint the link to. */
		private final double[] to;

		/** The viewer coordinates to paint the link from. */
		private final double[] vFrom;

		/** The viewer coordinates to paint the link to. */
		private final double[] vTo;

		/** The ghost vertex to paint. */
		private V vertex;

		private final AffineTransform3D renderTransform;

		private final ScreenVertexMath screenVertexMath;

		public boolean paintGhostVertex;

		public boolean paintGhostLink;

		public boolean paintGhostTarget;

		public boolean snap;

		public boolean edgeToRemove;

		public EditSpecialBehavioursOverlay()
		{
			from = new double[ 3 ];
			vFrom = new double[ 3 ];
			to = new double[ 3 ];
			vTo = new double[ 3 ];

			renderTransform = new AffineTransform3D();
			screenVertexMath = new ScreenVertexMath();
		}

		@Override
		public void drawOverlays( final Graphics g )
		{
			final Graphics2D graphics = ( Graphics2D ) g;
			g.setColor( EDIT_GRAPH_OVERLAY_COLOR );

			renderer.getViewerPosition( to, vTo );

			// The vertex
			if ( paintGhostVertex )
			{
				final AffineTransform3D transform = getRenderTransformCopy();
				graphics.setStroke( EDIT_GRAPH_OVERLAY_GHOST_STROKE );

				// The spot ghost, painted using ellipse projection.
				final AffineTransform torig = graphics.getTransform();

				screenVertexMath.init( vertex, transform );

				final Ellipse ellipse = screenVertexMath.getProjectEllipse();
				OverlayGraphRenderer.drawEllipse( graphics, ellipse, torig, false );

				// The target
				if ( paintGhostTarget )
				{
					ellipse.setCenter( vTo[ 0 ], vTo[ 1 ] );
					graphics.setStroke( EDIT_GRAPH_OVERLAY_NORMAL_STROKE );
					OverlayGraphRenderer.drawEllipse( graphics, ellipse, torig, false );
				}
			}

			// The link.
			if ( paintGhostLink )
			{
				graphics.setStroke( snap
						? ( edgeToRemove ? EDIT_GRAPH_OVERLAY_REMOVAL_STROKE : EDIT_GRAPH_OVERLAY_BIG_STROKE )
						: EDIT_GRAPH_OVERLAY_NORMAL_STROKE );
				renderer.getViewerPosition( from, vFrom );

				g.drawLine( ( int ) vFrom[ 0 ], ( int ) vFrom[ 1 ],
						( int ) vTo[ 0 ], ( int ) vTo[ 1 ] );
			}
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}

		@Override
		public void transformChanged( final AffineTransform3D transform )
		{
			synchronized ( renderTransform )
			{
				renderTransform.set( transform );
			}
		}

		private AffineTransform3D getRenderTransformCopy()
		{
			final AffineTransform3D transform = new AffineTransform3D();
			synchronized ( renderTransform )
			{
				transform.set( renderTransform );
			}
			return transform;
		}
	}

	private class AddOrLinkSpot extends AbstractNamedBehaviour implements DragBehaviour
	{
		private final V source;

		private final V target;

		private final E edge;

		private final double[] start;

		private final double[] pos;

		private boolean moving;

		private final double[][] mat;

		private final boolean forward;

		private boolean creatingNewSpot;

		public AddOrLinkSpot( final String name, final boolean forward )
		{
			super( name );
			this.forward = forward;
			source = overlayGraph.vertexRef();
			target = overlayGraph.vertexRef();
			edge = overlayGraph.edgeRef();
			start = new double[ 3 ];
			pos = new double[ 3 ];
			moving = false;
			mat = new double[ 3 ][ 3 ];
		}

		@Override
		public void init( final int x, final int y )
		{
			overlay.paintGhostLink = false;
			overlay.paintGhostVertex = false;
			overlay.paintGhostTarget = false;

			/*
			 * Are we creating a new spot in an empty place or linking from an
			 * existing spot?
			 */
			lock.readLock().lock();
			try
			{
				creatingNewSpot = ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, source ) == null );
			}
			finally
			{
				lock.readLock().unlock();
			}

			if ( creatingNewSpot )
			{
				final int timepoint = renderer.getCurrentTimepoint();
				renderer.getGlobalPosition( x, y, pos );
				lock.writeLock().lock();
				try
				{
					// Add new spot.
					overlayGraph.addVertex( source ).init( timepoint, pos, EditBehaviours.lastRadius );

					// Link to it if autolink mode is on.
					if ( EditBehaviours.autoLink )
					{
						final RefSet< V > selectedVertices = selection.getSelectedVertices();
						if ( selectedVertices.size() == 1 )
						{
							final V previous = selectedVertices.iterator().next();
							/*
							 * Careful with directed graphs. We always check and
							 * create links forward in time.
							 */
							final int t1 = source.getTimepoint();
							final int t2 = previous.getTimepoint();
							if ( t1 != t2 )
							{
								final V from = t1 > t2 ? previous : source;
								final V to = t1 > t2 ? source : previous;
								final E eref = overlayGraph.edgeRef();
								overlayGraph.addEdge( from, to, eref ).init();
								overlayGraph.releaseRef( eref );
							}
						}
					}
					overlayGraph.notifyGraphChanged();
					undo.setUndoPoint();

					if ( FOCUS_EDITED_SPOT )
						focus.focusVertex( source );

					if ( SELECT_ADDED_SPOT )
					{
						selection.pauseListeners();
						selection.clearSelection();
						selection.setSelected( source, true );
						selection.resumeListeners();
					}
				}
				finally
				{
					lock.writeLock().unlock();
				}
			}
			else
			{
				// Move to next or previous time point and check that we can.
				final int currentTimepoint = viewer.state().getCurrentTimepoint();
				if ( forward )
					viewer.nextTimePoint();
				else
					viewer.previousTimePoint();

				final int newTimepoint = viewer.state().getCurrentTimepoint();
				if ( currentTimepoint == newTimepoint )
				{
					/*
					 * Refuse to work: we are in the same time-point. We do not
					 * want to create links inside the same time-point.
					 */
					return;
				}

				// Get vertex we clicked inside.
				renderer.getGlobalPosition( x, y, start );
				source.localize( pos );
				LinAlgHelpers.subtract( pos, start, start );

				// Set it as ghost vertex for the overlay.
				overlay.vertex = source;

				// Set it as ghost link for the overlay.
				System.arraycopy( pos, 0, overlay.from, 0, pos.length );
				System.arraycopy( pos, 0, overlay.to, 0, pos.length );
				overlay.paintGhostLink = true;
				overlay.paintGhostVertex = true;
				overlay.paintGhostTarget = false;
			}
			moving = true;
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( moving )
			{
				if ( creatingNewSpot )
				{
					// Move the new spot around.
					renderer.getGlobalPosition( x, y, pos );
					LinAlgHelpers.add( pos, start, pos );
					source.setPosition( pos );
				}
				else
				{
					// Show a future link to a new or existing spot.
					lock.readLock().lock();
					try
					{
						if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, target ) == null )
						{
							// No target in the vicinity - paint the future one.
							overlay.paintGhostTarget = true;
							overlay.snap = false;
							renderer.getGlobalPosition( x, y, pos );
							LinAlgHelpers.add( pos, start, pos );
							System.arraycopy( pos, 0, overlay.to, 0, pos.length );
						}
						else
						{
							// Snap ghost link to found target.
							overlay.paintGhostTarget = false;
							overlay.snap = true;
							target.localize( overlay.to );
							/*
							 * Is there a link between the source and found
							 * target? Tell the overlay.
							 */
							overlay.edgeToRemove = ( overlayGraph.getEdge( source, target, edge ) != null )
									|| ( overlayGraph.getEdge( target, source, edge ) != null );
						}
					}
					finally
					{
						lock.readLock().unlock();
					}
				}
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			try
			{
				if ( moving && !creatingNewSpot )
				{
					boolean targetFound = false;
					lock.readLock().lock();
					try
					{
						targetFound = renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, target ) != null;
					}
					finally
					{
						lock.readLock().unlock();
					}

					lock.writeLock().lock();
					try
					{
						if ( !targetFound )
						{
							/*
							 * No target in the vicinity -> create a new spot
							 * and link to it.
							 */
							source.getCovariance( mat );
							final int timepoint = viewer.state().getCurrentTimepoint();
							overlayGraph.addVertex( target ).init( timepoint, pos, mat );
						}

						/*
						 * Link the new or existing spot to source vertex if
						 * there is none between the two. Otherwise remove the
						 * existing link. Careful to oriented edge.
						 */
						if ( overlayGraph.getEdge( source, target, edge ) != null )
						{ // forward
							overlayGraph.remove( edge );
							return;
						}
						if ( overlayGraph.getEdge( target, source, edge ) != null )
						{ // backward
							overlayGraph.remove( edge );
							return;
						}

						if ( forward )
							overlayGraph.addEdge( source, target, edge ).init();
						else
							overlayGraph.addEdge( target, source, edge ).init();

						if ( FOCUS_EDITED_SPOT )
							focus.focusVertex( target );

						if ( SELECT_ADDED_SPOT )
						{
							selection.pauseListeners();
							selection.clearSelection();
							selection.setSelected( target, true );
							selection.resumeListeners();
						}
					}
					finally
					{
						overlayGraph.notifyGraphChanged();
						undo.setUndoPoint();
						lock.writeLock().unlock();
					}
				}
			}
			finally
			{
				moving = false;
				overlay.paintGhostVertex = false;
				overlay.paintGhostLink = false;
				overlay.paintGhostTarget = false;
			}
		}
	}
}
