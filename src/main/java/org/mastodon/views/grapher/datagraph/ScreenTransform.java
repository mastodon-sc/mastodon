/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.grapher.datagraph;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * A transformation from data graph layout coordinates to screen coordinates.
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
 * Layout coordinates: CAREFUL: the Y coordinates is inverted so that we match
 * the classical display of XY plots (min to max go from bottom to top of the
 * screen).
 * 
 * <pre>
 *      maxY
 * minX +----------------------+ maxX
 *      |                      |
 *      |                      |
 *      |                      |
 *      +----------------------+
 *      minY
 * </pre>
 *
 * @author Tobias Pietzsch
 */
public class ScreenTransform
		implements InvertibleRealTransform, Concatenable< ScreenTransform >, PreConcatenable< ScreenTransform >
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
	 * Instantiate a new blank transform.
	 */
	public ScreenTransform()
	{
		this( 0, 1, 0, 1, 2, 2 );
	}

	/**
	 * Instantiate a new transform.
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
	public ScreenTransform( final double minX, final double maxX, final double minY, final double maxY,
			final int screenWidth, final int screenHeight )
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
	 * Instantiate a new transform as a copy of the specified transform.
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
	 * Set all the values of this transform from the values of the specified
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

	public void set( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth,
			final int screenHeight )
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
	 * Set the screen size.
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
	 * Convert the specified screen X position to a layout X position.
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
	 * Convert the specified screen Y position to a layout Y position.
	 *
	 * @param y
	 *            the screen Y position.
	 * @return the layout Y position.
	 */
	public double screenToLayoutY( final double y )
	{
		return minY + ( ( screenHeight - y ) / scaleY );
	}

	/**
	 * Convert the specified layout X position to a screen X position.
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
	 * Convert the specified layout Y position to a screen Y position.
	 *
	 * @param y
	 *            the layout Y position.
	 * @return the screen Y position.
	 */
	public double layoutToScreenY( final double y )
	{
		return screenHeight - ( ( y - minY ) * scaleY );
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
		minY = lY - ( screenHeight - screenCenterY ) / scaleY;
		maxY = minY + newSizeY;
	}

	/**
	 * Translate such that screen coordinates move by {@code (dX, dY)}
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
	 * Translate such that screen coordinates move by {@code dX} in X.
	 *
	 * @param dX
	 *            the amount, in screen coordinates, to move along the X axis.
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
	 *            the amount, in screen coordinates, to move along the Y axis.
	 */
	public void shiftY( final double dY )
	{
		final double lY = -dY / scaleY;
		minY += lY;
		maxY += lY;
	}

	/**
	 * Translate such that layout coordinates move by {@code dX} in X.
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
	 * Translate such that layout coordinates move by {@code dY} in Y.
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
		this.minX = ( 1 - ratio ) * start.minX + ratio * end.minX;
		this.maxX = ( 1 - ratio ) * start.maxX + ratio * end.maxX;
		this.minY = ( 1 - ratio ) * start.minY + ratio * end.minY;
		this.maxY = ( 1 - ratio ) * start.maxY + ratio * end.maxY;
		this.screenWidth = ( int ) ( ( 1 - ratio ) * start.screenWidth + ratio * end.screenWidth );
		this.screenHeight = ( int ) ( ( 1 - ratio ) * start.screenHeight + ratio * end.screenHeight );
		update();
	}

	@Override
	public String toString()
	{
		return "X: " + minX + " -> " + maxX + ", Y: " + minY + " -> " + maxY + ", width = " + screenWidth
				+ ", height = " + screenHeight;
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
