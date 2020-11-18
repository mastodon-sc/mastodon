package org.mastodon.views.bvv.scene;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

class CylinderPool implements ModifiableRefPool< Cylinder >
{
	final ShapeTransformPool shapes;

	final ColorPool colors;

	private final ConcurrentLinkedQueue< Cylinder > tmpObjRefs;

	public CylinderPool( final ShapeTransformPool shapes, final ColorPool colors )
	{
		this.shapes = shapes;
		this.colors = colors;
		tmpObjRefs = new ConcurrentLinkedQueue<>();
	}

	@Override
	public Cylinder createRef()
	{
		final Cylinder obj = tmpObjRefs.poll();
		return obj == null ? new Cylinder( this ) : obj;
	}

	@Override
	public void releaseRef( final Cylinder obj )
	{
		tmpObjRefs.add( obj );
	}

	@Override
	public Cylinder getObject( final int id, final Cylinder obj )
	{
		shapes.getObject( id, obj.shape );
		colors.getObject( id, obj.color );
		return obj;
	}

	@Override
	public Cylinder getObjectIfExists( final int id, final Cylinder obj )
	{
		if ( shapes.getObjectIfExists( id, obj.shape ) == null )
			return null;
		colors.getObjectIfExists( id, obj.color );
		return obj;
	}

	@Override
	public int getId( final Cylinder o )
	{
		return shapes.getId( o.shape );
	}

	@Override
	public Class< Cylinder > getRefClass()
	{
		return Cylinder.class;
	}

	@Override
	public int size()
	{
		return shapes.size();
	}

	@Override
	public void delete( final Cylinder obj )
	{
		shapes.delete( obj.shape );
		colors.delete( obj.color );
	}

	@Override
	public Cylinder create( final Cylinder ref )
	{
		shapes.create( ref.shape );
		colors.create( ref.color );
		return ref;
	}

	@Override
	public void clear()
	{
		shapes.clear();
		colors.clear();
	}

	@Override
	public Iterator< Cylinder > iterator()
	{
		return iterator( createRef() );
	}

	// garbage-free version
	public Iterator< Cylinder > iterator( final Cylinder obj )
	{
		final Iterator< ShapeTransform > si = shapes.iterator( obj.shape );
		final Iterator< Color > ci = colors.iterator( obj.color );
		return new Iterator< Cylinder >()
		{
			@Override
			public boolean hasNext()
			{
				return si.hasNext();
			}

			@Override
			public Cylinder next()
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
