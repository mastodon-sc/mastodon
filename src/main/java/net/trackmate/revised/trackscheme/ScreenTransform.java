package net.trackmate.revised.trackscheme;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * A transformation from some layout coordinates to screen coordinates.
 * <p>
 * It is defined by a bounding box in layout coordinates, and the size of the
 * screen that this should be mapped to. The forward direction of this transform
 * maps layout coordinates to screen coordinates. The inverse transformation
 * maps screen coordinates to layout coordinates.
 * <p>
 * Screen coordinates:
 *
 * <pre>
 *   0
 * 0 +----------------------+ screenWidth
 *   |                      |
 *   |                      |
 *   |                      |
 *   +----------------------+
 *   screenHeight
 * </pre>
 * <p>
 * Layout coordinates:
 *
 * <pre>
 *      minY
 * minX +----------------------+ maxX
 *      |                      |
 *      |                      |
 *      |                      |
 *      +----------------------+
 *      maxY
 * </pre>
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

	/**
	 * Instantiates a new blank transform.
	 */
	public ScreenTransform()
	{
		this ( 0, 1, 0, 1, 1, 1 );
	}

	/**
	 * Instantiates a new transform.
	 *
	 * @param minX
	 *            the minimal X layout position displayed on screen.
	 * @param maxX
	 *            the maximal X layout position displayed on screen.
	 * @param minY
	 *            the minimal Y layout position displayed on screen.
	 * @param maxY
	 *            the maximal Y layout position displayed on screen.
	 * @param screenWidth
	 *            the screen width.
	 * @param screenHeight
	 *            the screen height.
	 */
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

	/**
	 * Instantiates a new transform from the values of the specified transform.
	 *
	 * @param t
	 *            the transform to copy values from.
	 */
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

	/**
	 * Returns a new copy of this transform.
	 */
	@Override
	public ScreenTransform copy()
	{
		return new ScreenTransform( this );
	}

	/**
	 * Sets all the values of this transform from the values of the specified
	 * transform.
	 *
	 * @param t
	 *            the transform to copy values from.
	 */
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

	/**
	 * Recompute {@link #scaleX} and {@link #scaleY}.
	 */
	private void update()
	{
		scaleX = ( screenWidth - 1 ) / ( maxX - minX );
		scaleY = ( screenHeight - 1 ) / ( maxY - minY );
	}

	/**
	 * Returns the minimal X layout position displayed on screen.
	 *
	 * @return the minimal X layout position.
	 */
	public double getMinX()
	{
		return minX;
	}

	/**
	 * Returns the maximal X layout position displayed on screen.
	 *
	 * @return the maximal X layout position.
	 */
	public double getMaxX()
	{
		return maxX;
	}

	/**
	 * Returns the minimal Y layout position displayed on screen.
	 *
	 * @return the minimal Y layout position.
	 */
	public double getMinY()
	{
		return minY;
	}

	/**
	 * Returns the maximal Y layout position displayed on screen.
	 *
	 * @return the maximal Y layout position.
	 */
	public double getMaxY()
	{
		return maxY;
	}

	/**
	 * Returns the screen width of this transform.
	 *
	 * @return the screen width.
	 */
	public int getScreenWidth()
	{
		return screenWidth;
	}

	/**
	 * Returns the screen height of this transform.
	 *
	 * @return the screen height.
	 */
	public int getScreenHeight()
	{
		return screenHeight;
	}

	/**
	 * Get the X scale factor. This is the factor by which layout <em>X</em>
	 * coordinates have to be multiplied to obtain screen <em>X</em>
	 * coordinates.
	 *
	 * @return the X scale factor.
	 */
	public double getScaleX()
	{
		return scaleX;
	}

	/**
	 * Get the Y scale factor. This is the factor by which layout <em>Y</em>
	 * coordinates have to be multiplied to obtain screen <em>Y</em>
	 * coordinates.
	 *
	 * @return the Y scale factor.
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
	 * Sets the screen size.
	 *
	 * @param w
	 *            the screen width.
	 * @param h
	 *            the screen height.
	 */
	public void setScreenSize( final int w, final int h )
	{
		this.screenWidth = w;
		this.screenHeight = h;
		update();
	}

	/**
	 * Converts a specified screen X position to a layout X position.
	 *
	 * @param x
	 *            the screen X position.
	 * @return the layout X position.
	 */
	public double screenToLayoutX( final double x )
	{
		return minX + x / scaleX;
	}

	/**
	 * Converts a specified screen Y position to a layout Y position.
	 *
	 * @param y
	 *            the screen Y position.
	 * @return the layout Y position.
	 */
	public double screenToLayoutY( final double y )
	{
		return minY + y / scaleY;
	}

	/**
	 * Converts a specified layout X position to a screen X position.
	 *
	 * @param x
	 *            the layout X position.
	 * @return the screen X position.
	 */
	public double layoutToScreenX( final double x )
	{
		return ( x - minX ) * scaleX;
	}

	/**
	 * Converts a specified layout Y position to a screen Y position.
	 *
	 * @param y
	 *            the layout Y position.
	 * @return the screen Y position.
	 */
	public double layoutToScreenY( final double y )
	{
		return ( y - minY ) * scaleY;
	}

	/**
	 * Zoom by specified scale factor, keeping screen coordinates
	 * {@code (screenCenterX, screenCenterY)} fixed.
	 *
	 * @param scale
	 *            the factor by which to zoom. Use values larger than 1 to zoom
	 *            out, and smaller than 1 to zoom in.
	 * @param screenCenterX
	 *            the X screen coordinate to keep fixed while zooming.
	 * @param screenCenterY
	 *            the Y screen coordinate to keep fixed while zooming.
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
	 *            the factor by which to zoom. Use values larger than 1 to zoom
	 *            out, and smaller than 1 to zoom in.
	 * @param screenCenterX
	 *            the X screen coordinate to keep fixed while zooming.
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
	 *            the factor by which to zoom. Use values larger than 1 to zoom
	 *            out, and smaller than 1 to zoom in.
	 * @param screenCenterY
	 *            the Y screen coordinate to keep fixed while zooming.
	 */
	public void zoomY( final double scale, final double screenCenterY )
	{
		final double lY = screenToLayoutY( screenCenterY );
		final double newSizeY = ( maxY - minY ) * scale;
		scaleY = ( screenHeight - 1 ) / newSizeY;
		minY = lY - screenCenterY / scaleY;
		maxY = minY + newSizeY;
	}

	/**
	 * Translates such that screen coordinates move by {@code (dX, dY)}
	 *
	 * @param dX
	 *            the amount, in screen coordinates, to move along the X axis.
	 * @param dY
	 *            the amount, in screen coordinates, to move along the Y axis.
	 */
	public void shift( final int dX, final int dY )
	{
		shiftX( dX );
		shiftY( dY );
	}

	/**
	 * Translates such that screen coordinates move by {@code dX} in X.
	 *
	 * @param dX
	 *            the amount, in screen coordinates, to move along the X axis.
	 */
	public void shiftX( final int dX )
	{
		final double lX = dX / scaleX;
		minX += lX;
		maxX += lX;
	}

	/**
	 * Translates such that screen coordinates move by {@code dY} in Y.
	 *
	 * @param dY
	 *            the amount, in screen coordinates, to move along the Y axis.
	 */
	public void shiftY( final int dY )
	{
		final double lY = dY / scaleY;
		minY += lY;
		maxY += lY;
	}

	/**
	 * Translates such that layout coordinates move by {@code dX} in X.
	 *
	 * @param dX
	 *            the amount, in layout coordinates, to move along the X axis.
	 */
	public void shiftLayoutX( final double dX )
	{
		minX += dX;
		maxX += dX;
	}

	/**
	 * Translates such that layout coordinates move by {@code dY} in Y.
	 *
	 * @param dY
	 *            the amount, in layout coordinates, to move along the Y axis.
	 */
	public void shiftLayoutY( final double dY )
	{
		minY += dY;
		maxY += dY;
	}

	/**
	 * Sets this transform to the linear interpolation
	 * {@code (1 - ratio) * start + ratio * end}.
	 *
	 * @param start
	 *            the starting transform.
	 * @param end
	 *            the ending transform.
	 * @param ratio
	 *            the ratio of interpolation, must be between 0 and 1.
	 */
	public void interpolate( final ScreenTransform start, final ScreenTransform end, final double ratio )
	{
		this.minX = (1 - ratio) * start.minX + ratio * end.minX;
		this.maxX = (1 - ratio) * start.maxX + ratio * end.maxX;
		this.minY = (1 - ratio) * start.minY + ratio * end.minY;
		this.maxY = (1 - ratio) * start.maxY + ratio * end.maxY;
		this.screenWidth = ( int ) ( (1 - ratio) * start.screenWidth + ratio * end.screenWidth );
		this.screenHeight = ( int ) ( (1 - ratio) * start.screenHeight + ratio * end.screenHeight );
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
