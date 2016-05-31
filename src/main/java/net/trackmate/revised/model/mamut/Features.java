package net.trackmate.revised.model.mamut;

import net.trackmate.graph.features.ObjFeature;
import net.trackmate.graph.io.FeatureSerializers;
import net.trackmate.graph.io.StringFeatureSerializer;

public class Features
{
	static final int NO_ENTRY_VALUE = -1;

	public static final ObjFeature< Spot, String > LABEL = new ObjFeature<>( "label" );
//	public static final IntFeature< Spot > TRACKLENGTH = new IntFeature<>( "track length", NO_ENTRY_VALUE );

	static {
		FeatureSerializers.put( LABEL, new StringFeatureSerializer<>() );
//		FeatureSerializers.put( TRACKLENGTH, new IntFeatureSerializer<>() );
	}

	private Features() {};
}
