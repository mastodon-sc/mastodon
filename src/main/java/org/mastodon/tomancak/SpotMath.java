package org.mastodon.tomancak;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.revised.model.mamut.Spot;

import static net.imglib2.util.LinAlgHelpers.cols;
import static net.imglib2.util.LinAlgHelpers.rows;

/**
 * Adapted from EllipsoidInsideTest, which was in turn adapted from ScreenVertexMath.
 * TODO: is there potential for unification?
 */
public class SpotMath
{
	private final double[] pos1 = new double[ 3 ];
	private final double[] diff = new double[ 3 ];
	private final double[][] cov = new double[ 3 ][ 3 ];
	private final double[][] P = new double[ 3 ][ 3 ];

	/**
	 * Returns {@code true} if the center of {@code s2} is contained in {@code s1} ellipsoid.
	 */
	public boolean containsCenter( final Spot s1, final Spot s2 )
	{
		return mahalanobisDistSqu( s1, s2 ) < 1.0;
	}

	/**
	 * Returns the squared mahalanobis distance of the center of {@code s2} to {@code s1} ellipsoid.
	 */
	public double mahalanobisDistSqu( final Spot s1, final Spot s2 )
	{
		s1.localize( pos1 );
		s1.getCovariance( cov );
		LinAlgHelpers.invertSymmetric3x3( cov, P );

		s2.localize( diff );
		LinAlgHelpers.subtract( diff, pos1, diff );
		return multSymmetric3x3bAb( P, diff );
	}

	static double multSymmetric3x3bAb( final double[][] A, final double[] b )
	{
		assert cols( A ) == 3;
		assert rows( A ) == 3;
		assert rows( b ) == 3;

		final double x = b[ 0 ];
		final double y = b[ 1 ];
		final double z = b[ 2 ];
		return A[ 0 ][ 0 ] * x * x + A[ 1 ][ 1 ] * y * y + A[ 2 ][ 2 ] * z * z + 2 * ( A[ 0 ][ 1 ] * x * y + A[ 0 ][ 2 ] * x * z + A[ 1 ][ 2 ] * y * z );
	}
}
