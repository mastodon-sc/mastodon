package org.mastodon.views.bvv;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import org.mastodon.views.bvv.scene.HasModCount;

class ReusableInstanceArrays< A extends HasModCount >
{
	private final IntUnaryOperator modCount;

	private final ArrayList< ReusableInstanceArray< A > > arrays;

	private final TIntObjectHashMap< ReusableInstanceArray< A > > timepointToArray;

	private int lrutimestamp = 1;

	public ReusableInstanceArrays( final IntUnaryOperator modCount, final int size, final Supplier< A > createInstanceArray )
	{
		this.modCount = modCount;
		arrays = new ArrayList<>( size );
		timepointToArray = new TIntObjectHashMap<>( size * 2, Constants.DEFAULT_LOAD_FACTOR, -1 );
		for ( int i = 0; i < size; ++i )
			arrays.add( new ReusableInstanceArray<>( createInstanceArray.get() ) );
	}

	private static class ReusableInstanceArray< A >
	{
		private final A instanceArray;
		private int timepoint = -1;
		private int lru = 0;

		public A getInstanceArray()
		{
			return instanceArray;
		}

		public ReusableInstanceArray( final A instanceArray )
		{
			this.instanceArray = instanceArray;
		}
	}

	public A getForTimepoint( final int timepoint )
	{
		ReusableInstanceArray< A > array = timepointToArray.get( timepoint );
		if ( array != null )
		{
			array.lru = lrutimestamp++;
			return array.instanceArray;
		}

		for ( ReusableInstanceArray< A > a : arrays )
		{
			if ( a.timepoint == -1 || a.instanceArray.getModCount() != modCount.applyAsInt( a.timepoint ) )
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

		timepointToArray.remove( array.timepoint );
		timepointToArray.put( timepoint, array );
		array.lru = lrutimestamp++;
		array.timepoint = timepoint;
		array.instanceArray.setModCount( -1 );
		return array.instanceArray;
	}
}
