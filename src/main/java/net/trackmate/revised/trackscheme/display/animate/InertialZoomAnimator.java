package net.trackmate.revised.trackscheme.display.animate;

import net.trackmate.revised.trackscheme.ScreenTransform;

public class InertialZoomAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private static final double dScale = 0.005;

	private final ScreenTransform transformStart;

	private final boolean zoomOut;

	private final boolean zoomX;

	private final boolean zoomY;

	private final int eX;

	private final int eY;

	private final int zoomSteps;

	private final ScreenTransform transform;

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
		this.transform = new ScreenTransform();
		transform.set( transformStart );
	}

	@Override
	protected ScreenTransform get( double t )
	{
		if ( t <= 0 ) { return transformStart; }
		if ( t > 1 )
		{
			t = 1;
		}

		final double tau = 1 / 3d;
		final double zoom = zoomOut ? 1d / ( 1d + zoomSteps * dScale * Math.exp( -t / tau ) ) : ( 1d + dScale * zoomSteps * Math.exp( -t / tau ) );
		if ( zoomX && !zoomY )
		{
			transform.zoomX( zoom, eX );
		}
		else if ( !zoomX && zoomY )
		{
			transform.zoomY( zoom, eY );
		}
		else if ( zoomX && zoomY )
		{
			transform.zoom( zoom, eX, eY );
		}
		return transform;
	}

}
