package net.trackmate.model.tgmm.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertexWrapper;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.graph.util.Graphs;
import net.trackmate.model.Link;
import net.trackmate.model.tgmm.SpotCovariance;
import net.trackmate.model.tgmm.TgmmModel;
import net.trackmate.model.tgmm.view.ModelEditListener.ModelEditEvent;
import net.trackmate.trackscheme.AbstractNamedDefaultKeyStrokeAction;
import net.trackmate.trackscheme.GraphIdBimap;
import net.trackmate.trackscheme.SelectionHandler;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeEdge;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.ViewerState;

public class ModelEditHandler implements MouseListener, MouseMotionListener, OverlayRenderer, KeyListener
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

	private final GraphIdBimap< SpotCovariance, Link< SpotCovariance >> idBimap;

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

	private final ActionMap actionMap;

	private final InputMap inputMap;

	private final GhostOverlay overlay;

	private final SpotMover spotMover;

	private final LinkedSpotCreator linkedSpotCreator;

	private final LinkCreator linkCreator;

	private final SpotCreator spotCreator;

	private final HashSet< ModelEditListener > listeners = new HashSet< ModelEditListener >();

	private final LinkRemover linkRemover;

	/*
	 * CONSTRUCTOR
	 */


	public ModelEditHandler( final TgmmModel model, final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > wrapper, final ViewerPanel viewer, final ShowTrackScheme trackscheme )
	{
		this.model = model;
		this.idBimap = model.getGraph().getIdBimap();
		this.wrapper = wrapper;
		this.viewer = viewer;
		this.trackscheme = trackscheme;
		this.selectionHandler = trackscheme.getSelectionHandler();
		this.ref = model.getGraph().vertexRef();
		this.actionMap = new ActionMap();
		this.inputMap = new InputMap();
		this.overlay = new GhostOverlay();
		this.spotMover = new SpotMover( ' ' );
		this.linkedSpotCreator = new LinkedSpotCreator( 's' );
		this.linkCreator = new LinkCreator( 'a' );
		this.spotCreator = new SpotCreator( 'a' );
		this.linkRemover = new LinkRemover( 'd' );
		install();
	}

	private void install()
	{
		final HashSet< AbstractNamedDefaultKeyStrokeAction > actions = new HashSet< AbstractNamedDefaultKeyStrokeAction >();
		actions.add( new ChangeSpotRadiusAction( true, true ) );
		actions.add( new ChangeSpotRadiusAction( true, false ) );
		actions.add( new ChangeSpotRadiusAction( false, true ) );
		actions.add( new ChangeSpotRadiusAction( false, false ) );
//		actions.add( new DeleteSpotAction() );

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

	public boolean addModelEditListener( final ModelEditListener listener )
	{
		return listeners.add( listener );
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
				final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
						wrapper.getSpatialSearch( viewer.getState().getCurrentTimepoint() );
				viewer.getGlobalMouseCoordinates( to );
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
					viewer.repaint();
					trackscheme.repaint();
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
		spotMover.move();
		linkCreator.move();
		linkRemover.findTarget();
	}

	@Override
	public void keyPressed( final KeyEvent e )
	{
		final char key = e.getKeyChar();

		if ( Character.toLowerCase( key ) == Character.toLowerCase( spotCreator.key ) )
		{
			spotCreator.create();
		}
		if ( !spotCreator.creating && Character.toLowerCase( key ) == Character.toLowerCase( linkCreator.key ) )
		{
			linkCreator.create( !e.isShiftDown() );
		}
		if ( key == spotMover.key )
		{
			spotMover.grab();
		}
		if ( Character.toLowerCase( key ) == Character.toLowerCase( linkedSpotCreator.key ) )
		{
			linkedSpotCreator.create( !e.isShiftDown() );
		}
		if ( key == linkRemover.key )
		{
			linkRemover.findSource();
		}
	}

	@Override
	public void keyReleased( final KeyEvent e )
	{
		final char key = e.getKeyChar();
		if ( Character.toLowerCase( key ) == Character.toLowerCase( spotMover.key ) )
		{
			if ( spotMover.get() != null )
			{
				final SpotCovariance spot = spotMover.get();
				final TrackSchemeVertex tv = getTrackSchemeVertex( spot );
				String str;
				if ( null == tv.getLabel() || tv.getLabel() == "" )
				{
					str = "moved spot " + tv.getLabel() + " to X=%.1f, Y=%.1f, Z=%.1f, t=%d";
				}
				else
				{
					str = "moved spot ID=" + tv.getInternalPoolIndex() + " to X=%.1f, Y=%.1f, Z=%.1f, t=%d";
				}
				viewer.showMessage( String.format( str, spot.getX(), spot.getY(), spot.getZ(), spot.getTimepoint() ) );
			}
			spotMover.release();

		}
		if ( Character.toLowerCase( key ) == Character.toLowerCase( linkedSpotCreator.key ) )
		{
			if ( spotMover.movedSpot != null )
			{
				final SpotCovariance spot = spotMover.movedSpot;
				final TrackSchemeVertex tv = getTrackSchemeVertex( spot );
				String str;
				if ( null == tv.getLabel() || tv.getLabel() == "" )
				{
					str = "created spot " + tv.getLabel() + " at X=%.1f, Y=%.1f, Z=%.1f, t=%d";
				}
				else
				{
					str = "created spot ID=" + tv.getInternalPoolIndex() + " at X=%.1f, Y=%.1f, Z=%.1f, t=%d";
				}
				viewer.showMessage( String.format( str, spot.getX(), spot.getY(), spot.getZ(), spot.getTimepoint() ) );
			}
			linkedSpotCreator.release();
		}
		if ( Character.toLowerCase( key ) == Character.toLowerCase( linkCreator.key ) )
		{
			linkCreator.release();
		}
		if ( Character.toLowerCase( key ) == Character.toLowerCase( spotCreator.key ) )
		{
			spotCreator.release();
		}
		if ( Character.toLowerCase( key ) == Character.toLowerCase( linkRemover.key ) )
		{
			linkRemover.release();
		}
	}

	@Override
	public void keyTyped( final KeyEvent e )
	{}

	/*
	 * PRIVATE METHODS.
	 */

	private void fireModelEditEvent()
	{
		final ModelEditEvent event = new ModelEditEvent();
		for ( final ModelEditListener listener : listeners )
		{
			listener.modelEdited( event );
		}
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
	
	private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> getMouseClosestSpot()
	{
		viewer.getGlobalMouseCoordinates( to );
		final ViewerState state = viewer.getState();
		final int timepoint = state.getCurrentTimepoint();

		final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
				wrapper.getSpatialSearch( timepoint );
		search.search( to );

		final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
		return v;
	}

	/**
	 * Creates a new {@link SpotCovariance} at the specified location and
	 * timepoint. Also makes sure it is properly added to the
	 * {@link TrackSchemeGraph} with the right id, and that the spatial search
	 * is updated with the new spot.
	 * 
	 * @param loc
	 *            the spatial location of the spot in global coordinates.
	 * @param timepoint
	 *            the timepoint to add it to.
	 * @return the spot created.
	 */
	private SpotCovariance createSpot( final double[] loc, final int timepoint )
	{
		final SpotCovariance spot = model.createSpot( timepoint, loc, radius, ref );
		final int id = idBimap.getVertexId( spot );
		trackscheme.getGraph().addVertex().init( id, "" + id, timepoint, false );
		wrapper.add( timepoint, id );
		return spot;
	}

	private boolean deleteSpot( final SpotCovariance spot )
	{
		final int timepoint = spot.getTimepoint();
		trackscheme.getGraph().remove( getTrackSchemeVertex( spot ) );
		final boolean ok = model.deleteSpot( spot );
		wrapper.refresh( timepoint );
		return ok;
	}

	private Link< SpotCovariance > createLink( final SpotCovariance source, final SpotCovariance target )
	{
		final SpotCovariance s, t;
		final int ttp = target.getTimepoint();
		final int stp = source.getTimepoint();
		if ( stp == ttp )
		{
			viewer.showMessage( "cannot create a link between two spots belonging to the same timepoint" );
			return null;
		}
		if ( ttp > stp )
		{
			s = source;
			t = target;
		}
		else
		{
			s = target;
			t = source;
		}

		final Link< SpotCovariance > link = model.createLink( s, t );
		trackscheme.getGraph().addEdge( getTrackSchemeVertex( s ), getTrackSchemeVertex( t ) );
		return link;
	}

	private void deleteLink( final SpotCovariance source, final SpotCovariance target )
	{
		final TrackSchemeVertex ts = getTrackSchemeVertex( source );
		final TrackSchemeVertex tt = getTrackSchemeVertex( target );
		TrackSchemeEdge tedge = trackscheme.getGraph().getEdge( ts, tt );
		if ( null == tedge )
		{
			tedge = trackscheme.getGraph().getEdge( tt, ts );
		}
		trackscheme.getGraph().remove( tedge );
		model.removeLink( source, target );
	}

	private TrackSchemeVertex getTrackSchemeVertex( final SpotCovariance spot )
	{
		final int id = model.getGraph().getIdBimap().getVertexId( spot );
		final TrackSchemeVertex tsv = trackscheme.getGraph().vertexRef();
		trackscheme.getGraph().getVertexPool().getByInternalPoolIndex( id, tsv );
		return tsv;
	}

	/*
	 * INNER CLASSES
	 */

	private class SpotMover
	{
		/**
		 * The spot currently moved by the mouse.
		 */
		private SpotCovariance movedSpot;

		private final char key;

		public SpotMover( final char key )
		{
			this.key = key;
		}

		private void grab()
		{
			if ( null != movedSpot )
				return;

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			grab( v.get() );
		}

		private void grab( final SpotCovariance spot )
		{
			movedSpot = spot;
		}

		private SpotCovariance get()
		{
			return movedSpot;
		}

		private void release()
		{
			if ( movedSpot == null )
				return;

			wrapper.updateSearchFor( movedSpot.getTimepoint() );
			movedSpot = null;
		}
		
		private void move()
		{
			if ( movedSpot == null )
				return;

			viewer.getGlobalMouseCoordinates( to );
			movedSpot.setX( to.getDoublePosition( 0 ) );
			movedSpot.setY( to.getDoublePosition( 1 ) );
			movedSpot.setZ( to.getDoublePosition( 2 ) );
		}
	}

	private class SpotCreator
	{
		private final double[] loc = new double[ 3 ];

		private final char key;

		private boolean creating = false;

		public SpotCreator( final char key )
		{
			this.key = key;
		}

		private void create()
		{
			// Only create if there is no spot under mouse.
			if ( creating || null != getSpotUnderMouse() )
				return;

			viewer.getGlobalMouseCoordinates( to );
			to.localize( loc );
			final SpotCovariance spot = createSpot( loc, viewer.getState().getCurrentTimepoint() );
			spotMover.grab( spot );
			creating = true;
		}

		private void release()
		{
			if ( !creating )
				return;

			final SpotCovariance spot = spotMover.get();
			final TrackSchemeVertex tv = getTrackSchemeVertex( spot );
			String str;
			if ( null == tv.getLabel() || tv.getLabel() == "" )
			{
				str = "created spot " + tv.getLabel() + " at X=%.1f, Y=%.1f, Z=%.1f, t=%d";
			}
			else
			{
				str = "created spot ID=" + tv.getInternalPoolIndex() + " at X=%.1f, Y=%.1f, Z=%.1f, t=%d";
			}
			viewer.showMessage( String.format( str, spot.getX(), spot.getY(), spot.getZ(), spot.getTimepoint() ) );
			spotMover.release();
			creating = false;
			fireModelEditEvent();
		}
	}

	private class LinkedSpotCreator
	{
		private final double[] loc = new double[ 3 ];

		private final char key;

		private LinkedSpotCreator( final char key )
		{
			this.key = key;
		}

		private void create( final boolean forward )
		{
			/*
			 * Check if a spot is within radius.
			 */

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;
			overlay.paint( v.get() );

			/*
			 * Move to next timepoint.
			 */

			final ViewerState state = viewer.getState();
			final int timepoint = state.getCurrentTimepoint();
			final int newTimePoint = forward ? timepoint + 1 : timepoint - 1;
			viewer.setTimepoint( newTimePoint );

			/*
			 * Create spot and link there.
			 */

			viewer.getGlobalMouseCoordinates( to );
			to.localize( loc );
			final SpotCovariance spot = createSpot( loc, newTimePoint );
			createLink( v.get(), spot );

			// Make new spot moveable.
			spotMover.grab( spot );
		}

		private void release()
		{
			spotMover.release();
			overlay.clear();
			fireModelEditEvent();
		}
	}

	private class LinkCreator
	{
		private final char key;

		private boolean creating = false;

		private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> target;

		private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> source;

		private double[] linkTarget;

		private LinkCreator( final char key )
		{
			this.key = key;
		}

		private void create( final boolean forward )
		{
			// Check if a spot is within radius.
			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( creating || null == v )
				return;

			overlay.paint( v.get() );
			linkTarget = overlay.setPaintGhostLink( true );
			viewer.getGlobalMouseCoordinates( to );
			to.localize( linkTarget );
			creating = true;
			source = v;
			target = null;
			creating = true;
			final int timepoint = viewer.getState().getCurrentTimepoint();
			final int newTimepoint = forward ? timepoint + 1 : timepoint - 1;
			viewer.setTimepoint( newTimepoint  );
		}

		private void move()
		{
			if ( !creating )
				return;

			viewer.getGlobalMouseCoordinates( to );
			final int timepoint = viewer.getState().getCurrentTimepoint();
			final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >>> search = wrapper.getSpatialSearch( timepoint );
			search.search( to );

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
			if ( v == null )
			{
				to.localize( linkTarget );
				return;
			}

			target = v;
			final double sqDist = search.nearestNeighborSquareDistance();
			final double snapSqDist = target.get().getBoundingSphereRadiusSquared();
			if ( sqDist < snapSqDist )
			{
				target.localize( linkTarget );
			}
			else
			{
				target = null;
				to.localize( linkTarget );
			}
		}

		private void release()
		{
			if ( !creating )
				return;

			if ( null != target )
			{
				if ( null == createLink( source.get(), target.get() ) )
					return;

				String str = "created link from spot ";
				if ( source.getTrackSchemeVertex().getLabel() == null || source.getTrackSchemeVertex().getLabel() == "" )
				{
					str += "ID=" + source.getInternalPoolIndex();
				}
				else
				{
					str += source.getTrackSchemeVertex().getLabel();
				}
				str += " at t=" + source.getTimepoint() + " to spot ";
				if ( target.getTrackSchemeVertex().getLabel() == null || target.getTrackSchemeVertex().getLabel() == "" )
				{
					str += "ID=" + target.getInternalPoolIndex();
				}
				else
				{
					str += target.getTrackSchemeVertex().getLabel();
				}
				str += " at t=" + target.getTimepoint();
				viewer.showMessage( str );
				target = null;
			}
			creating = false;
			source = null;
			overlay.clear();
			fireModelEditEvent();
		}
	}

	private class LinkRemover
	{
		private final char key;

		private boolean removing = false;

		private SpotCovariance source;

		private final AffineTransform3D t = new AffineTransform3D();

		private final RealPoint sview = new RealPoint( 3 );

		private final RealPoint tview = new RealPoint( 3 );

		private final RealPoint eview = new RealPoint( 3 );

		private SpotCovariance choice;

		private LinkRemover( final char key )
		{
			this.key = key;
		}

		private void findSource()
		{
			if ( removing || getSpotUnderMouse() != null )
				return;

			removing = true;
			source = getMouseClosestSpot().get();
			source.localize( overlay.setPaintGhostLink( true ) );
		}

		private void findTarget()
		{
			if ( !removing )
				return;

			viewer.getState().getViewerTransform( t );
			viewer.getGlobalMouseCoordinates( to );
			t.applyInverse( eview, to );

			// source coords in view space.
			to.setPosition( source );
			t.applyInverse( sview, to );

			double min = Double.POSITIVE_INFINITY;
			choice = null;
			for ( final Link< SpotCovariance > link : source.edges() )
			{
				/*
				 * Compute distance of mouse to link segment.
				 */

				final SpotCovariance target = Graphs.getOppositeVertex( link, source, model.getGraph().vertexRef() );
				to.setPosition( target );
				t.applyInverse( tview, to );
				// We only care for XY projection.
				final double d2 = sqdist( sview, tview );
				final double t = (
						( eview.getDoublePosition( 0 ) - sview.getDoublePosition( 0 ) ) * ( tview.getDoublePosition( 0 ) - sview.getDoublePosition( 0 ) )
								+
						( eview.getDoublePosition( 1 ) - sview.getDoublePosition( 1 ) ) * ( tview.getDoublePosition( 1 ) - sview.getDoublePosition( 1 ) )
						) / d2;
				
				final double dist2;
				if (t < 0)
				{
					dist2 = sqdist( sview, eview );
				}
				else if ( t > 1 )
				{
					dist2 = sqdist( tview, eview );
				}
				else
				{
					final double xproj = sview.getDoublePosition( 0 ) + t * ( tview.getDoublePosition( 0 ) - sview.getDoublePosition( 0 ) );
					final double yproj = sview.getDoublePosition( 1 ) + t * ( tview.getDoublePosition( 1 ) - sview.getDoublePosition( 1 ) );
					final RealPoint proj = new RealPoint( xproj, yproj, 0. );
					dist2 = sqdist( proj, eview );
				}

				if ( dist2 < min )
				{
					min = dist2;
					choice = target;
				}
			}

			if ( null == choice )
				return;

			overlay.paint( choice );
		}

		private void release()
		{
			if ( !removing )
				return;

			if ( null != choice )
			{
				deleteLink( source, choice );
				String str = "removed link from spot ";
				if ( getTrackSchemeVertex( source ).getLabel() == null || getTrackSchemeVertex( source ).getLabel() == "" )
				{
					str += "ID=" + source.getInternalPoolIndex();
				}
				else
				{
					str += getTrackSchemeVertex( source ).getLabel();
				}
				str += " at t=" + source.getTimepoint() + " to spot ";
				if ( getTrackSchemeVertex( choice ).getLabel() == null || getTrackSchemeVertex( choice ).getLabel() == "" )
				{
					str += "ID=" + choice.getInternalPoolIndex();
				}
				else
				{
					str += getTrackSchemeVertex( choice ).getLabel();
				}
				str += " at t=" + choice.getTimepoint();
				viewer.showMessage( str );
				choice = null;
			}
			source = null;
			removing = false;
			overlay.clear();
			fireModelEditEvent();
		}

		private final double sqdist( final RealPoint p1, final RealPoint p2 )
		{
			double s = 0;
			for ( int d = 0; d < 2; d++ )
			{
				final double dx = p2.getDoublePosition( d ) - p1.getDoublePosition( d );
				s += dx * dx;
			}
			return s;
		}
	}

	/*
	 * ACTIONS
	 */

	private class DeleteSpotAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		public DeleteSpotAction()
		{
			super( "deleteSpot", KeyStroke.getKeyStroke( 'd' ) );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			final SpotCovariance spot = v.get();
			final boolean deleted = deleteSpot( spot );
			String str;
			if ( deleted )
			{
				str = "removed spot ";
			}
			else
			{
				str = "problem removing spot ";
			}
			if ( getTrackSchemeVertex( spot ).getLabel() == null || getTrackSchemeVertex( spot ).getLabel() == "" )
			{
				str += "ID=" + spot.getInternalPoolIndex();
			}
			else
			{
				str += getTrackSchemeVertex( spot ).getLabel();
			}
			str += " from t=" + spot.getTimepoint();
			fireModelEditEvent();
			viewer.showMessage( str );
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
	
	/*
	 * OVERLAY FOR CURRENT EDIT.
	 */

	@Override
	public final void drawOverlays( final Graphics g )
	{
		overlay.drawOverlays( g );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	private class GhostOverlay implements OverlayRenderer
	{

		private SpotCovariance ghostSpot;

		private final AffineTransform3D transform = new AffineTransform3D();

		private final double[] lPos1 = new double[ 3 ];

		private final double[] gPos1 = new double[ 3 ];

		private final double[] lPos2 = new double[ 3 ];

		private final double[] gPos2 = new double[ 3 ];

		private final double[][] S = new double[ 3 ][ 3 ];

		private final double[][] T = new double[ 3 ][ 3 ];

		private final double[][] TS = new double[ 3 ][ 3 ];

		private final double nSigmas = SpotCovariance.nSigmas;

		private final BasicStroke stroke;

		private final Color color = Color.WHITE;

		private boolean paintGhostLink = false;

		private GhostOverlay()
		{
			final float dash[] = { 4.0f };
			stroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f );
		}

		/**
		 * Sets the spot to paint as a "ghost" by this overlay. If
		 * <code>null</code>, will not paint anything.
		 * 
		 * @param spot
		 *            the spot to paint as a "ghost".
		 */
		private void paint( final SpotCovariance spot )
		{
			this.ghostSpot = spot;
		}

		/**
		 * Sets whether the ghost link should be painted. Write the link target
		 * position (in global coordinates) in the returned double array
		 * instance.
		 * 
		 * @param doPaint
		 *            if <code>true</code>, the ghost link will be painted.
		 * @return the double array instance into which the link target
		 *         coordinates should be written.
		 */
		private double[] setPaintGhostLink( final boolean doPaint )
		{
			this.paintGhostLink = doPaint;
			return lPos2;
		}

		private void clear()
		{
			this.paintGhostLink = false;
			this.ghostSpot = null;
		}

		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( null == ghostSpot && !paintGhostLink )
				return;

			final Graphics2D graphics = ( Graphics2D ) g;
			graphics.setColor( color );
			graphics.setStroke( stroke );
			final AffineTransform torig = graphics.getTransform();

			viewer.getState().getViewerTransform( transform );

			if ( null != ghostSpot )
			{
				ghostSpot.localize( lPos1 );
				transform.apply( lPos1, gPos1 );

				ghostSpot.getCovariance( S );
				for ( int r = 0; r < 3; ++r )
					for ( int c = 0; c < 3; ++c )
						T[ r ][ c ] = transform.get( r, c );

				LinAlgHelpers.mult( T, S, TS );
				LinAlgHelpers.multABT( TS, T, S );
				// will not return orthogonal V.
				S[ 0 ][ 1 ] = S[ 1 ][ 0 ];
				S[ 0 ][ 2 ] = S[ 2 ][ 0 ];
				S[ 1 ][ 2 ] = S[ 2 ][ 1 ];

				final double[][] S2 = new double[ 2 ][ 2 ];
				for ( int r = 0; r < 2; ++r )
					for ( int c = 0; c < 2; ++c )
						S2[ r ][ c ] = S[ r ][ c ];
				final EigenvalueDecomposition eig2 = new Matrix( S2 ).eig();
				final double[] eigVals2 = eig2.getRealEigenvalues();
				final double w = nSigmas * Math.sqrt( eigVals2[ 0 ] );
				final double h = nSigmas * Math.sqrt( eigVals2[ 1 ] );
				final Matrix V2 = eig2.getV();
				final double c = V2.getArray()[ 0 ][ 0 ];
				final double s = V2.getArray()[ 1 ][ 0 ];
				final double theta = Math.atan2( s, c );
				graphics.translate( gPos1[ 0 ], gPos1[ 1 ] );
				graphics.rotate( theta );
				graphics.draw( new Ellipse2D.Double( -w, -h, 2 * w, 2 * h ) );
				graphics.setTransform( torig );
			}
			
			if ( paintGhostLink )
			{
				final int x1 = ( int ) gPos1[ 0 ];
				final int y1 = ( int ) gPos1[ 1 ];
				
				transform.apply( lPos2, gPos2 );
				final int x2 = ( int ) gPos2[ 0 ];
				final int y2 = ( int ) gPos2[ 1 ];
				
				graphics.drawLine( x1, y1, x2, y2 );
			}

		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}


}
