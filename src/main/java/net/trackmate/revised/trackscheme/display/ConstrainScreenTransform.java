package net.trackmate.revised.trackscheme.display;

import net.trackmate.revised.trackscheme.ScreenTransform;

public class ConstrainScreenTransform
{
	/**
	 * Modify a {@link ScreenTransform}, such that it covers at least the
	 * {@code minSize}, does not cover anything outside the
	 * {@code (minBound, maxBound)} interval (all in trackscheme layout
	 * coordinates). If possible, enforcing {@code (minBound, maxBound)} will
	 * maintain the size of the area covered by {@code transform}, that is, the
	 * area of the {@code transform} will be shifted instead of truncated.
	 *
	 * @param transform
	 * @param minSizeX
	 * @param minSizeY
	 * @param minBoundX
	 * @param maxBoundX
	 * @param minBoundY
	 * @param maxBoundY
	 */
	public static void constrainTransform(
			final ScreenTransform transform,
			final double minSizeX,
			final double minSizeY,
			final double minBoundX,
			final double maxBoundX,
			final double minBoundY,
			final double maxBoundY )
	{
		double minX = transform.getMinX();
		double maxX = transform.getMaxX();
		double minY = transform.getMinY();
		double maxY = transform.getMaxY();
		final int screenWidth = transform.getScreenWidth();
		final int screenHeight = transform.getScreenHeight();

		// check < minimal size
		if ( maxX - minX < minSizeX )
		{
			final double c = ( minX + maxX ) / 2;
			minX = c - minSizeX / 2;
			maxX = c + minSizeX / 2;
		}
		if ( maxY - minY < minSizeY )
		{
			final double c = ( minY + maxY ) / 2;
			minY = c - minSizeY / 2;
			maxY = c + minSizeY / 2;
		}

		// check > max size
		if ( maxX - minX > maxBoundX - minBoundX )
		{
			minX = minBoundX;
			maxX = maxBoundX;
		}
		if ( maxY - minY > maxBoundY - minBoundY )
		{
			minY = minBoundY;
			maxY = maxBoundY;
		}

		// check out of bounds
		if ( minX < minBoundX )
		{
			final double s = maxX - minX;
			minX = minBoundX;
			maxX = minX + s;
		}
		else if ( maxX > maxBoundX )
		{
			final double s = maxX - minX;
			maxX = maxBoundX;
			minX = maxX - s;
		}
		if ( minY < minBoundY )
		{
			final double s = maxY - minY;
			minY = minBoundY;
			maxY = minY + s;
		}
		else if ( maxY > maxBoundY )
		{
			final double s = maxY - minY;
			maxY = maxBoundY;
			minY = maxY - s;
		}

		transform.set( minX, maxX, minY, maxY, screenWidth, screenHeight );
	}

	/**
	 * Check whether the given {@link ScreenTransform} covers an area less or
	 * equal to the specified {@code minSize}.
	 *
	 * @param transform
	 * @param minSizeX
	 * @param minSizeY
	 * @return
	 */
	public static boolean hasMinSize(
			final ScreenTransform transform,
			final double minSizeX,
			final double minSizeY )
	{
		return hasMinSizeX( transform, minSizeX ) || hasMinSizeY( transform, minSizeY );
	}

	/**
	 * Check whether the given {@link ScreenTransform} covers an area having
	 * width less or equal to the specified {@code minSizeX}.
	 *
	 * @param transform
	 * @param minSizeX
	 * @return true, iff {@code transform} covers less than or equal to
	 *         {@code minSizeX}.
	 */
	public static boolean hasMinSizeX(
			final ScreenTransform transform,
			final double minSizeX )
	{
		return transform.getMaxX() - transform.getMinX() <= minSizeX;
	}

	/**
	 * Check whether the given {@link ScreenTransform} covers an area having
	 * width less or equal to the specified {@code minSizeY}.
	 *
	 * @param transform
	 * @param minSizeX
	 * @return true, iff {@code transform} covers less than or equal to
	 *         {@code minSizeY}.
	 */
	public static boolean hasMinSizeY(
			final ScreenTransform transform,
			final double minSizeY )
	{
		return transform.getMaxY() - transform.getMinY() <= minSizeY;
	}
}
