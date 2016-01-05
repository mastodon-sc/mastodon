package net.trackmate.revised.trackscheme.display.animate;

import net.trackmate.revised.trackscheme.ScreenTransform;

public class InertialScreenTransformAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private final ScreenTransform t0;

	private final ScreenTransform t1;

	private final ScreenTransform transform = new ScreenTransform();

	private static final double s = 3.;

	private final double tau;

	/**
	 * Extrapolate {@link ScreenTransform} that changed from {@code t0} to
	 * {@code t1} in time interval {@code dt}, beyond {@code t1}.
	 *
	 * @param t0
	 *            transform at begin of interval.
	 * @param t1
	 *            transform at end of interval.
	 * @param dt
	 *            duration of interval.
	 * @param duration
	 *            duration of inertial animation (same units as {@code dt}).
	 */
	public InertialScreenTransformAnimator( final ScreenTransform t0, final ScreenTransform t1, final long dt, final long duration )
	{
		super( duration );
		this.t0 = t0.copy();
		this.t1 = t1.copy();
		this.tau = duration / ( s * dt );
	}

	@Override
	protected ScreenTransform get( double t )
	{
		if ( t <= 0 )
			return t1;

		if ( t > 1 )
			t = 1;

		final double inc = tau * ( 1 - Math.exp( -t * s ) );
		transform.interpolate( t0, t1, 1 + inc );
		return transform;
	}
}
