package net.trackmate.io;

import java.util.HashMap;

import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.io.RawFeatureIO.Serializer;

public class FeatureSerializers
{
	private static final HashMap< VertexFeature< ?, ?, ? >, Serializer< ?, ? > > vertexSerializers = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public static < M, V extends Vertex< ? > > Serializer< M, V > get( final VertexFeature< M, V, ? > feature )
	{
		final Serializer< M, V > serializer = ( Serializer< M, V > ) vertexSerializers.get( feature );
		return serializer;
	}

	public static < M, V extends Vertex< ? > > void put( final VertexFeature< M, V, ? > feature, final Serializer< M, V > serializer )
	{
		vertexSerializers.put( feature, serializer );
	}
}
