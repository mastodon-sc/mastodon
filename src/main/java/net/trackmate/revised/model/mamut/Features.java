package net.trackmate.revised.model.mamut;

import net.trackmate.graph.features.ObjFeature;
import net.trackmate.graph.io.FeatureSerializers;
import net.trackmate.graph.io.StringVertexFeatureSerializer;

public class Features
{
	static final int NO_ENTRY_VALUE = -1;

	public static final ObjFeature< Spot, String > LABEL = new ObjFeature<>( "label" );
//	public static final IntVertexFeature< Spot > TRACKLENGTH = new IntVertexFeature<>( "track length", NO_ENTRY_VALUE );

	static {
		FeatureSerializers.put( LABEL, new StringVertexFeatureSerializer<>() );
//		FeatureSerializers.put( TRACKLENGTH, new IntVertexFeatureSerializer<>() );
	}

	private Features() {};
}
