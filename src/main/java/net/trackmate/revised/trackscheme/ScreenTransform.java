package net.trackmate.revised.trackscheme;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * A transformation from trackscheme layout coordinates to screen coordinates.
 * <p>
 * It is defined by a bounding box in trackscheme layout coordinates, and the
 * size of the screen that this should be mapped to.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenTransform implements InvertibleRealTransform
{
	private double minX;

	private double maxX;

	private double minY;

	private double maxY;

	private int screenWidth;

	private int screenHeight;

	private double scaleX;

	private double scaleY;

	public ScreenTransform()
	{}

	public ScreenTransform( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		update();
	}

	public ScreenTransform( final ScreenTransform t )
	{
		this.minX = t.minX;
		this.maxX = t.maxX;
		this.minY = t.minY;
		this.maxY = t.maxY;
		this.screenWidth = t.screenWidth;
		this.screenHeight = t.screenHeight;
		this.scaleX = t.scaleX;
		this.scaleY = t.scaleY;
	}

	@Override
	public ScreenTransform copy()
	{
		return new ScreenTransform( this );
	}

	public void set( final ScreenTransform t )
	{
		this.minX = t.minX;
		this.maxX = t.maxX;
		this.minY = t.minY;
		this.maxY = t.maxY;
		this.screenWidth = t.screenWidth;
		this.screenHeight = t.screenHeight;
		this.scaleX = t.scaleX;
		this.scaleY = t.scaleY;
	}

	private void update()
	{
		scaleX = ( screenWidth - 1 ) / ( maxX - minX );
		scaleY = ( screenHeight - 1 ) / ( maxY - minY );
	}

	/**
	 * TODO
	 * @return
	 */
	public double getMinX()
	{
		return minX;
	}

	/**
	 * TODO
	 * @return
	 */
	public double getMaxX()
	{
		return maxX;
	}

	/**
	 * TODO
	 * @return
	 */
	public double getMinY()
	{
		return minY;
	}

	/**
	 * TODO
	 * @return
	 */
	public double getMaxY()
	{
		return maxY;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getScreenWidth()
	{
		return screenWidth;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getScreenHeight()
	{
		return screenHeight;
	}

	/**
	 * Compute the x scale factor. This is the factor by which layout <em>x</em>
	 * coordinates have to be multiplied to obtain screen <em>x</em>
	 * coordinates.
	 *
	 * @return the x scale factor.
	 */
	public double getScaleX()
	{
		return scaleX;
	}

	/**
	 * Compute the y scale factor. This is the factor by which layout <em>y</em>
	 * coordinates have to be multiplied to obtain screen <em>y</em>
	 * coordinates.
	 *
	 * @return the y scale factor.
	 */
	public double getScaleY()
	{
		return scaleY;
	}

// TODO remove?
//	void setScreenTranslated( final int dX, final int dY, final ScreenTransform source )
//	{
//		final double xInvScale = ( maxX - minX ) / ( screenWidth - 1 );
//		final double yInvScale = ( maxY - minY ) / ( screenHeight - 1 );
//		minX = source.minX + xInvScale * dX;
//		maxX = source.maxX + xInvScale * dX;
//		minY = source.minY + yInvScale * dY;
//		maxY = source.maxY + yInvScale * dY;
//	}
//
	public double screenToLayoutX( final double x )
	{
		return minX + x / scaleX;
	}

	public double screenToLayoutY( final double y )
	{
		return minY + y / scaleY;
	}

	public double layoutToScreenX( final double x )
	{
		return ( x - minX ) * scaleX;
	}

	public double layoutToScreenY( final double y )
	{
		return ( y - minY ) * scaleY;
	}

	/**
	 * Zoom by specified scale factor, keeping {@code screenCenterX, screenCenterY} fixed.
	 *
	 * @param scale
	 * @param screenCenterX
	 * @param screenCenterY
	 */
	public void zoom( final double scale, final double screenCenterX, final double screenCenterY )
	{
		zoomX( scale, screenCenterX );
		zoomY( scale, screenCenterY );
	}

	/**
	 * Zoom in X by specified scale factor, keeping {@code screenCenterX} fixed.
	 *
	 * @param scale
	 * @param screenCenterX
	 */
	public void zoomX( final double scale, final double screenCenterX )
	{
		final double lX = screenToLayoutX( screenCenterX );
		final double newSizeX = ( maxX - minX ) * scale;
		scaleX = ( screenWidth - 1 ) / newSizeX;
		minX = lX - screenCenterX / scaleX;
		maxX = minX + newSizeX;
	}

	/**
	 * Zoom in Y by specified scale factor, keeping {@code screenCenterY} fixed.
	 *
	 * @param scale
	 * @param screenCenterY
	 */
	public void zoomY( final double scale, final double screenCenterX )
	{
		final double lY = screenToLayoutY( screenCenterX );
		final double newSizeY = ( maxY - minY ) * scale;
		scaleY = ( screenHeight - 1 ) / newSizeY;
		minY = lY - screenCenterX / scaleY;
		maxY = minY + newSizeY;
	}

	@Override
	public String toString()
	{
		return "X: " + minX + " -> " + maxX + ", Y: " + minY + " -> " + maxY + ", width = " + screenWidth + ", height = " + screenHeight;
	}

	@Override
	public int numSourceDimensions()
	{
		return 2;
	}

	@Override
	public int numTargetDimensions()
	{
		return 2;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		target[ 0 ] = layoutToScreenX( source[ 0 ] );
		target[ 1 ] = layoutToScreenY( source[ 1 ] );
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		target[ 0 ] = ( float ) layoutToScreenX( source[ 0 ] );
		target[ 1 ] = ( float ) layoutToScreenY( source[ 1 ] );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		target.setPosition( layoutToScreenX( source.getDoublePosition( 0 ) ), 0 );
		target.setPosition( layoutToScreenY( source.getDoublePosition( 1 ) ), 1 );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		source[ 0 ] = screenToLayoutX( target[ 0 ] );
		source[ 1 ] = screenToLayoutY( target[ 1 ] );
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		source[ 0 ] = ( float ) screenToLayoutX( target[ 0 ] );
		source[ 1 ] = ( float ) screenToLayoutY( target[ 1 ] );
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		source.setPosition( screenToLayoutX( target.getDoublePosition( 0 ) ), 0 );
		source.setPosition( screenToLayoutY( target.getDoublePosition( 1 ) ), 1 );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new InvertibleRealTransform()
		{
			@Override
			public int numTargetDimensions()
			{
				return 2;
			}

			@Override
			public int numSourceDimensions()
			{
				return 2;
			}

			@Override
			public void apply( final RealLocalizable source, final RealPositionable target )
			{
				ScreenTransform.this.applyInverse( target, source );
			}

			@Override
			public void apply( final float[] source, final float[] target )
			{
				ScreenTransform.this.applyInverse( target, source );
			}

			@Override
			public void apply( final double[] source, final double[] target )
			{
				ScreenTransform.this.applyInverse( target, source );
			}

			@Override
			public InvertibleRealTransform inverse()
			{
				return ScreenTransform.this;
			}

			@Override
			public InvertibleRealTransform copy()
			{
				return ScreenTransform.this.copy().inverse();
			}

			@Override
			public void applyInverse( final RealPositionable source, final RealLocalizable target )
			{
				ScreenTransform.this.apply( target, source );
			}

			@Override
			public void applyInverse( final float[] source, final float[] target )
			{
				ScreenTransform.this.apply( target, source );
			}

			@Override
			public void applyInverse( final double[] source, final double[] target )
			{
				ScreenTransform.this.apply( target, source );
			}
		};
	}
}
