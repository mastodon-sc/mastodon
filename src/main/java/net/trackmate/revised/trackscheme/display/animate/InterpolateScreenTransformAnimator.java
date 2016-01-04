package net.trackmate.revised.trackscheme.display.animate;

import net.trackmate.revised.trackscheme.ScreenTransform;

public class InterpolateScreenTransformAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private final ScreenTransform t0;

	private final ScreenTransform t1;

	private final ScreenTransform transform = new ScreenTransform();

	/**
	 * Animate {@link ScreenTransform} by interpolating between {@code t0} and
	 * {@code t1}.
	 *
	 * @param t0
	 *            transform at begin of animation.
	 * @param t1
	 *            transform at end of animation.
	 * @param duration
	 *            duration of animation.
	 */
	public InterpolateScreenTransformAnimator( final ScreenTransform t0, final ScreenTransform t1, final long duration )
	{
		super( duration );
		this.t0 = t0.copy();
		this.t1 = t1.copy();
	}

	@Override
	protected ScreenTransform get( final double t )
	{
		if ( t <= 0 )
			return t0;
		else if ( t > 1 )
			return t1;
		else
		{
			transform.interpolate( t0, t1, t );
			return transform;
		}
	}
}
