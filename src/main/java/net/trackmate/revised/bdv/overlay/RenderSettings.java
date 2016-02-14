package net.trackmate.revised.bdv.overlay;

public class RenderSettings
{
	/*
	 * PUBLIC DISPLAY CONFIG DEFAULTS.
	 */

	public static final int DEFAULT_LIMIT_TIME_RANGE = 20;
	public static final double DEFAULT_LIMIT_FOCUS_RANGE = 100.;
	public static final boolean DEFAULT_USE_ANTI_ALIASING = true;
	public static final boolean DEFAULT_USE_GRADIENT = false;
	public static final boolean DEFAULT_DRAW_SPOTS = true;
	public static final boolean DEFAULT_DRAW_LINKS = true;
	public static final boolean DEFAULT_DRAW_ELLIPSE = true;
	public static final boolean DEFAULT_DRAW_SLICE_INTERSECTION = true;
	public static final boolean DEFAULT_DRAW_SLICE_PROJECTION = !DEFAULT_DRAW_SLICE_INTERSECTION;
	public static final boolean DEFAULT_DRAW_POINTS = !DEFAULT_DRAW_ELLIPSE || (DEFAULT_DRAW_ELLIPSE && DEFAULT_DRAW_SLICE_INTERSECTION);
	public static final boolean DEFAULT_DRAW_POINTS_FOR_ELLIPSE = false;
	public static final boolean DEFAULT_IS_FOCUS_LIMIT_RELATIVE = true;
	public static final double DEFAULT_ELLIPSOID_FADE_DEPTH = 0.2;
	public static final double DEFAULT_POINT_FADE_DEPTH = 0.2;

	public RenderSettings()
	{
		useAntialiasing = DEFAULT_USE_ANTI_ALIASING;
		useGradient = DEFAULT_USE_GRADIENT;
		timeLimit = DEFAULT_LIMIT_TIME_RANGE;
		drawLinks = DEFAULT_DRAW_LINKS;
		drawSpots = DEFAULT_DRAW_SPOTS;
		drawEllipsoidSliceProjection = DEFAULT_DRAW_SLICE_PROJECTION;
		drawEllipsoidSliceIntersection = DEFAULT_DRAW_SLICE_INTERSECTION;
		drawPoints = DEFAULT_DRAW_POINTS;
		drawPointsForEllipses = DEFAULT_DRAW_POINTS_FOR_ELLIPSE;
		focusLimit = DEFAULT_LIMIT_FOCUS_RANGE;
		isFocusLimitViewRelative = DEFAULT_IS_FOCUS_LIMIT_RELATIVE;
		ellipsoidFadeDepth = DEFAULT_ELLIPSOID_FADE_DEPTH;
		pointFadeDepth = DEFAULT_POINT_FADE_DEPTH;
	}

	public void set( final RenderSettings settings )
	{
		useAntialiasing = settings.useAntialiasing;
		useGradient = settings.useGradient;
		timeLimit = settings.timeLimit;
		drawLinks = settings.drawLinks;
		drawSpots = settings.drawSpots;
		drawEllipsoidSliceProjection = settings.drawEllipsoidSliceProjection;
		drawEllipsoidSliceIntersection = settings.drawEllipsoidSliceIntersection;
		drawPoints = settings.drawPoints;
		drawPointsForEllipses = settings.drawPointsForEllipses;
		focusLimit = settings.focusLimit;
		isFocusLimitViewRelative = settings.isFocusLimitViewRelative;
		ellipsoidFadeDepth = settings.ellipsoidFadeDepth;
		pointFadeDepth = settings.pointFadeDepth;
	}

	/*
	 * DISPLAY SETTINGS FIELDS.
	 */

	/**
	 * Whether to use antialiasing (for drawing everything).
	 */
	private boolean useAntialiasing;

	/**
	 * If {@code true}, draw links using a gradient from source color to target
	 * color. If {@code false}, draw links using the target color.
	 */
	private boolean useGradient;

	/**
	 * Maximum number of timepoints into the past for which outgoing edges
	 * should be drawn.
	 */
	private int timeLimit;

	/**
	 * Whether to draw links (at all).
	 * For specific settings, see TODO
	 */
	private boolean drawLinks;

	/**
	 * Whether to draw spots (at all).
	 * For specific settings, see TODO
	 */
	private boolean drawSpots;

	/**
	 * Whether to draw the projections of spot ellipsoids onto the view plane.
	 */
	private boolean drawEllipsoidSliceProjection;

	/**
	 * Whether to draw the intersections of spot ellipsoids with the view plane.
	 */
	private boolean drawEllipsoidSliceIntersection;

	/**
	 * Whether to draw spot centers.
	 */
	private boolean drawPoints;

	/**
	 * Whether to draw spot centers also for those points that are visible as ellipses.
	 */
	private boolean drawPointsForEllipses;

	/**
	 * Maximum distance from view plane up to which to draw spots.
	 *
	 * <p>
	 * Depending on {@link #isFocusLimitViewRelative}, the distance is
	 * either in the current view coordinate system or in the global coordinate
	 * system. If {@code isFocusLimitViewRelative() == true} then the
	 * distance is in current view coordinates. For example, a value of 100
	 * means that spots will be visible up to 100 pixel widths from the view
	 * plane. Thus, the effective focus range depends on the current zoom level.
	 * If {@code isFocusLimitViewRelative() == false} then the distance
	 * is in global coordinates. A value of 100 means that spots will be visible
	 * up to 100 units (of the global coordinate system) from the view plane.
	 *
	 * <p>
	 * Ellipsoids are drawn increasingly translucent the closer they are
	 * to {@link #focusLimit}. See {@link #ellipsoidFadeDepth}.
	 */
	private double focusLimit;

	/**
	 * Whether the {@link #focusLimit} is relative to the the current
	 * view coordinate system.
	 *
	 * <p>
	 * If {@code true} then the distance is in current view coordinates. For
	 * example, a value of 100 means that spots will be visible up to 100 pixel
	 * widths from the view plane. Thus, the effective focus range depends on
	 * the current zoom level. If {@code false} then the distance is in global
	 * coordinates. A value of 100 means that spots will be visible up to 100
	 * units (of the global coordinate system) from the view plane.
	 */
	private boolean isFocusLimitViewRelative;

	/**
	 * The ratio of {@link #focusLimit} at which ellipsoids start to
	 * fade. Ellipsoids are drawn increasingly translucent the closer they are
	 * to {@link #focusLimit}. Up to ratio {@link #ellipsoidFadeDepth}
	 * they are fully opaque, then their alpha value goes to 0 linearly.
	 */
	private double ellipsoidFadeDepth;

	/**
	 * The ratio of {@link #focusLimit} at which points start to
	 * fade. Points are drawn increasingly translucent the closer they are
	 * to {@link #focusLimit}. Up to ratio {@link #pointFadeDepth}
	 * they are fully opaque, then their alpha value goes to 0 linearly.
	 */
	private double pointFadeDepth;

	/**
	 * Get the antialiasing setting.
	 *
	 * @return {@code true} if antialiasing is used.
	 */
	public boolean getUseAntialiasing()
	{
		return useAntialiasing;
	}

	/**
	 * Set whether to use antialiasing for drawing.
	 *
	 * @param useAntialiasing
	 */
	public void setUseAntialiasing( final boolean useAntialiasing )
	{
		this.useAntialiasing = useAntialiasing;
	}

	/**
	 * Whether a gradient is used for drawing links.
	 *
	 * @return {@code true} if links are drawn using a gradient from source
	 *         color to target color, or {@code false}, if links are drawn using
	 *         the target color.
	 */
	public boolean getUseGradient()
	{
		return useGradient;
	}

	/**
	 * Set whether to use a gradient for drawing links. If
	 * {@code useGradient=true}, draw links using a gradient from source color
	 * to target color. If {@code useGradient=false}, draw links using the
	 * target color.
	 *
	 * @param useGradient
	 *            whether to use a gradient for drawing links.
	 */
	public void setUseGradient( final boolean useGradient )
	{
		this.useGradient = useGradient;
	}

	/**
	 * Get the maximum number of timepoints into the past for which outgoing
	 * edges should be drawn.
	 *
	 * @return maximum number of timepoints into the past to draw links.
	 */
	public int getTimeLimit()
	{
		return timeLimit;
	}

	/**
	 * Set the maximum number of timepoints into the past for which outgoing
	 * edges should be drawn.
	 *
	 * @param timeLimit
	 *            maximum number of timepoints into the past to draw links.
	 */
	public void setTimeLimit( final int timeLimit )
	{
		this.timeLimit = timeLimit;
	}

	/**
	 * Get whether to draw links (at all). For specific settings, see
	 * {@link #getTimeLimit()}, {@link #getUseGradient()}.
	 *
	 * @return {@code true} if links are drawn.
	 */
	public boolean getDrawLinks()
	{
		return drawLinks;
	}

	/**
	 * Set whether to draw links (at all). For specific settings, see
	 * {@link #setTimeLimit(int)}, {@link #setUseGradient(boolean)}.
	 *
	 * @param drawLinks
	 *            whether to draw links.
	 */
	public void setDrawLinks( final boolean drawLinks )
	{
		this.drawLinks = drawLinks;
	}

	/**
	 * Get whether to draw spots (at all). For specific settings, see TODO
	 */
	public boolean getDrawSpots()
	{
		return drawSpots;
	}

	/**
	 * Set whether to draw spots (at all). For specific settings, see TODO
	 *
	 * @param drawSpots
	 *            whether to draw spots.
	 */
	public void setDrawSpots( final boolean drawSpots )
	{
		this.drawSpots = drawSpots;
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
	public void setDrawEllipsoidSliceProjection( final boolean drawEllipsoidSliceProjection )
	{
		this.drawEllipsoidSliceProjection = drawEllipsoidSliceProjection;
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
	public void setDrawEllipsoidSliceIntersection( final boolean drawEllipsoidSliceIntersection )
	{
		this.drawEllipsoidSliceIntersection = drawEllipsoidSliceIntersection;
	}

	/**
	 * Get whether spot centers are drawn.
	 * <p>
	 * Note that spot centers are usually only drawn, if no ellipse for the spot
	 * was drawn (unless {@link #getDrawPointsForEllipses()} {@code == true}).
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
	 * was drawn (unless {@link #getDrawPointsForEllipses()} {@code == true}).
	 *
	 * @param drawPoints
	 *            whether spot centers are drawn.
	 */
	public void setDrawSpotCenters( final boolean drawPoints )
	{
		this.drawPoints = drawPoints;
	}

	/**
	 * Get whether spot centers are also drawn for those points that are visible
	 * as ellipses. See {@link #getDrawSpotCenters()}.
	 *
	 * @return whether spot centers are also drawn for those points that are
	 *         visible as ellipses.
	 */
	public boolean getDrawPointsForEllipses()
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
	public void setDrawPointsForEllipses( final boolean drawPointsForEllipses )
	{
		this.drawPointsForEllipses = drawPointsForEllipses;
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
	public void setFocusLimit( final double focusLimit )
	{
		this.focusLimit = focusLimit;
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
	public void setFocusLimitViewRelative( final boolean isFocusLimitViewRelative )
	{
		this.isFocusLimitViewRelative = isFocusLimitViewRelative;
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
	public void setEllipsoidFadeDepth( final double ellipsoidFadeDepth )
	{
		this.ellipsoidFadeDepth = ellipsoidFadeDepth;
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
	public void setPointFadeDepth( final double pointFadeDepth )
	{
		this.pointFadeDepth = pointFadeDepth;
	}
}
