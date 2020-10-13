package org.mastodon.views.bvv;

import net.imglib2.util.LinAlgHelpers;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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
public class Ellipsoid extends PoolObject< Ellipsoid, EllipsoidPool, BufferMappedElement >
{
	public static class EllipsoidLayout extends PoolObjectLayoutJoml
	{
		final Matrix3fField mat3fE = matrix3fField();
		final Matrix3fField mat3fInvE = matrix3fField();
		final Vector3fField vec3fT = vector3fField();
	}

	public static EllipsoidLayout layout = new EllipsoidLayout();

	public final Matrix3fAttributeValue e;
	public final Matrix3fAttributeValue inve;
	public final Vector3fAttributeValue t;

	Ellipsoid( final EllipsoidPool pool )
	{
		super( pool );
		e = pool.mat3fE.createQuietAttributeValue( this );
		inve = pool.mat3fInvE.createQuietAttributeValue( this );
		t = pool.vec3fT.createQuietAttributeValue( this );
	}

	public Ellipsoid init()
	{
		e.identity();
		inve.identity();
		t.zero();
		return this;
	}

	public void set( Ellipsoid other )
	{
		this.e.set( other.e );
		this.inve.set( other.inve );
		this.t.set( other.t );
	}

	public void set(
			final Matrix3fc e,
			final Matrix3fc inve,
			final Vector3fc t )
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
		return String.format( "Ellipsoid(%d, pos=%s, e=%s, e^-1=%s)",
				getInternalPoolIndex(),
				t.get().toString(),
				e.get().toString(),
				inve.get().toString() );
	}
}
