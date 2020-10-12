package org.mastodon.views.bvv;

import net.imglib2.util.LinAlgHelpers;
import org.joml.Matrix3f;
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
 * Cylinder instance in vertex attribute array.
 *
 * @author Tobias Pietzsch
 */
public class CylinderInstance extends PoolObject< CylinderInstance, CylinderInstances, BufferMappedElement >
{
	public static class CylinderInstanceLayout extends PoolObjectLayoutJoml
	{
		final Matrix3fField mat3fE = matrix3fField();
		final Matrix3fField mat3fInvE = matrix3fField();
		final Vector3fField vec3fT = vector3fField();
	}

	public static CylinderInstanceLayout layout = new CylinderInstanceLayout();

	public final Matrix3fAttributeValue e;
	public final Matrix3fAttributeValue inve;
	public final Vector3fAttributeValue t;

	CylinderInstance( final CylinderInstances pool )
	{
		super( pool );
		e = pool.mat3fE.createQuietAttributeValue( this );
		inve = pool.mat3fInvE.createQuietAttributeValue( this );
		t = pool.vec3fT.createQuietAttributeValue( this );
	}

	public CylinderInstance init()
	{
		e.identity();
		inve.identity();
		t.zero();
		return this;
	}

	public CylinderInstance init( BvvEdge< ?, ? > edge )
	{
		set( edge );
		return this;
	}

	public void set( BvvEdge< ?, ? > edge )
	{
		// TODO: avoid object creation (reuse joml objects)
		// TODO: will this create a new vertex ref?
		final BvvVertex< ?, ? > from = edge.getSource();
		final BvvVertex< ?, ? > to = edge.getSource();
		set(
				new Vector3f( from.x(), from.y(), from.z() ),
				new Vector3f( to.x(), to.y(), to.z() ) );
	}

	public CylinderInstance init( final Vector3fc from, final Vector3fc to )
	{
		set( from, to );
		return this;
	}

	public void set( final Vector3fc from, final Vector3fc to )
	{
		// TODO: avoid object creation (reuse joml objects)
		final Vector3f k = to.sub( from, new Vector3f() );
		final float length = k.length();
		if ( length < 0.0001 )
		{
			throw new RuntimeException( "TODO. not implemented yet" );
			// need to set some default parameters here
		}
		k.mul( 1.0f / length );
		final Vector3f ei = new Vector3f().setComponent( k.minComponent(), 1 );

		final Vector3f l = k.cross( ei, new Vector3f() );
		final Vector3f m = l.cross( k, new Vector3f() );

		final Matrix3f E = new Matrix3f( l, m, k );
		final Matrix3f Einv = E.scale( 1, 1, 1 / length, new Matrix3f() );
		E.scale( 1, 1, length );

		e.set( E );
		inve.set( Einv );
		t.set( from );
	}

	public void set( CylinderInstance other )
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
		return String.format( "CylinderInstance(%d, pos=%s, e=%s, e^-1=%s)",
				getInternalPoolIndex(),
				t.get().toString(),
				e.get().toString(),
				inve.get().toString() );
	}
}
