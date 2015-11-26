package net.trackmate.revised.trackscheme.util;

import java.util.Comparator;

import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class TrackSchemeUtil
{

	private static final Comparator< TrackSchemeVertex > LABEL_COMPARATOR = new Comparator< TrackSchemeVertex >()
	{
		AlphanumComparator alphaNumComparator = AlphanumComparator.instance;

		@Override
		public int compare( final TrackSchemeVertex o1, final TrackSchemeVertex o2 )
		{
			return alphaNumComparator.compare( o1.getLabel(), o2.getLabel() );
		}
	};

	public static final Comparator< TrackSchemeVertex > labelComparator()
	{
		return LABEL_COMPARATOR;
	}

	private TrackSchemeUtil()
	{}

}
