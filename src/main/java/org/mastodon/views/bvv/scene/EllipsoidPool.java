package org.mastodon.views.bvv.scene;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EllipsoidPool implements ModifiableRefPool< Ellipsoid >
{
	private final EllipsoidShapePool shapes;

	private final ColorPool colors;

	private final ConcurrentLinkedQueue< Ellipsoid > tmpObjRefs;

	public EllipsoidPool( final EllipsoidShapePool shapes, final ColorPool colors )
	{
		this.shapes = shapes;
		this.colors = colors;
		tmpObjRefs = new ConcurrentLinkedQueue<>();
	}

	@Override
	public Ellipsoid createRef()
	{
		final Ellipsoid obj = tmpObjRefs.poll();
		return obj == null ? new Ellipsoid( this ) : obj;
	}

	@Override
	public void releaseRef( final Ellipsoid obj )
	{
		tmpObjRefs.add( obj );
	}

	@Override
	public Ellipsoid getObject( final int id, final Ellipsoid obj )
	{
		shapes.getObject( id, obj.shape );
		colors.getObject( id, obj.color );
		return obj;
	}

	@Override
	public Ellipsoid getObjectIfExists( final int id, final Ellipsoid obj )
	{
		if ( shapes.getObject( id, obj.shape ) == null )
			return null;
		colors.getObject( id, obj.color );
		return obj;
	}

	@Override
	public int getId( final Ellipsoid o )
	{
		return shapes.getId( o.shape );
	}

	@Override
	public Class< Ellipsoid > getRefClass()
	{
		return Ellipsoid.class;
	}

	@Override
	public int size()
	{
		return shapes.size();
	}

	@Override
	public void delete( final Ellipsoid obj )
	{
		shapes.delete( obj.shape );
		colors.delete( obj.color );
	}

	@Override
	public Ellipsoid create( final Ellipsoid ref )
	{
		shapes.create( ref.shape );
		colors.create( ref.color );
		return ref;
	}
}
