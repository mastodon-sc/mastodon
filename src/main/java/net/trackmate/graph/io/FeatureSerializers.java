package net.trackmate.graph.io;

import java.util.HashMap;

import net.trackmate.graph.features.Feature;
import net.trackmate.graph.io.RawFeatureIO.Serializer;

public class FeatureSerializers
{
	private static final HashMap< Feature< ?, ?, ? >, Serializer< ?, ? > > serializers = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public static < M, O > Serializer< M, O > get( final Feature< M, O, ? > feature )
	{
		final Serializer< M, O > serializer = ( Serializer< M, O > ) serializers.get( feature );
		return serializer;
	}

	public static < M, O > void put( final Feature< M, O, ? > feature, final Serializer< M, O > serializer )
	{
		serializers.put( feature, serializer );
	}
}
