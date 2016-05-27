package net.trackmate.revised.bdv.overlay;

import static net.trackmate.revised.bdv.overlay.EditBevaviours.POINT_SELECT_DISTANCE_TOLERANCE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import net.trackmate.revised.model.mamut.Spot;
import net.trackmate.undo.UndoPointMarker;

public class EditSpecialBevaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends AbstractBehaviours
{

	private static final String ADD_SPOT_AND_LINK_IT = "add linked spot";
	private static final String TOGGLE_LINK = "toggle link";

	private static final String[] ADD_SPOT_AND_LINK_IT_KEYS = new String[] { "A" };
	private static final String[] TOGGLE_LINK_KEYS = new String[] { "L" } ;

	public static final Color EDIT_GRAPH_OVERLAY_COLOR = Color.WHITE;
	public static final BasicStroke EDIT_GRAPH_OVERLAY_STROKE = new BasicStroke( 2f );
	public static final BasicStroke EDIT_GRAPH_OVERLAY_GHOST_STROKE = new BasicStroke(
			1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			1.0f, new float[] { 4f, 10f }, 0f );

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final UndoPointMarker undo;

	private final ViewerPanel viewer;

	private final EditSpecialBevaviours< V, E >.EditSpecialBehavioursOverlay overlay;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		new EditSpecialBevaviours<>( triggerBehaviourBindings, config, viewer, overlayGraph, renderer, undo );
	}


	private EditSpecialBevaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ViewerPanel viewer,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final UndoPointMarker undo )
	{
		super( triggerBehaviourBindings, "graph-special", config, new String[] { "bdv" } );
		this.viewer = viewer;
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.undo = undo;

		// Create and register overlay.
		overlay = new EditSpecialBehavioursOverlay();
		overlay.transformChanged( viewer.getDisplay().getTransformEventHandler().getTransform() );
		viewer.getDisplay().addOverlayRenderer( overlay );
		viewer.getDisplay().addTransformListener( overlay );

		// Behaviours.
		behaviour( new AddSpotAndLinkIt(), ADD_SPOT_AND_LINK_IT, ADD_SPOT_AND_LINK_IT_KEYS );
		behaviour( new ToggleLink(), TOGGLE_LINK, TOGGLE_LINK_KEYS );
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

				final double[] tr = screenVertexMath.getProjectCenter();
				final double theta = screenVertexMath.getProjectTheta();
				final Ellipse2D ellipse = screenVertexMath.getProjectEllipse();

				graphics.translate( tr[ 0 ], tr[ 1 ] );
				graphics.rotate( theta );
				graphics.draw( ellipse );
				graphics.setTransform( torig );
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


	private class ToggleLink implements DragBehaviour
	{

		private final V source;

		private final V target;

		private final E edgeRef;

		private boolean editing;


		public ToggleLink()
		{
			source = overlayGraph.vertexRef();
			target = overlayGraph.vertexRef();
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
				viewer.nextTimePoint();

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

					final E edge = overlayGraph.getEdge( source, target, edgeRef );
					if ( null == edge )
						overlayGraph.addEdge( source, target, edgeRef );
					else
						overlayGraph.remove( edge );

					overlayGraph.notifyGraphChanged();
					undo.setUndoPoint();
				}
				overlay.paintGhostVertex = false;
				overlay.paintGhostLink = false;
				editing = false;
			}
		}
	}

	private class AddSpotAndLinkIt implements DragBehaviour
	{
		private final V source;

		private final V target;

		private final E edge;

		private final double[] start;

		private final double[] pos;

		private boolean moving;

		private final JamaEigenvalueDecomposition eig;

		private final double[][] mat;

		public AddSpotAndLinkIt()
		{
			source = overlayGraph.vertexRef();
			target = overlayGraph.vertexRef();
			edge = overlayGraph.edgeRef();
			start = new double[ 3 ];
			pos = new double[ 3 ];
			moving = false;
			eig = new JamaEigenvalueDecomposition( 3 );
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
				viewer.nextTimePoint();

				// Compute new radius as mean of ellipse semi-axes.
				source.getCovariance( mat );
				eig.decompose( mat );
				final double[] eigs = eig.getRealEigenvalues();
				double radius = 0;
				for ( final double e : eigs )
					radius += Math.sqrt( e ) ;
				radius *= Spot.nSigmas / eigs.length;

				// Create new vertex under click location.
				final int timepoint = renderer.getCurrentTimepoint();
				overlayGraph.addVertex( timepoint, pos, radius, target );

				// Link it to source vertex.
				overlayGraph.addEdge( source, target, edge );

				// Set it as ghost link for the overlay.
				System.arraycopy( pos, 0, overlay.from, 0, pos.length );
				System.arraycopy( pos, 0, overlay.to, 0, pos.length );
				overlay.paintGhostLink = true;

				overlayGraph.notifyGraphChanged();
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
