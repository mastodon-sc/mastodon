package org.mastodon.revised.trackscheme.display;

import org.mastodon.revised.trackscheme.ScreenTransform;

public class ConstrainScreenTransform
{
	/**
	 * Modifies a {@link ScreenTransform}, such that it covers at least the
	 * {@code minSize} and at most the {@code maxSize} (in TrackScheme layout
	 * coordinates). The {@code (minBound, maxBound)} interval specifies the
	 * bounds of the layouted graph in TrackScheme layout coordinates.
	 * <p>
	 * Makes sure that the {@code transform} does not move too far outside the
	 * visible screen area minus {@code borderRatio}, in the following way: If
	 * the transformed {@code (minBound, maxBound)} interval is larger than the
	 * allowed area, the {@code minBoundX} may be transformed only such that
	 * {@code x <= width * borderRatioX}, and analogously for the other bounds.
	 * If the transformed {@code (minBound, maxBound)} interval is smaller than
	 * the allowed area, the {@code minBoundX} may be transformed only such that
	 * {@code x >= width * borderRatioX}, and analogously for the other bounds.
	 *
	 * @param transform
	 *            the transform to modify.
	 * @param minSizeX
	 *            the X min size.
	 * @param minSizeY
	 *            the Y min size.
	 * @param maxSizeX
	 *            the X max size.
	 * @param maxSizeY
	 *            the Y max size.
	 * @param minBoundX
	 *            the X min bound.
	 * @param maxBoundX
	 *            the X max bound.
	 * @param minBoundY
	 *            the Y min bound.
	 * @param maxBoundY
	 *            the Y max bound.
	 * @param borderRatioX
	 *            the X border ratio to limit moving out the visible screen.
	 * @param borderRatioY
	 *            the Y border ratio to limit moving out the visible screen.
	 */
	public static void constrainTransform(
			final ScreenTransform transform,
			final double minSizeX,
			final double minSizeY,
			final double maxSizeX,
			final double maxSizeY,
			final double minBoundX,
			final double maxBoundX,
			final double minBoundY,
			final double maxBoundY,
			final double borderRatioX,
			final double borderRatioY )
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
		if ( maxX - minX > maxSizeX )
		{
			final double c = ( minX + maxX ) / 2;
			minX = c - maxSizeX / 2;
			maxX = c + maxSizeX / 2;
		}
		if ( maxY - minY > maxSizeY )
		{
			final double c = ( minY + maxY ) / 2;
			minY = c - maxSizeY / 2;
			maxY = c + maxSizeY / 2;
		}

		// check out of bounds
		final double scaleX = ( maxX - minX ) / ( screenWidth - 1 );
		final double scaleY = ( maxY - minY ) / ( screenHeight - 1 );
		final double borderX = scaleX * screenWidth * borderRatioX;
		final double borderY = scaleY * screenHeight * borderRatioY;
		final double w = maxX - minX;
		final double addw = Math.max( borderX, w - ( maxBoundX - minBoundX ) - borderX );
		final double h = maxY - minY;
		final double addh = Math.max( borderY, h - ( maxBoundY - minBoundY ) - borderY );
		if ( minX < minBoundX - addw )
		{
			minX = minBoundX - addw;
			maxX = minX + w;
		}
		else if ( maxX > maxBoundX + addw )
		{
			maxX = maxBoundX + addw;
			minX = maxX - w;
		}
		if ( minY < minBoundY - addh )
		{
			minY = minBoundY - addh;
			maxY = minY + h;
		}
		else if ( maxY > maxBoundY + addh )
		{
			maxY = maxBoundY + addh;
			minY = maxY - h;
		}

		transform.set( minX, maxX, minY, maxY, screenWidth, screenHeight );
	}


	/**
	 * Zoom out in X to the maximally allowed width.
	 *
	 * @param transform
	 *            the transform to modify.
	 * @param maxSizeX
	 *            the maximal allowed width.
	 * @param minBoundX
	 *            the X min bound.
	 * @param maxBoundX
	 *            the X max bound.
	 * @param borderRatioX
	 *            the X border ratio to limit moving out the visible screen.
	 */
	public static void zoomOutFullyX(
			final ScreenTransform transform,
			final double maxSizeX,
			final double minBoundX,
			final double maxBoundX,
			final double borderRatioX )
	{
		final int screenWidth = transform.getScreenWidth();
		final double scaleX = maxSizeX / ( screenWidth - 1 );
		final double borderX = scaleX * screenWidth * borderRatioX;
		final double w = maxSizeX;
		final double addw = Math.max( borderX, w - ( maxBoundX - minBoundX ) - borderX );
		final double minX = minBoundX - addw;
		final double maxX = maxBoundX + addw;
		transform.set( minX, maxX, transform.getMinY(), transform.getMaxY(), screenWidth, transform.getScreenHeight() );
	}

	/**
	 * Zoom out in X to the maximally allowed width.
	 *
	 * @param transform
	 *            the transform to modify.
	 * @param maxSizeX
	 *            the maximal allowed width.
	 * @param minBoundX
	 *            the X min bound.
	 * @param maxBoundX
	 *            the X max bound.
	 * @param borderRatioX
	 *            the X border ratio to limit moving out the visible screen.
	 */
	public static void zoomOutFullyY(
			final ScreenTransform transform,
			final double maxSizeY,
			final double minBoundY,
			final double maxBoundY,
			final double borderRatioY )
	{
		final int screenHeight = transform.getScreenHeight();
		final double scaleY = maxSizeY / ( screenHeight - 1 );
		final double borderY = scaleY * screenHeight * borderRatioY;
		final double h = maxSizeY;
		final double addh = Math.max( borderY, h - ( maxBoundY - minBoundY ) - borderY );
		final double minY = minBoundY - addh;
		final double maxY = maxBoundY + addh;
		transform.set( transform.getMinX(), transform.getMaxX(), minY, maxY, transform.getScreenWidth(), screenHeight );
	}


	private static final double JITTER_EPSILON = 0.0000000001;

	/**
	 * If the difference between {@code transform} and {@code reference} is
	 * extremely small, set {@code transform = reference}.
	 *
	 * @param transform
	 *            the transform to modify.
	 * @param reference
	 *            the reference transform to compare.
	 */
	public static void removeJitter( final ScreenTransform transform, final ScreenTransform reference )
	{
		if ( Math.abs( transform.getMinX() - reference.getMinX() ) < JITTER_EPSILON &&
				Math.abs( transform.getMaxX() - reference.getMaxX() ) < JITTER_EPSILON &&
				Math.abs( transform.getMinY() - reference.getMinY() ) < JITTER_EPSILON &&
				Math.abs( transform.getMaxY() - reference.getMaxY() ) < JITTER_EPSILON &&
				transform.getScreenWidth() == reference.getScreenWidth() &&
				transform.getScreenHeight() == reference.getScreenHeight() )
			transform.set( reference );
	}


	/**
	 * Checks whether the given {@link ScreenTransform} covers an area having
	 * width less or equal to the specified {@code minSizeX}.
	 *
	 * @param transform
	 *            the transform to test.
	 * @param minSizeX
	 *            the min width.
	 * @return {@code true}, iff {@code transform} covers less than or equal to
	 *         {@code minSizeX}.
	 */
	public static boolean hasMinSizeX(
			final ScreenTransform transform,
			final double minSizeX )
	{
		return transform.getMaxX() - transform.getMinX() <= minSizeX;
	}

	/**
	 * Checks whether the given {@link ScreenTransform} covers an area having
	 * width less or equal to the specified {@code minSizeY}.
	 *
	 * @param transform
	 *            the transform to test.
	 * @param minSizeY
	 *            the min height.
	 * @return {@code true}, iff {@code transform} covers less than or equal to
	 *         {@code minSizeY}.
	 */
	public static boolean hasMinSizeY(
			final ScreenTransform transform,
			final double minSizeY )
	{
		return transform.getMaxY() - transform.getMinY() <= minSizeY;
	}

	/**
	 * Checks whether the given {@link ScreenTransform} covers an area having
	 * width greater or equal to the specified {@code maxSizeX}.
	 *
	 * @param transform
	 *            the transform to test.
	 * @param maxSizeX
	 *            the size in X to exceed to pass this test.
	 * @return true, iff {@code transform} covers greater than or equal to
	 *         {@code maxSizeX}.
	 */
	public static boolean hasMaxSizeX(
			final ScreenTransform transform,
			final double maxSizeX )
	{
		return transform.getMaxX() - transform.getMinX() >= maxSizeX;
	}

	/**
	 * Checks whether the given {@link ScreenTransform} covers an area having
	 * width greater or equal to the specified {@code maxSizeY}.
	 *
	 * @param transform
	 *            the transform to test.
	 * @param maxSizeY
	 *            the size in Y to exceed to pass this test.
	 * @return true, iff {@code transform} covers greater than or equal to
	 *         {@code maxSizeY}.
	 */
	public static boolean hasMaxSizeY(
			final ScreenTransform transform,
			final double maxSizeY )
	{
		return transform.getMaxY() - transform.getMinY() >= maxSizeY;
	}
}
