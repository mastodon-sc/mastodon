package org.mastodon.views.bvv;

import net.imglib2.util.LinAlgHelpers;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttributeValue;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttributeValue;

/**
 * Ellipsoid instance in vertex attribute array.
 *
 * @author Tobias Pietzsch
 */
public class EllipsoidInstance extends PoolObject< EllipsoidInstance, EllipsoidInstances< ?, ? >, BufferMappedElement >
{
	public static class EllipsoidInstanceLayout extends PoolObjectLayoutJoml
	{
		final Matrix3fField mat3fE = matrix3fField();
		final Matrix3fField mat3fInvE = matrix3fField();
		final Vector3fField vec3fT = vector3fField();
	}

	public static EllipsoidInstanceLayout layout = new EllipsoidInstanceLayout();

	public final Matrix3fAttributeValue e;
	public final Matrix3fAttributeValue inve;
	public final Vector3fAttributeValue t;

	private final JamaEigenvalueDecomposition eig3 = new JamaEigenvalueDecomposition( 3 );
	private final double cov[][] = new double[ 3 ][ 3 ];
	private final float[] edata = new float[ 9 ];
	private final float[] invedata = new float[ 9 ];

	EllipsoidInstance( final EllipsoidInstances< ?, ? > pool )
	{
		super( pool );
		e = pool.mat3fE.createQuietAttributeValue( this );
		inve = pool.mat3fInvE.createQuietAttributeValue( this );
		t = pool.vec3fT.createQuietAttributeValue( this );
	}

	public EllipsoidInstance init()
	{
		e.identity();
		inve.identity();
		t.zero();
		return this;
	}

	public EllipsoidInstance init( BvvVertex< ?, ? > vertex )
	{
		set( vertex );
		return this;
	}

	public void set( BvvVertex< ?, ? > vertex )
	{
		vertex.getCovariance( cov );
		eig3.decomposeSymmetric( cov );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		double det = LinAlgHelpers.det3x3(
				V[ 0 ][ 0 ], V[ 0 ][ 1 ], V[ 0 ][ 2 ],
				V[ 1 ][ 0 ], V[ 1 ][ 1 ], V[ 1 ][ 2 ],
				V[ 2 ][ 0 ], V[ 2 ][ 1 ], V[ 2 ][ 2 ] );
		if ( det < 0 )
			LinAlgHelpers.scale( V, -1, V );
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				edata[ j + 3 * i ] = ( float ) ( e * V[ j ][ i ] );
				invedata[ j + 3 * i ] = ( float ) ( inve * V[ j ][ i ] );
			}
		}

		e.set( edata );
		inve.set( invedata );
		t.set( vertex.x(), vertex.y(), vertex.z() );
	}

	public void set( EllipsoidInstance other )
	{
		this.e.set( other.e );
		this.inve.set( other.inve );
		this.t.set( other.t );
	}

	public void set(
			final Matrix3fc e,
			final Matrix3fc inve,
			final Vector3f t )
	{
		this.e.set( e );
		this.inve.set( inve );
		this.t.set( t );
	}

	@Override
	protected void setToUninitializedState()
	{}

	@Override
	public String toString()
	{
		return String.format( "EllipsoidInstance(%d, pos=%s, e=%s, e^-1=%s)",
				getInternalPoolIndex(),
				t.get().toString(),
				e.get().toString(),
				inve.get().toString() );
	}
}
