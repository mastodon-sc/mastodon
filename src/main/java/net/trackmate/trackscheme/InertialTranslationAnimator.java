package net.trackmate.trackscheme;

import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

public class InertialTranslationAnimator extends AbstractTransformAnimator< ScreenTransform >
{

	private final double tau;

	private final ScreenTransform transformStart;

	private final double vy0;

	private final double vx0;

	public InertialTranslationAnimator( final ScreenTransform transformStart, final double vx0, final double vy0, final long duration )
	{
		super( duration );
		this.transformStart = transformStart;
		this.vy0 = vy0;
		this.vx0 = vx0;
		this.tau = 0.33d; // in unit of duration
	}

	@Override
	protected ScreenTransform get( double t )
	{
		if ( t <= 0 ) { return transformStart; }
		if ( t > 1 )
		{
			t = 1;
		}

		final ScreenTransform transform = new ScreenTransform();
		transform.set( transformStart );

		final double dx = vx0 * Math.exp( -t / tau ) * t;
		final double dy = vy0 * Math.exp( -t / tau ) * t;
		
		transform.minX = transform.minX + dx;
		transform.maxX = transform.maxX + dx;

		transform.minY = transform.minY + dy;
		transform.maxY = transform.maxY + dy;

		return transform;
	}

}
