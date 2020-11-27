package tpietzsch.util;

import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class MatrixMath
{
	public static Vector4f homogPlane( HyperPlane plane )
	{
		assert plane.numDimensions() == 3;
		float x = ( float ) plane.getNormal()[ 0 ];
		float y = ( float ) plane.getNormal()[ 1 ];
		float z = ( float ) plane.getNormal()[ 2 ];
		float w = ( float ) -plane.getDistance();
		return new Vector4f( x, y, z, w );
	}

	public static HyperPlane hyperPlane( Vector4f plane )
	{
		return new HyperPlane( plane.x(), plane.y(), plane.z(), -plane.w() );
	}

	/**
	 * Setup view and projection frustum transformation such that a camera looks at (the center of) the (BDV) screen plane.
	 *
	 * @param dCam distance camera to z=0 plane
	 * @param dClip visible depth away from z=0 in both directions
	 * @param screenWidth the width of the screen
	 * @param screenHeight the height of the screen
	 * @param screenPadding additional padding on all sides of the screen plane
	 * @param matrix is set to the screenPerspective transformation and returned
	 */
	public static Matrix4f screenPerspective( double dCam, final double dClip, final double screenWidth, final double screenHeight, final double screenPadding, Matrix4f matrix )
	{
		return screenPerspective( dCam, dClip, dClip, screenWidth, screenHeight, screenPadding, matrix );
	}

	/**
	 * Setup view and projection frustum transformation such that a camera looks at (the center of) the (BDV) screen plane.
	 *
	 * @param dCam distance camera to z=0 plane
	 * @param dClipNear visible depth away from z=0 towards camera
	 * @param dClipFar visible depth away from z=0 away from camera
	 * @param screenWidth the width of the screen
	 * @param screenHeight the height of the screen
	 * @param screenPadding additional padding on all sides of the screen plane
	 * @param matrix is set to the screenPerspective transformation and returned
	 */
	public static Matrix4f screenPerspective( double dCam, final double dClipNear, final double dClipFar, final double screenWidth, final double screenHeight, final double screenPadding, Matrix4f matrix )
	{
		double r0 = ( screenWidth + screenPadding ) / 2;
		double t0 = ( screenHeight + screenPadding ) / 2;

		double p = ( dCam - dClipNear ) / dCam;
		float t = ( float ) ( t0 * p );
		float r = ( float ) ( r0 * p );
		float b = -t;
		float l = -r;
		float n = ( float ) ( dCam - dClipNear );
		float f = ( float ) ( dCam + dClipFar );

		matrix
				.setFrustum( l, r, b, t, n, f )
				.scale( 1f, -1f, -1f )
				.translate( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam );
		return matrix;

		// frustum by hand...
		/*
		float m00 = ( float ) ( 2 * n / ( r - l ) );
		float m11 = ( float ) ( 2 * n / ( t - b ) );
		float m02 = ( float ) ( ( r + l ) / ( r - l ) );
		float m12 = ( float ) ( ( t + b ) / ( t - b ) );
		float m22 = ( float ) ( -( f + n ) / ( f - n ) );
		float m23 = ( float ) ( -2 * f * n / ( f - n ) );

		matrix.set(
				m00, 0, 0, 0,
				0, m11, 0, 0,
				m02, m12, m22, -1,
				0, 0, m23, 0
		);
		*/
	}

	/**
	 * Set {@code matrix } to the "bdv screen transformation":
	 * <ol>
	 *     <li>translate the origin from the upper-left corner to the center of the screen (in XY).</li>
	 *     <li>translate the origin back from the screen by {@code dCam} in Z</li>
	 * </ol>
	 * This corresponds to a camera in front of the screen, looking at (the center of) the (BDV) screen plane.
	 * It transforms coordinates in the BDV screen coordinate system (X left, Y down, Z into the screen, origin at the upper-left screen corner)
	 * to BVV camera coordinates (still X left, Y down, Z into the screen, but origin in XY center of the screen, and in front of the screen by {@code z=dCam}).
	 *
	 * @param dCam
	 * 		distance camera to z=0 plane
	 * @param screenWidth
	 * 		the width of the screen
	 * @param screenHeight
	 * 		the height of the screen
	 * @param matrix
	 * 		is set to the screen transformation and returned
	 */
	public static Matrix4f screen( double dCam, final double screenWidth, final double screenHeight, Matrix4f matrix )
	{
		return matrix.translation( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam );
	}

	/**
	 * Setup projection frustum transformation such that the (BDV) screen plane at distance {@code dCam} exactly touches the frustum sides.
	 * Also invert the Y and Z axes to rotate from BVV camera coordinates (right-handed, X left, Y down, Z into screen) to OpenGL (right-handed, X left, Y up, Z out of screen) coordinates.
	 *
	 * @param dCam
	 * 		distance camera to z=0 plane
	 * @param dClipNear
	 * 		visible depth away from z=0 towards camera
	 * @param dClipFar
	 * 		visible depth away from z=0 away from camera
	 * @param screenWidth
	 * 		the width of the screen
	 * @param screenHeight
	 * 		the height of the screen
	 * @param screenPadding
	 * 		additional padding on all sides of the screen plane
	 * @param matrix
	 * 		is set to the screenPerspective transformation and returned
	 */
	public static Matrix4f perspective( double dCam, final double dClipNear, final double dClipFar, final double screenWidth, final double screenHeight, final double screenPadding, Matrix4f matrix )
	{
		double r0 = ( screenWidth + screenPadding ) / 2;
		double t0 = ( screenHeight + screenPadding ) / 2;

		double p = ( dCam - dClipNear ) / dCam;
		float t = ( float ) ( t0 * p );
		float r = ( float ) ( r0 * p );
		float b = -t;
		float l = -r;
		float n = ( float ) ( dCam - dClipNear );
		float f = ( float ) ( dCam + dClipFar );

		matrix
				.setFrustum( l, r, b, t, n, f )
				.scale( 1f, -1f, -1f );
		return matrix;
	}

	/**
	 * Apply a {@link AffineTransform3D} to the specified matrix.
	 *
	 * @param affine the affine transformation
	 * @param matrix affine transformation is applied to this matrix
	 */
	public static Matrix4f affine( final AffineTransform3D affine, final Matrix4f matrix )
	{
		final float[] m = new float[ 16 ];
		for ( int c = 0; c < 4; ++c )
			for ( int r = 0; r < 3; ++r )
				m[ c * 4 + r ] = ( float ) affine.get( r, c );
		m[ 3 ] = 0f;
		m[ 7 ] = 0f;
		m[ 11 ] = 0f;
		m[ 15 ] = 1f;

		if ( ( matrix.properties() & Matrix4fc.PROPERTY_IDENTITY ) != 0 )
			matrix.set( m );
		else if ( ( matrix.properties() & Matrix4fc.PROPERTY_AFFINE ) != 0 )
			matrix.mulAffine( new Matrix4f().set( m ) );
		else
			matrix.mul( new Matrix4f().set( m ) );

		return matrix;
	}


	private MatrixMath()
	{
	}
}
