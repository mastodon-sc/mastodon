package net.trackmate.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.trackmate.graph.Vertex;
import net.trackmate.io.RawFeatureIO.Serializer;
import net.trackmate.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.io.RawGraphIO.GraphToFileIdMap;

public class IntVertexFeatureSerializer< V extends Vertex< ? > > implements Serializer< TObjectIntMap< V >, V >
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
