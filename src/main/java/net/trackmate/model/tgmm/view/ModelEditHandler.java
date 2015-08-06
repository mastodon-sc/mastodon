package net.trackmate.model.tgmm.view;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertexWrapper;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.model.Link;
import net.trackmate.model.tgmm.SpotCovariance;
import net.trackmate.model.tgmm.TgmmModel;
import net.trackmate.trackscheme.SelectionHandler;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeVertex;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.ViewerState;

public class ModelEditHandler implements MouseListener
{
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

	public ModelEditHandler( final TgmmModel model, final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > wrapper, final ViewerPanel viewer, final ShowTrackScheme trackscheme )
	{
		this.model = model;
		this.wrapper = wrapper;
		this.viewer = viewer;
		this.trackscheme = trackscheme;
		this.selectionHandler = trackscheme.getSelectionHandler();
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		new Thread( "Select spot thread." )
		{
			@Override
			public void run()
			{
				final ViewerState state = viewer.getState();
				state.getViewerTransform( t );
				from.setPosition( e.getX(), 0 );
				from.setPosition( e.getY(), 1 );
				from.setPosition( 0., 2 );
				t.applyInverse( to, from );

				final int timepoint = state.getCurrentTimepoint();
				final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
						wrapper.getSpatialSearch( timepoint );
				search.search( to );

				final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
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
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	/*
	 * PRIVATE METHODS.
	 */

	private void repaint()
	{
		trackscheme.repaint();
		viewer.repaint();
	}
}
