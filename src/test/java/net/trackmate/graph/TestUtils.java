package net.trackmate.graph;

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

	private TestUtils()
	{}
}
