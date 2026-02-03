package org.mastodon.views.bdv.overlay.shapes.ellipsoid.style;

import java.util.ArrayList;
import java.util.Collection;

import org.mastodon.views.bdv.overlay.RenderSettings3D;

/**
 * Render settings for 3D ellipsoid shapes, that are painted as an intersection
 * with or a projection on a 2D slice.
 */
public class EllipsoidRenderSettings extends RenderSettings3D< EllipsoidRenderSettings >
{

	public static final boolean DEFAULT_DRAW_ELLIPSE = true;

	public static final boolean DEFAULT_DRAW_SLICE_INTERSECTION = true;

	public static final boolean DEFAULT_DRAW_SLICE_PROJECTION = !DEFAULT_DRAW_SLICE_INTERSECTION;

	public static final boolean DEFAULT_DRAW_POINTS =
			!DEFAULT_DRAW_ELLIPSE || ( DEFAULT_DRAW_ELLIPSE && DEFAULT_DRAW_SLICE_INTERSECTION );

	public static final boolean DEFAULT_DRAW_POINTS_FOR_ELLIPSE = false;

	private boolean drawEllipsoidSliceProjection = DEFAULT_DRAW_SLICE_PROJECTION;

	private boolean drawEllipsoidSliceIntersection = DEFAULT_DRAW_SLICE_INTERSECTION;

	/** Whether to draw ellipsoid centers. */
	private boolean drawPoints = DEFAULT_DRAW_POINTS;

	private boolean drawPointsForEllipses = DEFAULT_DRAW_POINTS_FOR_ELLIPSE;

	private EllipsoidRenderSettings()
	{
		super();
	}

	@Override
	public synchronized void set( final EllipsoidRenderSettings other )
	{
		super.set( other );
		this.drawEllipsoidSliceProjection = other.drawEllipsoidSliceProjection;
		this.drawEllipsoidSliceIntersection = other.drawEllipsoidSliceIntersection;
		this.drawPointsForEllipses = other.drawPointsForEllipses;
		notifyListeners();
	}


	/**
	 * Get whether the projections of spot ellipsoids onto the view plane are
	 * drawn.
	 *
	 * @return {@code true} iff projections of spot ellipsoids onto the view
	 *         plane are drawn.
	 */
	public boolean getDrawEllipsoidSliceProjection()
	{
		return drawEllipsoidSliceProjection;
	}

	/**
	 * Set whether to draw the projections of spot ellipsoids onto the view
	 * plane.
	 *
	 * @param drawEllipsoidSliceProjection
	 *            whether to draw projections of spot ellipsoids onto the view
	 *            plane.
	 */
	public synchronized void setDrawEllipsoidSliceProjection( final boolean drawEllipsoidSliceProjection )
	{
		if ( this.drawEllipsoidSliceProjection != drawEllipsoidSliceProjection )
		{
			this.drawEllipsoidSliceProjection = drawEllipsoidSliceProjection;
			notifyListeners();
		}
	}

	/**
	 * Get whether the intersections of spot ellipsoids with the view plane are
	 * drawn.
	 *
	 * @return {@code true} iff intersections of spot ellipsoids with the view
	 *         plane are drawn.
	 */
	public boolean getDrawEllipsoidSliceIntersection()
	{
		return drawEllipsoidSliceIntersection;
	}

	/**
	 * Set whether to draw the intersections of spot ellipsoids with the view
	 * plane.
	 *
	 * @param drawEllipsoidSliceIntersection
	 *            whether to draw intersections of spot ellipsoids with the view
	 *            plane.
	 */
	public synchronized void setDrawEllipsoidSliceIntersection( final boolean drawEllipsoidSliceIntersection )
	{
		if ( this.drawEllipsoidSliceIntersection != drawEllipsoidSliceIntersection )
		{
			this.drawEllipsoidSliceIntersection = drawEllipsoidSliceIntersection;
			notifyListeners();
		}
	}

	/**
	 * Get whether spot centers are also drawn for those points that are visible
	 * as ellipses. See {@link #getDrawSpotCenters()}.
	 *
	 * @return whether spot centers are also drawn for those points that are
	 *         visible as ellipses.
	 */
	public boolean getDrawSpotCentersForEllipses()
	{
		return drawPointsForEllipses;
	}

	/**
	 * Set whether spot centers are also drawn for those points that are visible
	 * as ellipses.
	 *
	 * @param drawPointsForEllipses
	 *            whether spot centers are also drawn for those points that are
	 *            visible as ellipses.
	 */
	public synchronized void setDrawSpotCentersForEllipses( final boolean drawPointsForEllipses )
	{
		if ( this.drawPointsForEllipses != drawPointsForEllipses )
		{
			this.drawPointsForEllipses = drawPointsForEllipses;
			notifyListeners();
		}
	}

	/**
	 * Get whether spot centers are drawn.
	 * <p>
	 * Note that spot centers are usually only drawn, if no ellipse for the spot
	 * was drawn (unless {@link #getDrawSpotCentersForEllipses()}
	 * {@code == true}).
	 *
	 * @return whether spot centers are drawn.
	 */
	public boolean getDrawSpotCenters()
	{
		return drawPoints;
	}

	/**
	 * Set whether spot centers are drawn.
	 * <p>
	 * Note that spot centers are usually only drawn, if no ellipse for the spot
	 * was drawn (unless {@link #getDrawSpotCentersForEllipses()}
	 * {@code == true}).
	 *
	 * @param drawPoints
	 *            whether spot centers are drawn.
	 */
	public synchronized void setDrawSpotCenters( final boolean drawPoints )
	{
		if ( this.drawPoints != drawPoints )
		{
			this.drawPoints = drawPoints;
			notifyListeners();
		}
	}

	// Default styles

	private static final EllipsoidRenderSettings defaultStyle;
	static
	{
		defaultStyle = new EllipsoidRenderSettings();
		defaultStyle.setName( "Default" );
	}

	private static final EllipsoidRenderSettings pointCloudStyle;
	static
	{
		pointCloudStyle = defaultStyle.copy( "Point cloud" );
		pointCloudStyle.setDrawLinks( false );
		pointCloudStyle.setDrawEllipsoidSliceIntersection( false );
		pointCloudStyle.setDrawEllipsoidSliceProjection( false );
		pointCloudStyle.setFocusLimitViewRelative( false );
	}

	private static final EllipsoidRenderSettings noOverlayStyle;
	static
	{
		noOverlayStyle = defaultStyle.copy( "No overlay" );
		noOverlayStyle.setDrawLinks( false );
		noOverlayStyle.setDrawSpots( false );
	}

	public static final Collection< EllipsoidRenderSettings > defaults;
	static
	{
		defaults = new ArrayList<>( 3 );
		defaults.add( defaultStyle );
		defaults.add( pointCloudStyle );
		defaults.add( noOverlayStyle );
	}

	public static EllipsoidRenderSettings defaultStyle()
	{
		return defaultStyle.copy();
	}

	@Override
	protected EllipsoidRenderSettings createInstance()
	{
		return new EllipsoidRenderSettings();
	}
}
