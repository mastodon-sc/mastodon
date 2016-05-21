package net.trackmate.graph.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import net.trackmate.graph.FeatureRegistry;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.graph.io.RawGraphIO.GraphToFileIdMap;

public class RawFeatureIO
{
	/**
	 * De/serialize a feature map of type {@code M}.
	 *
	 * TODO Create its counterpart for edge features.
	 *
	 * @param <M>
	 *            the feature map type
	 * @param <V>
	 *            the vertex type
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static interface Serializer< M, V extends Vertex< ? > >
	{
		public void writeFeatureMap(
				final GraphToFileIdMap< V, ? > idmap,
				final M featureMap,
				final ObjectOutputStream oos )
						throws IOException;

		public void readFeatureMap(
				final FileIdToGraphMap< V, ? > idmap,
				final M featureMap,
				final ObjectInputStream ois )
						throws IOException, ClassNotFoundException;
	}

	/**
	 * TODO How to serialize edge features, knowing that they are mapped by key
	 * strings as well?
	 * 
	 * @param idmap
	 * @param features
	 * @param featuresToSerialize
	 * @param oos
	 * @throws IOException
	 */
	public static < V extends Vertex< ? > > void writeFeatureMaps(
			final GraphToFileIdMap< V, ? > idmap,
			final GraphFeatures< V, ? > features,
			final List< VertexFeature< ?, V, ? > > featuresToSerialize,
			final ObjectOutputStream oos )
					throws IOException
	{
		final String[] keys = new String[ featuresToSerialize.size() ];
		int i = 0;
		for ( final VertexFeature< ?, V, ? > feature : featuresToSerialize )
			keys[ i++ ] = feature.getKey();
		oos.writeObject( keys );

		for ( final VertexFeature< ?, V, ? > feature : featuresToSerialize )
			serializeFeatureMap( idmap, feature, features.getVertexFeature( feature ), oos );
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V extends Vertex< ? > > void serializeFeatureMap(
			final GraphToFileIdMap< V, ? > idmap,
			final VertexFeature< M, V, ? > feature,
			final Object featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final Serializer< M, V > serializer = FeatureSerializers.get( feature );
		if ( serializer == null )
			throw new IOException( "No Serializer registered for " + feature );
		serializer.writeFeatureMap( idmap, ( M ) featureMap, oos );
	}

	/**
	 * TODO how to deserialize edge features?
	 *
	 * @param idmap
	 * @param features
	 * @param ois
	 * @throws IOException
	 */
	public static < V extends Vertex< ? > > void readFeatureMaps(
			final FileIdToGraphMap< V, ? > idmap,
			final GraphFeatures< V, ? > features,
			final ObjectInputStream ois )
					throws IOException
	{
		try
		{
			final String[] keys = ( String[] ) ois.readObject();
			for ( final String key : keys )
			{
				@SuppressWarnings( "unchecked" )
				final VertexFeature< ?, V, ? > feature = ( VertexFeature< ?, V, ? > ) FeatureRegistry.getVertexFeature( key );
				deserializeFeatureMap( idmap, feature, features.getVertexFeature( feature ), ois );
			}
		}
		catch ( final ClassNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V extends Vertex< ? > > void deserializeFeatureMap(
			final FileIdToGraphMap< V, ? > idmap,
			final VertexFeature< M, V, ? > feature,
			final Object featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		final Serializer< M, V > serializer = FeatureSerializers.get( feature );
		if ( serializer == null )
			throw new IOException( "No Serializer registered for " + feature );
		serializer.readFeatureMap( idmap, ( M ) featureMap, ois );
	}
}
