package org.mastodon.revised.model.mamut;

import org.mastodon.features.ObjFeature;
import org.mastodon.io.features.FeatureSerializers;
import org.mastodon.io.features.StringFeatureSerializer;

public class ModelFeatures
{
	static final int NO_ENTRY_VALUE = -1;

	public static final ObjFeature< Spot, String > LABEL = new ObjFeature<>( "label" );
//	public static final IntFeature< Spot > TRACKLENGTH = new IntFeature<>( "track length", NO_ENTRY_VALUE );

	static {
		FeatureSerializers.put( LABEL, new StringFeatureSerializer<>() );
//		FeatureSerializers.put( TRACKLENGTH, new IntFeatureSerializer<>() );
	}

	private ModelFeatures() {};
}
