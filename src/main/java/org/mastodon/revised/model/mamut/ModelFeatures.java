package org.mastodon.revised.model.mamut;

import org.mastodon.graph.features.ObjFeature;
import org.mastodon.graph.io.FeatureSerializers;
import org.mastodon.graph.io.StringFeatureSerializer;

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
