package org.mastodon.views.dbvv;

import bdv.TransformEventHandler3D;
import bdv.TransformState;
import bdv.cache.CacheControl;
import bdv.viewer.RequestRepaint;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SynchronizedViewerState;
import bdv.viewer.TimePointListener;
import bdv.viewer.TransformListener;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerStateChange;
import bdv.viewer.ViewerStateChangeListener;
import bdv.viewer.render.PainterThread;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.ViewerState;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.bvv.BvvOptions;
import org.scijava.listeners.Listeners;
import tpietzsch.example2.InteractiveGLDisplayCanvas;

public class DBvvPanel
		extends JPanel
		implements RequestRepaint, PainterThread.Paintable, ViewerStateChangeListener
{
	/**
	 * Currently rendered state (visible sources, transformation, timepoint,
	 * etc.) A copy can be obtained by {@link #getState()}.
	 */
	private final ViewerState state;

	private final PainterThread painterThread;

	private final TransformEventHandler3D transformEventHandler;

	private final DBvvEntities entities;

	private final DBvvRenderer renderer;

	private final InteractiveGLDisplayCanvas display;

	private final JSlider sliderTime;

	private boolean blockSliderTimeEvents;

	/**
	 * These listeners will be notified about changes to the current timepoint
	 * {@link ViewerState#getCurrentTimepoint()}. This is done <em>before</em>
	 * calling {@link #requestRepaint()} so listeners have the chance to
	 * interfere.
	 */
	private final Listeners.List< TimePointListener > timePointListeners;

	private final Listeners.List< TransformListener< AffineTransform3D > > transformListeners;

	public DBvvPanel(
			final ModelGraph viewGraph,
			final SelectionModel< Spot, Link > selection,
			final HighlightModel< Spot, Link > highlight,
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

		final int displayWidth = options.getWidth();
		final int displayHeight = options.getHeight();

		painterThread = new PainterThread( this );
		entities = new DBvvEntities( viewGraph );
		renderer = new DBvvRenderer( displayWidth, displayHeight, viewGraph, entities, selection, highlight );
		transformEventHandler = new TransformEventHandler3D(
				TransformState.from( state()::getViewerTransform, state()::setViewerTransform ) );

		display = new InteractiveGLDisplayCanvas( displayWidth, displayHeight );
		display.setTransformEventHandler( transformEventHandler );
		display.addGLEventListener( glEventListener );
		display.canvasSizeListeners().add( renderer::setScreenSize );

		sliderTime = new JSlider( SwingConstants.HORIZONTAL, 0, numTimepoints - 1, 0 );
		sliderTime.addChangeListener( e -> {
			if ( !blockSliderTimeEvents )
				setTimepoint( sliderTime.getValue() );
		} );

		add( display, BorderLayout.CENTER );
		if ( numTimepoints > 1 )
			add( sliderTime, BorderLayout.SOUTH );
		setFocusable( false );

		timePointListeners = new Listeners.SynchronizedList<>( l -> l.timePointChanged( state().getCurrentTimepoint() ) );
		transformListeners = new Listeners.SynchronizedList<>( l -> l.transformChanged( state().getViewerTransform() ) );

		state().changeListeners().add( this );

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
		state().setCurrentTimepoint( timepoint );
	}

	public TransformEventHandler3D getTransformEventHandler()
	{
		return transformEventHandler;
	}

	public DBvvRenderer getRenderer()
	{
		return renderer;
	}

	public Listeners< TimePointListener > timePointListeners()
	{
		return timePointListeners;
	}

	public Listeners< TransformListener< AffineTransform3D > > transformListeners()
	{
		return transformListeners;
	}

	/**
	 * @deprecated Use {@link #state()} instead.
	 *
	 * Get a copy of the current {@link ViewerState}.
	 *
	 * @return a copy of the current {@link ViewerState}.
	 */
	@Deprecated
	public ViewerState getState()
	{
		return state.copy();
	}

	/**
	 * Get the ViewerState. This can be directly used for modifications, e.g.,
	 * adding/removing sources etc. See {@link SynchronizedViewerState} for
	 * thread-safety considerations.
	 */
	public SynchronizedViewerState state()
	{
		return state.getState();
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

	/**
	 * Get the viewer canvas.
	 *
	 * @return the viewer canvas.
	 */
	public InteractiveGLDisplayCanvas getDisplay()
	{
		return display;
	}

	@Override
	public void paint()
	{
		display.display();
	}

	@Override
	public void viewerStateChanged( final ViewerStateChange change )
	{
		switch ( change )
		{
		case CURRENT_SOURCE_CHANGED:
		case DISPLAY_MODE_CHANGED:
		case GROUP_NAME_CHANGED:
		case CURRENT_GROUP_CHANGED:
		case SOURCE_ACTIVITY_CHANGED:
		case GROUP_ACTIVITY_CHANGED:
		case VISIBILITY_CHANGED:
		case SOURCE_TO_GROUP_ASSIGNMENT_CHANGED:
		case NUM_SOURCES_CHANGED:
		case NUM_GROUPS_CHANGED:
		case INTERPOLATION_CHANGED:
		case NUM_TIMEPOINTS_CHANGED:
			break;
		case CURRENT_TIMEPOINT_CHANGED:
		{
			final int timepoint = state().getCurrentTimepoint();
			SwingUtilities.invokeLater( () -> {
				blockSliderTimeEvents = true;
				if ( sliderTime.getValue() != timepoint )
					sliderTime.setValue( timepoint );
				blockSliderTimeEvents = false;
			} );
			timePointListeners.list.forEach( l -> l.timePointChanged( timepoint ) );
			requestRepaint();
			break;
		}
		case VIEWER_TRANSFORM_CHANGED:
			final AffineTransform3D transform = state().getViewerTransform();
			transformListeners.list.forEach( l -> l.transformChanged( transform ) );
			requestRepaint();
		}
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
