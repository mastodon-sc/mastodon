package org.mastodon.views.bvv.scene;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

class EllipsoidPool implements ModifiableRefPool< Ellipsoid >
{
	final EllipsoidShapePool shapes;

	final ColorPool colors;

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

	@Override
	public Iterator< Ellipsoid > iterator()
	{
		return iterator( createRef() );
	}

	// garbage-free version
	public Iterator< Ellipsoid > iterator( final Ellipsoid obj )
	{
		final Iterator< EllipsoidShape > si = shapes.iterator( obj.shape );
		final Iterator< Color > ci = colors.iterator( obj.color );
		return new Iterator< Ellipsoid >()
		{
			@Override
			public boolean hasNext()
			{
				return si.hasNext();
			}

			@Override
			public Ellipsoid next()
			{
				si.next();
				ci.next();
				return obj;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
