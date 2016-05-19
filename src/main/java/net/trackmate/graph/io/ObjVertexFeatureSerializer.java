package net.trackmate.graph.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.io.RawFeatureIO.Serializer;
import net.trackmate.graph.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.graph.io.RawGraphIO.GraphToFileIdMap;

public class ObjVertexFeatureSerializer< V extends Vertex< ? >, O > implements Serializer< Map< V, O >, V >
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
