package org.mastodon.views.bdv.overlay;

/**
 * A specialization of {@link RenderSettings} for objects that are rendered in a
 * 3D scene. Mainly adds parameters to control visibility with respect to the
 * camera position.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <S>
 *            the concrete type of the render settings class.
 */
public abstract class RenderSettings3D< S extends RenderSettings3D< S > > extends RenderSettings< S >
{

	public static final double DEFAULT_ELLIPSOID_FADE_DEPTH = 0.2;

	public static final boolean DEFAULT_IS_FOCUS_LIMIT_RELATIVE = true;

	public static final double DEFAULT_POINT_FADE_DEPTH = 0.;

	public static final double DEFAULT_LIMIT_FOCUS_RANGE = 100.;

	private boolean isFocusLimitViewRelative = DEFAULT_IS_FOCUS_LIMIT_RELATIVE;

	/**
	 * TODO: rename. This is generic to all ellipsoids, not just ellipsoids.
	 */
	private double ellipsoidFadeDepth = DEFAULT_ELLIPSOID_FADE_DEPTH;

	private double focusLimit = DEFAULT_LIMIT_FOCUS_RANGE;

	@Override
	protected synchronized void set( final S settings )
	{
		super.set( settings );
		final RenderSettings3D< S > other = settings;
		this.isFocusLimitViewRelative = other.isFocusLimitViewRelative;
		this.ellipsoidFadeDepth = other.ellipsoidFadeDepth;
		this.focusLimit = other.focusLimit;
	}

	/**
	 * The ratio of {@link #focusLimit} at which points start to fade. Points
	 * are drawn increasingly translucent the closer they are to
	 * {@link #focusLimit}. Up to ratio {@link #pointFadeDepth} they are fully
	 * opaque, then their alpha value goes to 0 linearly.
	 */
	private double pointFadeDepth = DEFAULT_POINT_FADE_DEPTH;

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

}
