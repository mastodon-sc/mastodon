package org.mastodon.graph.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.mastodon.graph.io.RawFeatureIO.Serializer;
import org.mastodon.graph.io.RawGraphIO.FileIdToObjectMap;
import org.mastodon.graph.io.RawGraphIO.ObjectToFileIdMap;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ObjFeatureSerializer< O, T > implements Serializer< Map< O, T >, O >
{
	@Override
	public void writeFeatureMap(
			final ObjectToFileIdMap< O > idmap,
			final Map< O, T > featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final TIntObjectHashMap< T > fmap = new TIntObjectHashMap< >();
		for ( final Entry< O, T > e : featureMap.entrySet() )
			fmap.put( idmap.getId( e.getKey() ), e.getValue() );
		oos.writeObject( fmap );
	}

	@Override
	public void readFeatureMap(
			final FileIdToObjectMap< O > idmap,
			final Map< O, T > featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		@SuppressWarnings( "unchecked" )
		final TIntObjectHashMap< T > fmap = ( TIntObjectHashMap< T > ) ois.readObject();
		featureMap.clear();
		final O ref = idmap.createRef();
		fmap.forEachEntry( ( final int key, final T value ) -> {
			featureMap.put( idmap.getObject( key, ref ), value );
			return true;
		} );
		idmap.releaseRef( ref );
	}
}
