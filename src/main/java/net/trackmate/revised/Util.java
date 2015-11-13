package net.trackmate.revised;

public class Util
{
	/**
	 * Computes the distance of a point <code>A0 (x0, y0)</code> to a segment
	 * defined by two points <code>A1 (x1, y1)</code> and
	 * <code>A2 (x2, y2)</code>. Returns <code>infinity</code> if the projection
	 * of <code>A0</code> on the segment does not lie between <code>A1</code>
	 * and <code>A2</code>.
	 *
	 * @param x0
	 *            x coordinate of point A0.
	 * @param y0
	 *            y coordinate of point A0.
	 * @param x1
	 *            x coordinate of segment extremity A1.
	 * @param y1
	 *            y coordinate of segment extremity A1.
	 * @param x2
	 *            x coordinate of segment extremity A2.
	 * @param y2
	 *            y coordinate of segment extremity A2.
	 * @return the distance from a line to a segment.
	 */
	public static final double segmentDist( final double x0, final double y0, final double x1, final double y1, final double x2, final double y2 )
	{
		final double l12sq = ( x2 - x1 ) * ( x2 - x1 ) + ( y2 - y1 ) * ( y2 - y1 );

		final double x = ( ( x0 - x1 ) * ( x2 - x1 ) + ( y0 - y1 ) * ( y2 - y1 ) ) / l12sq;
		if ( x < 0 || x > 1 ) { return Double.POSITIVE_INFINITY; }

		final double d = Math.abs(
				( y2 - y1 ) * x0 - ( x2 - x1 ) * y0 + x2 * y1 - y2 * x1
				) / Math.sqrt( l12sq );
		return d;
	}

	private Util()
	{}

}
