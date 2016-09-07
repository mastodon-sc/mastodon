package org.mastodon.graph.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.mastodon.graph.features.Feature;
import org.mastodon.graph.features.FeatureRegistry;
import org.mastodon.graph.features.Features;

public class RawFeatureIO
{
	/**
	 * De/serialize a feature map of type {@code M}.
	 *
	 * TODO Create its counterpart for edge features.
	 *
	 * @param <M>
	 *            the feature map type
	 * @param <O>
	 *            the vertex type
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static interface Serializer< M, O >
	{
		public void writeFeatureMap(
				final ObjectToFileIdMap< O > idmap,
				final M featureMap,
				final ObjectOutputStream oos )
						throws IOException;

		public void readFeatureMap(
				final FileIdToObjectMap< O > idmap,
				final M featureMap,
				final ObjectInputStream ois )
						throws IOException, ClassNotFoundException;
	}

	/**
	 * TODO javadoc
	 *
	 * @param idmap
	 * @param features
	 * @param featuresToSerialize
	 * @param oos
	 * @throws IOException
	 */
	public static < O > void writeFeatureMaps(
			final ObjectToFileIdMap< O > idmap,
			final Features< O > features,
			final List< Feature< ?, O, ? > > featuresToSerialize,
			final ObjectOutputStream oos )
					throws IOException
	{
		final String[] keys = new String[ featuresToSerialize.size() ];
		int i = 0;
		for ( final Feature< ?, O, ? > feature : featuresToSerialize )
			keys[ i++ ] = feature.getKey();
		oos.writeObject( keys );

		for ( final Feature< ?, O, ? > feature : featuresToSerialize )
			serializeFeatureMap( idmap, feature, features.getFeatureMap( feature ), oos );
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V > void serializeFeatureMap(
			final ObjectToFileIdMap< V > idmap,
			final Feature< M, V, ? > feature,
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
	 * TODO javadoc
	 *
	 * @param idmap
	 * @param features
	 * @param ois
	 * @throws IOException
	 */
	public static < O > void readFeatureMaps(
			final FileIdToObjectMap< O > idmap,
			final Features< O > features,
			final ObjectInputStream ois )
					throws IOException
	{
		try
		{
			final String[] keys = ( String[] ) ois.readObject();
			for ( final String key : keys )
			{
				@SuppressWarnings( "unchecked" )
				final Feature< ?, O, ? > feature = ( Feature< ?, O, ? > ) FeatureRegistry.getFeature( key );
				deserializeFeatureMap( idmap, feature, features.getFeatureMap( feature ), ois );
			}
		}
		catch ( final ClassNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V > void deserializeFeatureMap(
			final FileIdToObjectMap< V > idmap,
			final Feature< M, V, ? > feature,
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
