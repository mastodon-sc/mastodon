package net.trackmate.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.io.RawGraphIO.GraphToFileIdMap;
import net.trackmate.revised.model.FeatureRegistry;
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
		public Object serializeFeatureMap(
				final GraphToFileIdMap< V, ? > idmap,
				final M featureMap );

		public M deserializeFeatureMap(
				final FileIdToGraphMap< V, ? > idmap,
				final Object serialized,
				final M featureMap );
	}

	public static class ObjVertexFeatureSerializer< V extends Vertex< ? >, O > implements Serializer< Map< V, O >, V >
	{
		@Override
		public Object serializeFeatureMap( final GraphToFileIdMap< V, ? > idmap, final Map< V, O > featureMap )
		{
			final TIntObjectHashMap< O > fmap = new TIntObjectHashMap< >();
			for ( final Entry< V, O > e : featureMap.entrySet() )
				fmap.put( idmap.getVertexId( e.getKey() ), e.getValue() );
			return fmap;
		}

		@Override
		public Map< V, O > deserializeFeatureMap( final FileIdToGraphMap< V, ? > idmap, final Object serialized, final Map< V, O > featureMap )
		{
			@SuppressWarnings( "unchecked" )
			final TIntObjectHashMap< O > fmap = ( TIntObjectHashMap< O > ) serialized;
			featureMap.clear();
			final V ref = idmap.vertexRef();
			fmap.forEachEntry( ( final int key, final O value ) -> {
				featureMap.put( idmap.getVertex( key, ref ), value );
				return true;
			} );
			idmap.releaseRef( ref );
			return featureMap;
		}
	}

	public static class IntVertexFeatureSerializer< V extends Vertex< ? > > implements Serializer< TObjectIntMap< V >, V >
	{
		@Override
		public Object serializeFeatureMap( final GraphToFileIdMap< V, ? > idmap, final TObjectIntMap< V > featureMap )
		{
			final TIntIntHashMap fmap = new TIntIntHashMap();
			featureMap.forEachEntry( ( final V key, final int value ) -> {
				fmap.put( idmap.getVertexId( key ), value );
				return true;
			} );
			return fmap;
		}

		@Override
		public TObjectIntMap< V > deserializeFeatureMap( final FileIdToGraphMap< V, ? > idmap, final Object serialized, final TObjectIntMap< V > featureMap )
		{
			final TIntIntHashMap fmap = ( TIntIntHashMap ) serialized;
			featureMap.clear();
			final V ref = idmap.vertexRef();
			fmap.forEachEntry( ( final int key, final int value ) -> {
				featureMap.put( idmap.getVertex( key, ref ), value );
				return true;
			} );
			idmap.releaseRef( ref );
			return featureMap;
		}
	}

	public static class FeatureSerializers< V extends Vertex< E >, E extends Edge< V > >
	{
		public final HashMap< VertexFeature< ?, V, ? >, Serializer< ?, V > > vertexMapSerializers = new HashMap< >();
	}

	public static class SFM implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public final Object serializedFeatureMap;

		public final String key;

		public SFM( final String key, final Object serializedFeatureMap )
		{
			this.key = key;
			this.serializedFeatureMap = serializedFeatureMap;
		}
	}

	public static < V extends Vertex< ? > > void writeFeatureMaps(
			final GraphToFileIdMap< V, ? > idmap,
			final Map< VertexFeature< ?, V, ? >, Object > vertexFeatureMaps,
			final FeatureSerializers< V, ? > registry,
			final ObjectOutputStream oos )
					throws IOException
	{
		final ArrayList< VertexFeature< ?, V, ? > > features = new ArrayList<>( vertexFeatureMaps.keySet() );

		final String[] keys = new String[ features.size() ];
		int i = 0;
		for ( final VertexFeature< ?, V, ? > feature : features )
			keys[ i++ ] = feature.getKey();
		oos.writeObject( keys );

		for ( final VertexFeature< ?, V, ? > feature : features )
		{
			final Object fmap = serializeFeatureMap( idmap, feature, vertexFeatureMaps.get( feature ), registry );
			oos.writeObject( new SFM( feature.getKey(), fmap ) );
		}
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V extends Vertex< ? > > Object serializeFeatureMap(
			final GraphToFileIdMap< V, ? > idmap,
			final VertexFeature< M, V, ? > feature,
			final Object featureMap,
			final FeatureSerializers< V, ? > registry )
					throws IOException
	{
		final Serializer< M, V > serializer = ( Serializer< M, V > ) registry.vertexMapSerializers.get( feature );
		if ( serializer == null )
			throw new IOException( "No Serializer registered for " + feature );
		return serializer.serializeFeatureMap( idmap, ( M ) featureMap );
	}

	public static < V extends Vertex< ? > > void readFeatureMaps(
			final FileIdToGraphMap< V, ? > idmap,
			final Map< VertexFeature< ?, V, ? >, Object > vertexFeatureMaps,
			final FeatureSerializers< V, ? > registry,
			final ObjectInputStream ois )
					throws IOException
	{
		try
		{
			final String[] keys = ( String[] ) ois.readObject();
			final int numFeatures = keys.length;
			for ( int i = 0; i < numFeatures; ++i )
			{
				final SFM sfm = ( SFM ) ois.readObject();
				@SuppressWarnings( "unchecked" )
				final VertexFeature< ?, V, ? > feature = ( VertexFeature< ?, V, ? > ) FeatureRegistry.getVertexFeature( sfm.key );
				deserializeFeatureMap( idmap, feature, sfm.serializedFeatureMap, vertexFeatureMaps.get( feature ), registry );
			}
		}
		catch ( final ClassNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	@SuppressWarnings( "unchecked" )
	private static < M, V extends Vertex< ? > > M deserializeFeatureMap(
			final FileIdToGraphMap< V, ? > idmap,
			final VertexFeature< M, V, ? > feature,
			final Object serializedFeatureMap,
			final Object featureMap,
			final FeatureSerializers< V, ? > registry )
					throws IOException
	{
		final Serializer< M, V > serializer = ( Serializer< M, V > ) registry.vertexMapSerializers.get( feature );
		if ( serializer == null )
			throw new IOException( "No Serializer registered for " + feature );
		return serializer.deserializeFeatureMap( idmap, serializedFeatureMap, ( M ) featureMap );
	}

}
