package org.mastodon.views.bvv;

import bdv.cache.CacheControl;
import bdv.viewer.RequestRepaint;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerOptions;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.ViewerState;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.PainterThread;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import tpietzsch.util.TransformHandler;

public class BvvPanel
		extends JPanel
		implements RequestRepaint
{
	/**
	 * Currently rendered state (visible sources, transformation, timepoint,
	 * etc.) A copy can be obtained by {@link #getState()}.
	 */
	private final ViewerState state;

	private final PainterThread painterThread;

	private final TransformHandler transformHandler;

	private final BvvRenderer< ?, ? > renderer;

	private final GLCanvas canvas;

	private final JSlider sliderTime;

	/**
	 * These listeners will be notified about changes to the current timepoint
	 * {@link ViewerState#getCurrentTimepoint()}. This is done <em>before</em>
	 * calling {@link #requestRepaint()} so listeners have the chance to
	 * interfere.
	 */
	private final CopyOnWriteArrayList< TimePointListener > timePointListeners;

	public < V extends BvvVertex< V, E >, E extends BvvEdge< E, V > > BvvPanel(
			final BvvGraph< V, E > viewGraph,
			final SelectionModel< V, E > selection,
			final HighlightModel< V, E > highlight,
			final List< SourceAndConverter< ? > > sources,
			final int numTimepoints,
			final CacheControl cacheControl,
			final ViewerOptions optional,
			final BvvOptions bvvOptional )
	{
		super( new BorderLayout() );

		final ViewerOptions.Values options = optional.values;
		final BvvOptions.Values bvvOptions = bvvOptional.values;

		final int numGroups = options.getNumSourceGroups();
		final ArrayList< SourceGroup > groups = new ArrayList<>( numGroups );
		for ( int i = 0; i < numGroups; ++i )
			groups.add( new SourceGroup( "group " + Integer.toString( i + 1 ) ) );
		state = new ViewerState( sources, groups, numTimepoints );
		for ( int i = Math.min( numGroups, sources.size() ) - 1; i >= 0; --i )
			state.getSourceGroups().get( i ).addSource( i );

		if ( !sources.isEmpty() )
			state.setCurrentSource( 0 );

		painterThread = new PainterThread( this::paint );
		renderer = new BvvRenderer<>( 640, 480, viewGraph, selection, highlight );

		final GLCapabilities capsReqUser = new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) );
		canvas = new GLCanvas( capsReqUser );
		canvas.setPreferredSize( new Dimension( options.getWidth(), options.getHeight() ) );
		canvas.addGLEventListener( glEventListener );

		sliderTime = new JSlider( SwingConstants.HORIZONTAL, 0, numTimepoints - 1, 0 );
		sliderTime.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				if ( e.getSource().equals( sliderTime ) )
					setTimepoint( sliderTime.getValue() );
			}
		} );

		add( canvas, BorderLayout.CENTER );
		if ( numTimepoints > 1 )
			add( sliderTime, BorderLayout.SOUTH );

		transformHandler = new TransformHandler();
		transformHandler.setCanvasSize( canvas.getWidth(), canvas.getHeight(), false );

					// TODO: FIX THIS HACK.
					//  This is just setting some transform that works ok for my default testing dataset
					//  Instead, initialize to a good initial view either based on the image data, or
					//  by looking at the transform of the last active BDV window
					final AffineTransform3D initialTransform = new AffineTransform3D();
					initialTransform.scale( 4 );

		transformHandler.setTransform( initialTransform );
		transformHandler.listeners().add( this::transformChanged );

		timePointListeners = new CopyOnWriteArrayList<>();

		painterThread.start();
	}

	/**
	 * Show the specified time-point.
	 *
	 * @param timepoint
	 *            time-point index.
	 */
	public synchronized void setTimepoint( final int timepoint )
	{
		if ( state.getCurrentTimepoint() != timepoint )
		{
			state.setCurrentTimepoint( timepoint );
			sliderTime.setValue( timepoint );
			for ( final TimePointListener l : timePointListeners )
				l.timePointChanged( timepoint );
			requestRepaint();
		}
	}

	public TransformHandler getTransformEventHandler()
	{
		return transformHandler;
	}

	/**
	 * Get a copy of the current {@link ViewerState}.
	 *
	 * @return a copy of the current {@link ViewerState}.
	 */
	public ViewerState getState()
	{
		return state.copy();
	}

	/**
	 * Repaint as soon as possible.
	 */
	@Override
	public void requestRepaint()
	{
		painterThread.requestRepaint();
	}

	/**
	 * Stop the painter thread.
	 */
	public void stop()
	{
		painterThread.interrupt();
	}

	public Component getDisplay()
	{
		return canvas;
	}

	private void paint()
	{
		canvas.display();
	}

	private void transformChanged( final AffineTransform3D transform )
	{
		state.setViewerTransform( transform );
		requestRepaint();
	}

	private final GLEventListener glEventListener = new GLEventListener()
	{
		private final AffineTransform3D worldToScreen = new AffineTransform3D();

		@Override
		public void init( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			renderer.init( gl );
		}

		@Override
		public void display( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			state.getViewerTransform( worldToScreen );
			final int timepoint = state.getCurrentTimepoint();
			renderer.display( gl, worldToScreen, timepoint );
		}

		@Override
		public void reshape( final GLAutoDrawable drawable, final int x, final int y, final int width, final int height )
		{
		}

		@Override
		public void dispose( final GLAutoDrawable drawable )
		{
		}
	};
}
