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
	public static class CompactingPool< O extends ModifiableRef< O > >
	{
		private final ModifiableRefPool< O > pool;

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
		private final IntRefMap< O > keyToObj;
		private final RefIntMap< O > objToKey;

		public CompactingPool( final ModifiableRefPool< O > pool, final int initialCapacity )
		{
			this.pool = pool;
			keyToObj = new IntRefHashMap<>( pool, NO_ENTRY_KEY, initialCapacity );
			objToKey = new RefIntHashMap<>( pool, NO_ENTRY_VALUE, initialCapacity );
		}

		// TODO version with reusable ref
		public O get( final int key )
		{
			return keyToObj.get( key );
		}

		// TODO version with reusable ref
		public O getOrAdd( final int key )
		{
			O obj = get( key );
			if ( obj == null )
			{
				// create new value
				obj = pool.create( pool.createRef() );
				keyToObj.put( key, obj );
				objToKey.put( obj, key );
			}
			return obj;
		}

		public int size()
		{
			return pool.size();
		}

		public void remove( final int key )
		{
			// TODO reusable refs
			final O obj = keyToObj.remove( key );
			if ( obj == null )
				throw new NoSuchElementException();
			if ( pool.getId( obj ) == size() - 1 )
			{
				objToKey.remove( obj );
				pool.delete( obj );
			}
			else
			{
				// swap with last, and remove last
				final O lastObj = pool.getObject( size() - 1, pool.createRef() );
				obj.set( lastObj );
				final int lastKey = objToKey.remove( lastObj );
				objToKey.put( obj, lastKey );
				keyToObj.put( lastKey, obj );
				pool.delete( lastObj );
			}
		}
	}

	// TODO move to mastodon-collection?
	public interface ModifiableRefPool< O > extends RefPool< O >
	{
		int size();

		void delete( final O obj );

		O create( final O ref );
	}

	// TODO move to mastodon-collection?
	public interface ModifiableRef< O extends ModifiableRef< O > > extends Ref< O >
	{
		void set( final O obj );
	}

	public static class EllipsoidPool implements ModifiableRefPool< Ellipsoid >
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

	public static class Ellipsoid implements ModifiableRef< Ellipsoid >
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

		@Override
		public void set( final Ellipsoid obj )
		{
			shape.set( obj.shape );
			color.set( obj.color );
		}
	}
}
