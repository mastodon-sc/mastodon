package net.trackmate.graph.io;

import java.util.HashMap;

import net.trackmate.graph.Vertex;
import net.trackmate.graph.features.unify.Feature;
import net.trackmate.graph.io.RawFeatureIO.Serializer;

public class FeatureSerializers
{
	private static final HashMap< Feature< ?, ?, ? >, Serializer< ?, ? > > vertexSerializers = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public static < M, V extends Vertex< ? > > Serializer< M, V > get( final Feature< M, V, ? > feature )
	{
		final Serializer< M, V > serializer = ( Serializer< M, V > ) vertexSerializers.get( feature );
		return serializer;
	}

	public static < M, V extends Vertex< ? > > void put( final Feature< M, V, ? > feature, final Serializer< M, V > serializer )
	{
		vertexSerializers.put( feature, serializer );
	}
}
