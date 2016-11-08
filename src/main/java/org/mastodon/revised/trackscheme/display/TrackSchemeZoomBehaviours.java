/**
 *
 */
package org.mastodon.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;
import org.mastodon.revised.trackscheme.display.animate.InterpolateScreenTransformAnimator;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;

/**
 * Drag behaviour that implements a zoom rectangle in TrackScheme.
 * <p>
 * This class depends on the {@link TransformEventHandler} of the TrackScheme
 * display to be an {@link InertialScreenTransformEventHandler}, to be able to
 * pass it a transform animator that will execute the zoom.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class TrackSchemeZoomBehaviours< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
		extends Behaviours
{

	private static final String TOGGLE_ZOOM = "trackscheme zoom";

	private static final ImageIcon ZOOM_ICON = new ImageIcon( TrackSchemeZoomBehaviours.class.getResource( "zoom.png" ) );

	private static final String[] TOGGLE_ZOOM_KEYS = new String[] { "Z" };

	public static final Color ZOOM_GRAPH_OVERLAY_COLOR = Color.BLUE.darker();

	private final TrackSchemePanel panel;

	private final InertialScreenTransformEventHandler transformEventHandler;

	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final TrackSchemePanel panel )
	{
		new TrackSchemeZoomBehaviours<>( config, panel )
				.install( triggerBehaviourBindings, "ts zoom" );
	}

	private TrackSchemeZoomBehaviours(
			final InputTriggerConfig config,
			final TrackSchemePanel panel )
	{
		super( config, new String[] { "ts" } );
		this.panel = panel;
		this.transformEventHandler = ( InertialScreenTransformEventHandler ) panel.getDisplay().getTransformEventHandler();

		// Create and register overlay.
		final ZoomRectangle zoom = new ZoomRectangle();
		zoom.transformChanged( panel.getDisplay().getTransformEventHandler().getTransform() );
		zoom.updateHeadersVisibility( panel.getOffsetDecorations().isVisibleX(), panel.getOffsetDecorations().getWidth(), 
				panel.getOffsetDecorations().isVisibleY(), panel.getOffsetDecorations().getHeight() );
		// put the overlay first, so that is below the graph rendering.
		panel.getDisplay().addOverlayRenderer( zoom.overlay );
		panel.getDisplay().addTransformListener( zoom );
		panel.getOffsetDecorations().addOffsetHeadersListener( zoom );

		// Behaviours.
		behaviour( zoom, TOGGLE_ZOOM, TOGGLE_ZOOM_KEYS );
	}

	public void zoomTo( final ScreenTransform tstart, final ScreenTransform tend )
	{
		ConstrainScreenTransform.removeJitter( tend, tstart );
		if ( !tend.equals( tstart ) )
		{
			final InterpolateScreenTransformAnimator animator = new InterpolateScreenTransformAnimator( tstart, tend, 200 );
			transformEventHandler.setAnimator( animator );
			transformEventHandler.runAnimation();
		}
	}

	private class ZoomRectangle implements DragBehaviour, OffsetHeadersListener, TransformListener< ScreenTransform >
	{

		private boolean editing;

		private int headerWidth;

		private int headerHeight;

		private final ScreenTransform screenTransform;

		private final ZoomOverlay overlay;

		public ZoomRectangle()
		{
			editing = false;
			screenTransform = new ScreenTransform();
			overlay = new ZoomOverlay();
		}

		@Override
		public void updateHeadersVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
		{
			headerWidth = isVisibleX ? width : 0;
			headerHeight = isVisibleY ? height : 0;
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
		public void init( final int x, final int y )
		{
			overlay.ox = x;
			overlay.oy = y;
			overlay.ex = x;
			overlay.ey = y;
			editing = true;
			overlay.paint = true;
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( editing )
			{
				overlay.ex = x;
				overlay.ey = y;
				panel.repaint();
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( editing )
			{
				editing = false;
				overlay.paint = false;

				final int x1 = Math.min( overlay.ox, overlay.ex ) - headerWidth;
				final int x2 = Math.max( overlay.ox, overlay.ex ) - headerWidth;
				final int y1 = Math.min( overlay.oy, overlay.ey ) - headerHeight;
				final int y2 = Math.max( overlay.oy, overlay.ey ) - headerHeight;
				final double[] screen1 = new double[] { x1, y1 };
				final double[] screen2 = new double[] { x2, y2 };
				final double[] layout1 = new double[ 2 ];
				final double[] layout2 = new double[ 2 ];

				screenTransform.applyInverse( layout1, screen1 );
				screenTransform.applyInverse( layout2, screen2 );

				final ScreenTransform tstart = transformEventHandler.getTransform();
				final ScreenTransform tend = new ScreenTransform(
						layout1[ 0 ],
						layout2[ 0 ],
						layout1[ 1 ],
						layout2[ 1 ],
						tstart.getScreenWidth(), tstart.getScreenHeight() );
				zoomTo( tstart, tend );
			}
		}

		private class ZoomOverlay implements OverlayRenderer
		{

			public int ey;

			public int ex;

			public int oy;

			public int ox;

			private boolean paint;

			public ZoomOverlay()
			{
				paint = false;
			}

			@Override
			public void drawOverlays( final Graphics g )
			{
				if ( !paint )
					return;

				final int x1 = Math.min( ox, ex );
				final int x2 = Math.max( ox, ex );
				final int y1 = Math.min( oy, ey );
				final int y2 = Math.max( oy, ey );

				g.setColor( ZOOM_GRAPH_OVERLAY_COLOR );
				g.drawRect( x1, y1, x2 - x1, y2 - y1 );
				g.drawImage( ZOOM_ICON.getImage(), x1 + 3, y1 + 3, null );
			}

			@Override
			public void setCanvasSize( final int width, final int height )
			{}
		}
	}
}
