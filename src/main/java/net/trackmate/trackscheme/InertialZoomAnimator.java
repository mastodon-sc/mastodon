package net.trackmate.trackscheme;

import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

public class InertialZoomAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private static final double dScale = .01;

	private final ScreenTransform transformStart;

	private final boolean zoomOut;

	private final boolean zoomX;

	private final boolean zoomY;

	private final int eX;

	private final int eY;

	private final int zoomSteps;

	public InertialZoomAnimator( final ScreenTransform transformStart, final int zoomSteps, final boolean zoomOut, final boolean zoomX, final boolean zoomY, final int eX, final int eY, final long duration )
	{
		super( duration );
		this.transformStart = transformStart;
		this.zoomSteps = zoomSteps;
		this.zoomOut = zoomOut;
		this.zoomX = zoomX;
		this.zoomY = zoomY;
		this.eX = eX;
		this.eY = eY;
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

		final double tau = 1 / 3;
		final double zoom = zoomOut ? 1d / ( 1d + zoomSteps * dScale * Math.exp( -t / tau ) ) : ( 1d + dScale * zoomSteps * Math.exp( -t / tau ) );
		if ( zoomX && !zoomY )
		{
			transform.scaleX( zoom, eX, eY );
		}
		else if ( !zoomX && zoomY )
		{
			transform.scaleY( zoom, eX, eY );
		}
		else if ( zoomX && zoomY )
		{
			transform.scale( zoom, eX, eY );
		}
		return transform;
	}

}
