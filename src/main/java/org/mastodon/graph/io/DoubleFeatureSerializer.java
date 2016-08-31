package org.mastodon.graph.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.graph.io.RawFeatureIO.Serializer;
import org.mastodon.graph.io.RawGraphIO.FileIdToObjectMap;
import org.mastodon.graph.io.RawGraphIO.ObjectToFileIdMap;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class DoubleFeatureSerializer< O > implements Serializer< TObjectDoubleMap< O >, O >
{
	@Override
	public void writeFeatureMap(
			final ObjectToFileIdMap< O > idmap,
			final TObjectDoubleMap< O > featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final TIntDoubleHashMap fmap = new TIntDoubleHashMap();
		featureMap.forEachEntry( ( final O key, final double value ) -> {
			fmap.put( idmap.getId( key ), value );
			return true;
		} );
		oos.writeObject( fmap );
	}

	@Override
	public void readFeatureMap(
			final FileIdToObjectMap< O > idmap,
			final TObjectDoubleMap< O > featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		final TIntDoubleHashMap fmap = ( TIntDoubleHashMap ) ois.readObject();
		featureMap.clear();
		final O ref = idmap.createRef();
		fmap.forEachEntry( ( final int key, final double value ) -> {
			featureMap.put( idmap.getObject( key, ref ), value );
			return true;
		} );
		idmap.releaseRef( ref );
	}
}
