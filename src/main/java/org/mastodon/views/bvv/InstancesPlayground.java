package org.mastodon.views.bvv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class InstancesPlayground
{

	/**
	 * GPU resources for parameters of instances.
	 * Associated to a pool.
	 * Updates when associated to a new pool, or when the current pool is modified.
	 *
	 * @param <K>
	 */
	static abstract class ReusableInstanceArray< K >
	{
		protected K pool = null;

		protected boolean needsUpdate = false;

		int lru = -1;

		void associate( K key )
		{
			this.pool = key;
			needsUpdate = true;
		}
	}

	static class ReusableInstanceArrays< K, A extends ReusableInstanceArray< K > >
	{
		private int lrutimestamp = 1;
		private final List< A > arrays = new ArrayList<>();
		private final Map< K, A > keyToArray = new WeakHashMap<>();

		// TODO:
		//  initialization: fill arrays list

		A get( K key )
		{
			A array = keyToArray.get( key );
			if ( array != null )
			{
				array.lru = lrutimestamp++;
				return array;
			}

			for ( A a : arrays )
			{
				if ( a.pool == null )
				{
					array = a;
					break;
				}
			}

			if ( array == null )
			{
				arrays.sort( Comparator.comparingInt( a -> a.lru ) );
				array = arrays.get( 0 );
			}

			if ( array.pool != null )
				keyToArray.remove( array.pool );

			array.lru = lrutimestamp++;
			array.associate( key );
			keyToArray.put( key, array );

			return array;
		}
	}

}
