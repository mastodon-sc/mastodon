package org.mastodon.views.bvv.scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

class ReusableResources< K, A extends ReusableResource< K > >
{
	private int lrutimestamp = 1;
	private final List< A > arrays = new ArrayList<>();
	private final Map< K, A > keyToArray = new WeakHashMap<>();

	ReusableResources( final int size, final Supplier< A > createInstanceArray )
	{
		for ( int i = 0; i < size; ++i )
			arrays.add( createInstanceArray.get() );
	}

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
			if ( a.key == null )
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

		if ( array.key != null )
			keyToArray.remove( array.key );

		array.lru = lrutimestamp++;
		array.associate( key );
		keyToArray.put( key, array );

		return array;
	}
}
