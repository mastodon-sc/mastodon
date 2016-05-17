package net.trackmate.graph;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.collection.RefSet;

import java.util.Comparator;

public class TestUtils
{
	public static final Comparator< TestVertex > idComparator = new Comparator< TestVertex >()
	{
		@Override
		public int compare( final TestVertex o1, final TestVertex o2 )
		{
			return o1.getId() - o2.getId();
		}
	};

	public static final String toString( final TIntObjectHashMap< RefSet< ? >> map )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "{\n" );
		for ( final int key : map.keys() )
		{
			sb.append( "  " + key + " -> " + map.get( key ) + "\n" );
		}
		sb.append( "}" );
		return sb.toString();
	}

	private TestUtils()
	{}
}
