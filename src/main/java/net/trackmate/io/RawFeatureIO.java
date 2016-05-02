package net.trackmate.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.graph.Vertex;
import net.trackmate.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.io.RawGraphIO.GraphToFileIdMap;
import net.trackmate.revised.model.FeatureRegistry;
import net.trackmate.revised.model.GraphFeatures;
import net.trackmate.revised.model.VertexFeature;

public class RawFeatureIO
{
	/**
	 * TODO
	 *
	 * @param <V>
	 * @param <E>
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

	public static class ObjVertexFeatureSerializer< V extends Vertex< ? >, O > implements Serializer< Map< V, O >, V >
	{
		@Override
		public void writeFeatureMap(
				final GraphToFileIdMap< V, ? > idmap,
				final Map< V, O > featureMap,
				final ObjectOutputStream oos )
						throws IOException
		{
			final TIntObjectHashMap< O > fmap = new TIntObjectHashMap< >();
			for ( final Entry< V, O > e : featureMap.entrySet() )
				fmap.put( idmap.getVertexId( e.getKey() ), e.getValue() );
			oos.writeObject( fmap );
		}

		@Override
		public void readFeatureMap(
				final FileIdToGraphMap< V, ? > idmap,
				final Map< V, O > featureMap,
				final ObjectInputStream ois )
						throws IOException, ClassNotFoundException
		{
			@SuppressWarnings( "unchecked" )
			final TIntObjectHashMap< O > fmap = ( TIntObjectHashMap< O > ) ois.readObject();
			featureMap.clear();
			final V ref = idmap.vertexRef();
			fmap.forEachEntry( ( final int key, final O value ) -> {
				featureMap.put( idmap.getVertex( key, ref ), value );
				return true;
			} );
			idmap.releaseRef( ref );
		}
	}

	public static class IntVertexFeatureSerializer< V extends Vertex< ? > > implements Serializer< TObjectIntMap< V >, V >
	{
		@Override
		public void writeFeatureMap(
				final GraphToFileIdMap< V, ? > idmap,
				final TObjectIntMap< V > featureMap,
				final ObjectOutputStream oos )
						throws IOException
		{
			final TIntIntHashMap fmap = new TIntIntHashMap();
			featureMap.forEachEntry( ( final V key, final int value ) -> {
				fmap.put( idmap.getVertexId( key ), value );
				return true;
			} );
			oos.writeObject( fmap );
		}

		@Override
		public void readFeatureMap(
				final FileIdToGraphMap< V, ? > idmap,
				final TObjectIntMap< V > featureMap,
				final ObjectInputStream ois )
						throws IOException, ClassNotFoundException
		{
			final TIntIntHashMap fmap = ( TIntIntHashMap ) ois.readObject();
			featureMap.clear();
			final V ref = idmap.vertexRef();
			fmap.forEachEntry( ( final int key, final int value ) -> {
				featureMap.put( idmap.getVertex( key, ref ), value );
				return true;
			} );
			idmap.releaseRef( ref );
		}
	}

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
