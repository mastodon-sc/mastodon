package org.mastodon.revised.bdv.overlay;

import static org.mastodon.revised.bdv.overlay.EditBehaviours.POINT_SELECT_DISTANCE_TOLERANCE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.revised.bdv.overlay.ScreenVertexMath.Ellipse;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.imglib2.util.LinAlgHelpers;

public class EditSpecialBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	private static final String ADD_SPOT_AND_LINK_IT_FORWARD = "add linked spot";
	private static final String ADD_SPOT_AND_LINK_IT_BACKWARD = "add linked spot backward";
	private static final String TOGGLE_LINK_FORWARD = "toggle link";
	private static final String TOGGLE_LINK_BACKWARD = "toggle link backward";

	private static final String[] ADD_SPOT_AND_LINK_IT_FORWARD_KEYS = new String[] { "A" };
	private static final String[] ADD_SPOT_AND_LINK_IT_BACKWARD_KEYS = new String[] { "C" };
	private static final String[] TOGGLE_LINK_FORWARD_KEYS = new String[] { "L" };
	private static final String[] TOGGLE_LINK_BACKWARD_KEYS = new String[] { "shift L" };

	public static final Color EDIT_GRAPH_OVERLAY_COLOR = Color.WHITE;
	public static final BasicStroke EDIT_GRAPH_OVERLAY_STROKE = new BasicStroke( 2f );
	public static final BasicStroke EDIT_GRAPH_OVERLAY_GHOST_STROKE = new BasicStroke(
			1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			1.0f, new float[] { 4f, 10f }, 0f );

	private final AddSpotAndLinkIt addSpotAndLinkItForwardBehaviour;

	private final AddSpotAndLinkIt addSpotAndLinkItBackwardBehaviour;

	private final ToggleLink toggleLinkForwardBehaviour;

	private final ToggleLink toggleLinkBackwardBehaviour;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void installActionBindings(
			final Behaviours behaviours,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		final EditSpecialBehaviours< V, E > eb = new EditSpecialBehaviours<>( viewer, overlayGraph, renderer, undo );

		behaviours.namedBehaviour( eb.addSpotAndLinkItForwardBehaviour, ADD_SPOT_AND_LINK_IT_FORWARD_KEYS );
		behaviours.namedBehaviour( eb.addSpotAndLinkItBackwardBehaviour, ADD_SPOT_AND_LINK_IT_BACKWARD_KEYS );
		behaviours.namedBehaviour( eb.toggleLinkForwardBehaviour, TOGGLE_LINK_FORWARD_KEYS );
		behaviours.namedBehaviour( eb.toggleLinkBackwardBehaviour, TOGGLE_LINK_BACKWARD_KEYS );
	}

	private final ViewerPanel viewer;

	private final OverlayGraph< V, E > overlayGraph;

	private final ReentrantReadWriteLock lock;

	private final OverlayGraphRenderer< V, E > renderer;

	private final UndoPointMarker undo;

	private final EditSpecialBehaviours< V, E >.EditSpecialBehavioursOverlay overlay;

	private EditSpecialBehaviours(
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		this.viewer = viewer;
		this.overlayGraph = overlayGraph;
		this.lock = overlayGraph.getLock();
		this.renderer = renderer;
		this.undo = undo;

		// Create and register overlay.
		overlay = new EditSpecialBehavioursOverlay();
		overlay.transformChanged( viewer.getDisplay().getTransformEventHandler().getTransform() );
		viewer.getDisplay().addOverlayRenderer( overlay );
		viewer.getDisplay().addTransformListener( overlay );

		// Behaviours.
		addSpotAndLinkItForwardBehaviour = new AddSpotAndLinkIt( ADD_SPOT_AND_LINK_IT_FORWARD, true );
		addSpotAndLinkItBackwardBehaviour = new AddSpotAndLinkIt( ADD_SPOT_AND_LINK_IT_BACKWARD, false );
		toggleLinkForwardBehaviour = new ToggleLink( TOGGLE_LINK_FORWARD, true );
		toggleLinkBackwardBehaviour = new ToggleLink( TOGGLE_LINK_BACKWARD, false );
	}

	// TODO: This should respect the same RenderSettings as OverlayGraphRenderer for painting the ghost vertex & edge!!!
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

			// The vertex
			if ( paintGhostVertex )
			{
				final AffineTransform3D transform = getRenderTransformCopy();
				graphics.setStroke( EDIT_GRAPH_OVERLAY_GHOST_STROKE );

				// The spot ghost, painted using ellipse projection.
				final AffineTransform torig = graphics.getTransform();

				screenVertexMath.init( vertex, transform );

				final Ellipse ellipse = screenVertexMath.getProjectEllipse();
				OverlayGraphRenderer.drawEllipse( graphics, ellipse, torig );
			}

			// The link.
			if ( paintGhostLink )
			{
				graphics.setStroke( EDIT_GRAPH_OVERLAY_STROKE );
				renderer.getViewerPosition( from, vFrom );
				renderer.getViewerPosition( to, vTo );
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

	// TODO What to do if the user changes the time-point while dragging?
	// TODO Because the user can move in time currently, always do a sanity check before inserting the link
	private class ToggleLink extends AbstractNamedBehaviour implements DragBehaviour
	{
		private final V source;

		private final V target;

		private final V tmp;

		private final E edgeRef;

		private boolean editing;

		private final boolean forward;

		public ToggleLink( final String name, final boolean forward )
		{
			super( name );
			this.forward = forward;
			source = overlayGraph.vertexRef();
			target = overlayGraph.vertexRef();
			tmp = overlayGraph.vertexRef();
			edgeRef = overlayGraph.edgeRef();
			editing = false;
		}

		@Override
		public void init( final int x, final int y )
		{
			// Get vertex we clicked inside.
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, source ) != null )
			{
				overlay.paintGhostLink = true;
				overlay.paintGhostVertex = true;
				source.localize( overlay.from );
				source.localize( overlay.to );
				overlay.vertex = source;

				// Move to next time point.
				if ( forward )
					viewer.nextTimePoint();
				else
					viewer.previousTimePoint();

				editing = true;
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( editing )
			{
				if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, target ) != null )
					target.localize( overlay.to );
				else
					renderer.getGlobalPosition( x, y, overlay.to );
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( editing )
			{
				if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, target ) != null )
				{
					target.localize( overlay.to );

					/*
					 * Careful with directed graphs. We always check and create
					 * links forward in time.
					 */
					if ( !forward )
					{
						tmp.refTo( source );
						source.refTo( target );
						target.refTo( tmp );
					}

					lock.writeLock().lock();
					try
					{
						final E edge = overlayGraph.getEdge( source, target, edgeRef );
						if ( null == edge )
							overlayGraph.addEdge( source, target, edgeRef ).init();
						else
							overlayGraph.remove( edge );

						overlayGraph.notifyGraphChanged();
						undo.setUndoPoint();
					}
					finally
					{
						lock.writeLock().unlock();
					}
				}
				overlay.paintGhostVertex = false;
				overlay.paintGhostLink = false;
				editing = false;
			}
		}
	}

	// TODO What to do if the user changes the time-point while dragging?
	private class AddSpotAndLinkIt extends AbstractNamedBehaviour implements DragBehaviour
	{
		private final V source;

		private final V target;

		private final E edge;

		private final double[] start;

		private final double[] pos;

		private boolean moving;

		private final double[][] mat;

		private final boolean forward;

		public AddSpotAndLinkIt( final String name, final boolean forward )
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
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, source ) != null )
			{
				// Get vertex we clicked inside.
				renderer.getGlobalPosition( x, y, start );
				source.localize( pos );
				LinAlgHelpers.subtract( pos, start, start );

				// Set it as ghost vertex for the overlay.
				overlay.vertex = source;
				overlay.paintGhostVertex = true;

				// Move to next time point.
				if ( forward )
					viewer.nextTimePoint();
				else
					viewer.previousTimePoint();

				lock.writeLock().lock();
				try
				{
					// Create new vertex under click location.
					source.getCovariance( mat );
					final int timepoint = renderer.getCurrentTimepoint();
					overlayGraph.addVertex( target ).init( timepoint, pos, mat );

					// Link it to source vertex. Careful for oriented edge.
					if ( forward )
						overlayGraph.addEdge( source, target, edge ).init();
					else
						overlayGraph.addEdge( target, source, edge ).init();

					// Set it as ghost link for the overlay.
					System.arraycopy( pos, 0, overlay.from, 0, pos.length );
					System.arraycopy( pos, 0, overlay.to, 0, pos.length );
					overlay.paintGhostLink = true;

					overlayGraph.notifyGraphChanged();
				}
				finally
				{
					lock.writeLock().unlock();
				}
				moving = true;
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( moving )
			{
				renderer.getGlobalPosition( x, y, pos );
				LinAlgHelpers.add( pos, start, pos );
				target.setPosition( pos );
				System.arraycopy( pos, 0, overlay.to, 0, pos.length );
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( moving )
			{
				overlay.paintGhostVertex = false;
				overlay.paintGhostLink = false;
				undo.setUndoPoint();
				overlayGraph.notifyGraphChanged();
				moving = false;
			}
		}
	}
}
