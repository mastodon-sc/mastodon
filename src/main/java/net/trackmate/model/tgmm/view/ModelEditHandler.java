package net.trackmate.model.tgmm.view;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertexWrapper;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.model.Link;
import net.trackmate.model.tgmm.SpotCovariance;
import net.trackmate.model.tgmm.TgmmModel;
import net.trackmate.trackscheme.AbstractNamedDefaultKeyStrokeAction;
import net.trackmate.trackscheme.GraphIdBimap;
import net.trackmate.trackscheme.SelectionHandler;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeVertex;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.ViewerState;

public class ModelEditHandler implements MouseListener, MouseMotionListener
{
	private static final double DEFAULT_RADIUS = 10.;

	/**
	 * By how portion of the current radius we change this radius for every
	 * change request.
	 */
	private static final double RADIUS_CHANGE_FACTOR = 0.1;

	/** The radius below which a spot cannot go. */
	private static final double MIN_RADIUS = 2.;

	private final ViewerPanel viewer;

	private final TgmmModel model;

	private final ShowTrackScheme trackscheme;

	private final SelectionHandler selectionHandler;

	private final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance >> wrapper;

	/**
	 * Used to read current transform from {@link #viewer} state, that
	 * transforms data coordinates into user (viewer) coordinates.
	 */
	private final AffineTransform3D t = new AffineTransform3D();

	/**
	 * Used to store position in user (viewer) coordinates.
	 */
	private final RealPoint from = new RealPoint( 3 );

	/**
	 * Used to store position in data coordinates.
	 */
	private final RealPoint to = new RealPoint( 3 );

	/**
	 * Radius used to create new spots.
	 */
	private double radius = DEFAULT_RADIUS;

	/**
	 * A reference used in PoolObjectCollection methods.
	 */
	private final SpotCovariance ref;

	/**
	 * The spot currently moved by the mouse.
	 */
	private SpotCovariance movedSpot;

	private final ActionMap actionMap;

	private final InputMap inputMap;

	private boolean moving = false;

	/*
	 * CONSTRUCTOR
	 */

	public ModelEditHandler( final TgmmModel model, final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > wrapper, final ViewerPanel viewer, final ShowTrackScheme trackscheme )
	{
		this.model = model;
		this.wrapper = wrapper;
		this.viewer = viewer;
		this.trackscheme = trackscheme;
		this.selectionHandler = trackscheme.getSelectionHandler();
		this.ref = model.getGraph().vertexRef();
		this.actionMap = new ActionMap();
		this.inputMap = new InputMap();
		install();
	}

	private void install()
	{
		final HashSet< AbstractNamedDefaultKeyStrokeAction > actions = new HashSet< AbstractNamedDefaultKeyStrokeAction >();
		actions.add( new CreateSpotAction() );
		actions.add( new SelectMovedSpotAction() );
		actions.add( new DeSelectMovedSpotAction() );
		actions.add( new ChangeSpotRadiusAction( true, true ) );
		actions.add( new ChangeSpotRadiusAction( true, false ) );
		actions.add( new ChangeSpotRadiusAction( false, true ) );
		actions.add( new ChangeSpotRadiusAction( false, false ) );

		for ( final AbstractNamedDefaultKeyStrokeAction action : actions )
		{
			actionMap.put( action.name(), action );
			inputMap.put( action.getDefaultKeyStroke(), action.name() );
		}
	}

	public ActionMap getActionMap()
	{
		return actionMap;
	}

	public InputMap getDefaultInputMap()
	{
		return inputMap;
	}

	/*
	 * MOUSE METHODS.
	 */

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		new Thread( "Select spot thread." )
		{
			@Override
			public void run()
			{
				final ViewerState state = viewer.getState();
				final int timepoint = state.getCurrentTimepoint();
				final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
						wrapper.getSpatialSearch( timepoint );
				viewToData( e.getPoint(), state );
				search.search( to );
				final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
				if ( null == v )
					return;

				final double boundingSphereRadiusSquared = v.get().getBoundingSphereRadiusSquared();
				final double sqDist = search.nearestNeighborSquareDistance();
				if ( sqDist < boundingSphereRadiusSquared )
				{
					final TrackSchemeVertex tv = v.getTrackSchemeVertex();
					if ( e.isShiftDown() )
					{
						selectionHandler.select( tv, true );
					}
					else
					{
						selectionHandler.clearSelection();
						selectionHandler.select( tv, false );
					}
					repaint();
				}
			}
		}.start();
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseDragged( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		if ( !moving || movedSpot == null )
			return;

		final ViewerState state = viewer.getState();
		viewToData( e.getPoint(), state );

		movedSpot.setX( to.getDoublePosition( 0 ) );
		movedSpot.setY( to.getDoublePosition( 1 ) );
		movedSpot.setZ( to.getDoublePosition( 2 ) );
	}

	/*
	 * PRIVATE METHODS.
	 */

	private void repaint()
	{
		trackscheme.repaint();
		viewer.repaint();
	}

	private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> getSpotUnderMouse()
	{
		viewer.getGlobalMouseCoordinates( to );
		final ViewerState state = viewer.getState();
		final int timepoint = state.getCurrentTimepoint();

		final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
				wrapper.getSpatialSearch( timepoint );
		search.search( to );

		final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
		if ( null == v || search.nearestNeighborSquareDistance() > v.get().getBoundingSphereRadiusSquared() )
			return null;

		return v;
	}

	/*
	 * PRIVATE METHODS
	 */
	
	/**
	 * Places the data coordinates corresponding to the specified point (in view
	 * coordinates) in to the {@link #to} field.
	 * 
	 * @param ml
	 * @param state
	 */
	private final void viewToData( final Point ml, final ViewerState state )
	{
		state.getViewerTransform( t );
		from.setPosition( ml.getX(), 0 );
		from.setPosition( ml.getY(), 1 );
		from.setPosition( 0., 2 );
		t.applyInverse( to, from );
	}

	/*
	 * ACTIONS
	 */

	private class SelectMovedSpotAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		public SelectMovedSpotAction()
		{
			super( "selectMovedSpot", KeyStroke.getKeyStroke( ' ' ) );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( moving )
				return;
			moving = true;

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			movedSpot = v.get();
		}
	}

	private class DeSelectMovedSpotAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		public DeSelectMovedSpotAction()
		{
			super( "deSelectMovedSpot", KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0, true ) );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			wrapper.updateSearchFor( movedSpot.getTimepointId() );
			movedSpot = null;
			moving = false;
		}
	}

	private class ChangeSpotRadiusAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		private final double factor;

		public ChangeSpotRadiusAction( final boolean fast, final boolean increase )
		{
			super(
					fast ?
							increase ? "increaseSpotRadiusFast" : "decreaseSpotRadiusFast"
							:
							increase ? "increaseSpotRadius" : "decreaseSpotRadius",
					fast ?
							increase ? KeyStroke.getKeyStroke( "shift E" ) : KeyStroke.getKeyStroke( "shift Q" )
							:
							increase ? KeyStroke.getKeyStroke( 'e' ) : KeyStroke.getKeyStroke( 'q' ) );

			this.factor = increase ?
					fast ? 10. : 1.
					:
					fast ? -5. : -1.;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			final SpotCovariance spot = v.get();

			double rad = Math.sqrt( spot.getBoundingSphereRadiusSquared() );
			rad += factor * RADIUS_CHANGE_FACTOR * rad;

			if ( rad < MIN_RADIUS )
			{
				rad = MIN_RADIUS;
			}

			radius = rad;
			spot.editRadius( radius );
			viewer.getDisplay().repaint();
		}

	}
	
	private class CreateSpotAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		private final double[] loc = new double[ 3 ];

		private final GraphIdBimap< SpotCovariance, Link< SpotCovariance >> idBimap;

		public CreateSpotAction()
		{
			super( "createSpot", KeyStroke.getKeyStroke( 'a' ) );
			this.idBimap = model.getGraph().getIdBimap();
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			viewer.getGlobalMouseCoordinates( to );
			to.localize( loc );
			final ViewerState state = viewer.getState();
			final int timepoint = state.getCurrentTimepoint();
			final SpotCovariance spot = model.createSpot( timepoint, loc, radius, ref );

			/*
			 * FIXME How to keep this in sync with the wrapper and TrackScheme?
			 * Discuss with @tpietzsch. Do this manually here? Have the model
			 * implements Listenable? Right now I am doing this manually.
			 */

			final int id = idBimap.getVertexId( spot );
			trackscheme.getGraph().addVertex().init( id, "Created!", timepoint, false );
			wrapper.add( timepoint, id );
			trackscheme.relayout();
			repaint();
		}
	}

}
