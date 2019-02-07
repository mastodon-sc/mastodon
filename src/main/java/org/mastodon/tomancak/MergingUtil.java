package org.mastodon.tomancak;

import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;

public class MergingUtil
{
	public static boolean hasLabel( final Spot spot )
	{
		final SpotPool pool = ( SpotPool ) spot.getModelGraph().vertices().getRefPool();
		ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
		return labels.isSet( spot );
	}

	public static String spotToString( final Spot spot )
	{
		return spotToString( spot, false );
	}

	public static String spotToString( final Spot spot, boolean onlyTrueLabels )
	{
		return String.format( "Spot( id=%3d, tp=%3d",
				spot.getInternalPoolIndex(),
				spot.getTimepoint() )
				+ ( !onlyTrueLabels || hasLabel( spot )
						? String.format( ", label='%s' )", spot.getLabel() )
						: " )" );
	}

}
