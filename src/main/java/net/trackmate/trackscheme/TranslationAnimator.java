package net.trackmate.trackscheme;

import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

/**
 * An animator that just executes a constant speed translation of the current
 * viewpoint to a target location, keeping all other view parameters constant.
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class TranslationAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private final ScreenTransform transformStart;

	private final double deltaX;

	private final double deltaY;

	private final double targetX;

	private final double targetY;

	public TranslationAnimator( final ScreenTransform transformStart, final double targetX, final double targetY, final long duration )
	{
		super( duration );
		this.transformStart = transformStart;
		this.targetX = targetX;
		this.targetY = targetY;
		this.deltaX = transformStart.maxX - transformStart.minX;
		this.deltaY = transformStart.maxY - transformStart.minY;
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

		transform.minX = transform.minX + t * ( targetX - deltaX / 2 - transform.minX );
		transform.maxX = transform.maxX + t * ( targetX + deltaX / 2 - transform.maxX );
		transform.minY = transform.minY + t * ( targetY - deltaY / 2 - transform.minY );
		transform.maxY = transform.maxY + t * ( targetY + deltaY / 2 - transform.maxY );

		return transform;
	}
}
