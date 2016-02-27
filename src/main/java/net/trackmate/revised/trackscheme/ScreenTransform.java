package net.trackmate.revised.trackscheme;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * A transformation from trackscheme layout coordinates to screen coordinates.
 * <p>
 * It is defined by a bounding box in trackscheme layout coordinates, and the
 * size of the screen that this should be mapped to.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenTransform implements InvertibleRealTransform, Concatenable< ScreenTransform >, PreConcatenable< ScreenTransform >
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
	{
		this ( 0, 1, 0, 1, 2, 2 );
	}

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

	public void set( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		update();
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
	 * Get the x scale factor. This is the factor by which layout <em>x</em>
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
	 * Get the y scale factor. This is the factor by which layout <em>y</em>
	 * coordinates have to be multiplied to obtain screen <em>y</em>
	 * coordinates.
	 *
	 * @return the y scale factor.
	 */
	public double getScaleY()
	{
		return scaleY;
	}

	/**
	 * Returns the distortion factor between layout X and Y layout coordinates.
	 *
	 * @return the X/Y coordinate ratio factor.
	 */
	public double getXtoYRatio()
	{
		return scaleY / scaleX;
	}

	/**
	 * Set the screen size.
	 *
	 * @param w
	 * @param h
	 */
	public void setScreenSize( final int w, final int h )
	{
		this.screenWidth = w;
		this.screenHeight = h;
		update();
	}

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
	 * Zoom by specified scale factor, keeping screen coordinates
	 * {@code (screenCenterX, screenCenterY)} fixed.
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
	 * Zoom in X by specified scale factor, keeping screen X coordinate
	 * {@code screenCenterX} fixed.
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
	 * Zoom in Y by specified scale factor, keeping screen Y coordinate
	 * {@code screenCenterY} fixed.
	 *
	 * @param scale
	 * @param screenCenterX
	 */
	public void zoomY( final double scale, final double screenCenterX )
	{
		final double lY = screenToLayoutY( screenCenterX );
		final double newSizeY = ( maxY - minY ) * scale;
		scaleY = ( screenHeight - 1 ) / newSizeY;
		minY = lY - screenCenterX / scaleY;
		maxY = minY + newSizeY;
	}

	/**
	 * Translate such that screen coordinates move by {@code (dX, dY)}
	 *
	 * @param dX
	 * @param dY
	 */
	public void shift( final int dX, final int dY )
	{
		shiftX( dX );
		shiftY( dY );
	}

	/**
	 * Translate such that screen coordinates move by {@code dX} in X.
	 *
	 * @param dX
	 */
	public void shiftX( final double dX )
	{
		final double lX = dX / scaleX;
		minX += lX;
		maxX += lX;
	}

	/**
	 * Translate such that screen coordinates move by {@code dY} in Y.
	 *
	 * @param dY
	 */
	public void shiftY( final double dY )
	{
		final double lY = dY / scaleY;
		minY += lY;
		maxY += lY;
	}

	/**
	 * Translate such that layout coordinates move by {@code dX} in X.
	 *
	 * @param dX
	 */
	public void shiftLayoutX( final double dX )
	{
		minX += dX;
		maxX += dX;
	}

	/**
	 * Translate such that layout coordinates move by {@code dY} in Y.
	 *
	 * @param dY
	 */
	public void shiftLayoutY( final double dY )
	{
		minY += dY;
		maxY += dY;
	}

	/**
	 * Set this transform to linear interpolation {@code (1 - ratio) * start + ratio * end}.
	 */
	public void interpolate( final ScreenTransform start, final ScreenTransform end, final double ratio )
	{
		this.minX = (1 - ratio) * start.minX + ratio * end.minX;
		this.maxX = (1 - ratio) * start.maxX + ratio * end.maxX;
		this.minY = (1 - ratio) * start.minY + ratio * end.minY;
		this.maxY = (1 - ratio) * start.maxY + ratio * end.maxY;
		this.screenWidth = ( int ) Math.round( ( (1 - ratio) * start.screenWidth + ratio * end.screenWidth ) );
		this.screenHeight = ( int ) Math.round( ( (1 - ratio) * start.screenHeight + ratio * end.screenHeight ) );
		update();
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
	public ScreenTransform inverse()
	{
		final double iMinX = -minX * scaleX;
		final double iMaxX = ( screenWidth - 1 - minX ) * scaleX;
		final double iMinY = -minY * scaleY;
		final double iMaxY = ( screenHeight - 1 - minY ) * scaleY;
		return new ScreenTransform( iMinX, iMaxX, iMinY, iMaxY, screenWidth, screenHeight );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if ( obj == null || !( obj instanceof ScreenTransform ) )
			return false;

		final ScreenTransform t = ( ScreenTransform ) obj;
		return t.minX == minX &&
				t.maxX == maxX &&
				t.minY == minY &&
				t.maxY == maxY &&
				t.screenWidth == screenWidth &&
				t.screenHeight == screenHeight;
	}

	@Override
	public ScreenTransform concatenate( final ScreenTransform a )
	{
		final double cMinX = a.minX + minX / a.scaleX;
		final double cMaxX = ( screenWidth - 1 ) / ( a.scaleX * scaleX ) + cMinX;
		final double cMinY = a.minY + minY / a.scaleY;
		final double cMaxY = ( screenHeight - 1 ) / ( a.scaleY * scaleY ) + cMinY;
		return new ScreenTransform( cMinX, cMaxX, cMinY, cMaxY, screenWidth, screenHeight );
	}

	@Override
	public Class< ScreenTransform > getConcatenableClass()
	{
		return ScreenTransform.class;
	}

	@Override
	public ScreenTransform preConcatenate( final ScreenTransform a )
	{
		final double cMinX = minX + a.minX / scaleX;
		final double cMaxX = ( screenWidth - 1 ) / ( scaleX * a.scaleX ) + cMinX;
		final double cMinY = minY + a.minY / scaleY;
		final double cMaxY = ( screenHeight - 1 ) / ( scaleY * a.scaleY ) + cMinY;
		return new ScreenTransform( cMinX, cMaxX, cMinY, cMaxY, screenWidth, screenHeight );
	}

	@Override
	public Class< ScreenTransform > getPreConcatenableClass()
	{
		return ScreenTransform.class;
	}
}
