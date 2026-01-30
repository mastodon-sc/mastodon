package org.mastodon.views.bdv.overlay.shapes.ellipsoid.style;

import java.util.ArrayList;
import java.util.Collection;

import org.mastodon.views.bdv.overlay.RenderSettings;

/**
 * Render settings for 3D ellipsoid shapes, that are painted as an intersection
 * with or a projection on a 2D slice.
 */
public class EllipsoidRenderSettings extends RenderSettings< EllipsoidRenderSettings >
{

	public static final boolean DEFAULT_DRAW_ELLIPSE = true;

	public static final boolean DEFAULT_DRAW_SLICE_INTERSECTION = true;

	public static final boolean DEFAULT_DRAW_SLICE_PROJECTION = !DEFAULT_DRAW_SLICE_INTERSECTION;

	public static final boolean DEFAULT_DRAW_POINTS =
			!DEFAULT_DRAW_ELLIPSE || ( DEFAULT_DRAW_ELLIPSE && DEFAULT_DRAW_SLICE_INTERSECTION );

	public static final boolean DEFAULT_DRAW_POINTS_FOR_ELLIPSE = false;

	public static final double DEFAULT_ELLIPSOID_FADE_DEPTH = 0.2;

	public static final boolean DEFAULT_IS_FOCUS_LIMIT_RELATIVE = true;

	public static final double DEFAULT_POINT_FADE_DEPTH = 0.;

	public static final double DEFAULT_LIMIT_FOCUS_RANGE = 100.;

	private boolean drawEllipsoidSliceProjection = DEFAULT_DRAW_SLICE_PROJECTION;

	private boolean drawEllipsoidSliceIntersection = DEFAULT_DRAW_SLICE_INTERSECTION;

	/** Whether to draw ellipsoid centers. */
	private boolean drawPoints = DEFAULT_DRAW_POINTS;

	private boolean drawPointsForEllipses = DEFAULT_DRAW_POINTS_FOR_ELLIPSE;

	private boolean isFocusLimitViewRelative = DEFAULT_IS_FOCUS_LIMIT_RELATIVE;

	private double ellipsoidFadeDepth = DEFAULT_ELLIPSOID_FADE_DEPTH;

	private double focusLimit = DEFAULT_LIMIT_FOCUS_RANGE;

	/**
	 * The ratio of {@link #focusLimit} at which points start to fade. Points
	 * are drawn increasingly translucent the closer they are to
	 * {@link #focusLimit}. Up to ratio {@link #pointFadeDepth} they are fully
	 * opaque, then their alpha value goes to 0 linearly.
	 */
	private double pointFadeDepth = DEFAULT_POINT_FADE_DEPTH;

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
		this.isFocusLimitViewRelative = other.isFocusLimitViewRelative;
		this.ellipsoidFadeDepth = other.ellipsoidFadeDepth;
		this.focusLimit = other.focusLimit;
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

	/**
	 * Get the maximum distance from the view plane up to which to spots are
	 * drawn.
	 * <p>
	 * Depending on {@link #getFocusLimitViewRelative()}, the distance is either
	 * in the current view coordinate system or in the global coordinate system.
	 * If {@code getFocusLimitViewRelative() == true} then the distance is in
	 * current view coordinates. For example, a value of 100 means that spots
	 * will be visible up to 100 pixel widths from the view plane. Thus, the
	 * effective focus range depends on the current zoom level. If
	 * {@code getFocusLimitViewRelative() == false} then the distance is in
	 * global coordinates. A value of 100 means that spots will be visible up to
	 * 100 units (of the global coordinate system) from the view plane.
	 * <p>
	 * Ellipsoids are drawn increasingly translucent the closer they are to the
	 * {@code focusLimit}. See {@link #getEllipsoidFadeDepth()}.
	 *
	 * @return the maximum distance from the view plane up to which to spots are
	 *         drawn.
	 */
	public double getFocusLimit()
	{
		return focusLimit;
	}

	/**
	 * Set the maximum distance from the view plane up to which to spots are
	 * drawn. See {@link #getFocusLimit()}.
	 *
	 * @param focusLimit
	 *            the maximum distance from the view plane up to which to spots
	 *            are drawn.
	 */
	public synchronized void setFocusLimit( final double focusLimit )
	{
		if ( this.focusLimit != focusLimit )
		{
			this.focusLimit = focusLimit;
			notifyListeners();
		}
	}

	/**
	 * Set whether the {@link #getFocusLimit()} is relative to the the current
	 * view coordinate system.
	 * <p>
	 * If {@code true} then the distance is in current view coordinates. For
	 * example, a value of 100 means that spots will be visible up to 100 pixel
	 * widths from the view plane. Thus, the effective focus range depends on
	 * the current zoom level. If {@code false} then the distance is in global
	 * coordinates. A value of 100 means that spots will be visible up to 100
	 * units (of the global coordinate system) from the view plane.
	 *
	 * @return {@code true} iff the {@link #getFocusLimit()} is relative to the
	 *         the current view coordinate system.
	 */
	public boolean getFocusLimitViewRelative()
	{
		return isFocusLimitViewRelative;
	}

	/**
	 * Set whether the {@link #getFocusLimit()} is relative to the the current
	 * view coordinate system. See {@link #getFocusLimitViewRelative()}.
	 *
	 * @param isFocusLimitViewRelative
	 *            whether the {@link #getFocusLimit()} is relative to the the
	 *            current view coordinate system.
	 */
	public synchronized void setFocusLimitViewRelative( final boolean isFocusLimitViewRelative )
	{
		if ( this.isFocusLimitViewRelative != isFocusLimitViewRelative )
		{
			this.isFocusLimitViewRelative = isFocusLimitViewRelative;
			notifyListeners();
		}
	}

	/**
	 * Get the ratio of {@link #getFocusLimit()} at which ellipsoids start to
	 * fade. Ellipsoids are drawn increasingly translucent the closer they are
	 * to {@link #getFocusLimit()}. Up to ratio {@link #getEllipsoidFadeDepth()}
	 * they are fully opaque, then their alpha value goes to 0 linearly.
	 *
	 * @return the ratio of {@link #getFocusLimit()} at which ellipsoids start
	 *         to fade.
	 */
	public double getEllipsoidFadeDepth()
	{
		return ellipsoidFadeDepth;
	}

	/**
	 * Set the ratio of {@link #getFocusLimit()} at which ellipsoids start to
	 * fade. See {@link #getEllipsoidFadeDepth()}.
	 *
	 * @param ellipsoidFadeDepth
	 *            the ratio of {@link #getFocusLimit()} at which ellipsoids
	 *            start to fade.
	 */
	public synchronized void setEllipsoidFadeDepth( final double ellipsoidFadeDepth )
	{
		if ( this.ellipsoidFadeDepth != ellipsoidFadeDepth )
		{
			this.ellipsoidFadeDepth = ellipsoidFadeDepth;
			notifyListeners();
		}
	}

	/**
	 * The ratio of {@link #getFocusLimit()} at which points start to fade.
	 * Points are drawn increasingly translucent the closer they are to
	 * {@link #getFocusLimit()}. Up to ratio {@link #getPointFadeDepth} they are
	 * fully opaque, then their alpha value goes to 0 linearly.
	 *
	 * @return the ratio of {@link #getFocusLimit()} at which points start to
	 *         fade.
	 */
	public double getPointFadeDepth()
	{
		return pointFadeDepth;
	}

	/**
	 * Set the ratio of {@link #getFocusLimit()} at which points start to fade.
	 * See {@link #getPointFadeDepth()}.
	 *
	 * @param pointFadeDepth
	 *            the ratio of {@link #getFocusLimit()} at which points start to
	 *            fade.
	 */
	public synchronized void setPointFadeDepth( final double pointFadeDepth )
	{
		if ( this.pointFadeDepth != pointFadeDepth )
		{
			this.pointFadeDepth = pointFadeDepth;
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
