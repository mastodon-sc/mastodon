package org.mastodon.views.bvv;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.mastodon.Ref;
import org.mastodon.RefPool;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.collection.ref.RefIntHashMap;

public class EllipsoidPlayground
{

	// TODO extract CompactingPool (to be reused for Cylinders)
	public static class Ellipsoids
	{
		private final EllipsoidPool pool;

		/**
		 * Int value used to declare that the requested key is not in the map.
		 * Negative, so that it cannot be an index in the pool.
		 */
		private static final int NO_ENTRY_KEY = -1;

		/**
		 * Int value used to declare that the requested value is not in the map.
		 * Negative, so that it cannot be an index in the pool.
		 */
		private static final int NO_ENTRY_VALUE = -2;

		// TODO: extract RefIntBimap (?)
		private final IntRefMap< Ellipsoid > keyToEllipsoid;
		private final RefIntMap< Ellipsoid > ellipsoidToKey;

		public Ellipsoids( final int initialCapacity )
		{
			pool = new EllipsoidPool( initialCapacity );
			keyToEllipsoid = new IntRefHashMap<>( pool, NO_ENTRY_KEY, initialCapacity );
			ellipsoidToKey = new RefIntHashMap<>( pool, NO_ENTRY_VALUE, initialCapacity );
		}

		// TODO version with reusable ref
		public Ellipsoid get( final int key )
		{
			return keyToEllipsoid.get( key );
		}

		// TODO version with reusable ref
		public Ellipsoid getOrAdd( final int key )
		{
			Ellipsoid ellipsoid = get( key );
			if ( ellipsoid == null )
			{
				// create new value
				ellipsoid = pool.create( pool.createRef() );
				keyToEllipsoid.put( key, ellipsoid );
				ellipsoidToKey.put( ellipsoid, key );
			}
			return ellipsoid;
		}

		public int size()
		{
			return pool.size();
		}

		public void remove( final int key )
		{
			// TODO reusable refs
			final Ellipsoid ellipsoid = keyToEllipsoid.remove( key );
			if ( ellipsoid == null )
				throw new NoSuchElementException();
			if ( ellipsoid.getInternalPoolIndex() == size() - 1 )
			{
				ellipsoidToKey.remove( ellipsoid );
				pool.delete( ellipsoid );
			}
			else
			{
				// swap with last, and remove last
				final Ellipsoid last = pool.getObject( size() - 1, pool.createRef() );
				ellipsoid.set( last );
				final int lastKey = ellipsoidToKey.remove( last );
				ellipsoidToKey.put( ellipsoid, lastKey );
				keyToEllipsoid.put( lastKey, ellipsoid );
				pool.delete( last );
			}
		}
	}

	public static class EllipsoidPool implements RefPool< Ellipsoid >
	{
		final EllipsoidShapePool shapes; // TODO: private?

		final ColorPool colors; // TODO: private?

		private final ConcurrentLinkedQueue< Ellipsoid > tmpObjRefs;

		public EllipsoidPool( final int initialCapacity )
		{
			shapes = new EllipsoidShapePool( initialCapacity );
			colors = new ColorPool( initialCapacity );
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

		// TODO interface method
		public int size()
		{
			return shapes.size();
		}

		// TODO interface method
		public void delete( final Ellipsoid obj )
		{
			shapes.delete( obj.shape );
			colors.delete( obj.color );
		}

		// TODO interface method
		public Ellipsoid create( final Ellipsoid ref )
		{
			shapes.create( ref.shape );
			colors.create( ref.color );
			return ref;
		}
	}

	public static class Ellipsoid implements Ref< Ellipsoid >
	{
		final EllipsoidPool pool; // TODO: private?
		final EllipsoidShape shape;
		final Color color;

		Ellipsoid( EllipsoidPool pool )
		{
			this.pool = pool;
			shape = pool.shapes.createRef();
			color = pool.colors.createRef();
		}

		@Override
		public int getInternalPoolIndex()
		{
			return shape.getInternalPoolIndex();
		}

		@Override
		public Ellipsoid refTo( final Ellipsoid obj )
		{
			return pool.getObject( obj.getInternalPoolIndex(), this );
		}

		public void set( final Ellipsoid obj )
		{
			shape.set( obj.shape );
			color.set( obj.color );
		}
	}
}
