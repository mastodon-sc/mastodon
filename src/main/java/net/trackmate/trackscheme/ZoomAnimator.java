package net.trackmate.trackscheme;

import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

public class ZoomAnimator extends AbstractTransformAnimator< ScreenTransform >
{

	private final ScreenTransform transformStart;

	private final double targetMinX;

	private final double targetMaxX;

	private final double targetMinY;

	private final double targetMaxY;

	public ZoomAnimator( final ScreenTransform transformStart, final double minX, final double minY, final double maxX, final double maxY, final long duration )
	{
		super( duration );
		this.transformStart = transformStart;
		this.targetMinX = minX;
		this.targetMaxX = maxX;
		this.targetMinY = minY;
		this.targetMaxY = maxY;
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

		transform.minX = transform.minX + t * ( targetMinX - transform.minX );
		transform.maxX = transform.maxX + t * ( targetMaxX - transform.maxX );
		transform.minY = transform.minY + t * ( targetMinY - transform.minY );
		transform.maxY = transform.maxY + t * ( targetMaxY - transform.maxY );

		return transform;
	}

}
