package net.trackmate.revised.model.mamut;

import net.trackmate.graph.features.ObjVertexFeature;
import net.trackmate.io.FeatureSerializers;
import net.trackmate.io.StringVertexFeatureSerializer;

public class Features
{
	static final int NO_ENTRY_VALUE = -1;

	public static final ObjVertexFeature< Spot, String > LABEL = new ObjVertexFeature<>( "label" );
//	public static final IntVertexFeature< Spot > TRACKLENGTH = new IntVertexFeature<>( "track length", NO_ENTRY_VALUE );

	static {
		FeatureSerializers.put( LABEL, new StringVertexFeatureSerializer<>() );
//		FeatureSerializers.put( TRACKLENGTH, new IntVertexFeatureSerializer<>() );
	}

	private Features() {};
}
