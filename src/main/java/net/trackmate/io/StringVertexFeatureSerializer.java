package net.trackmate.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import net.trackmate.graph.zzgraphinterfaces.Vertex;
import net.trackmate.io.RawFeatureIO.Serializer;
import net.trackmate.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.io.RawGraphIO.GraphToFileIdMap;

public class StringVertexFeatureSerializer< V extends Vertex< ? > > implements Serializer< Map< V, String >, V >
{
	@Override
	public void writeFeatureMap(
			final GraphToFileIdMap< V, ? > idmap,
			final Map< V, String > featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final int numFeatures = featureMap.size();
		final int[] ids = new int[ numFeatures ];
		final ByteArrayOutputStream bs = new ByteArrayOutputStream();
		final DataOutputStream ds = new DataOutputStream( bs );

		int i = 0;
		for ( final Entry< V, String > e : featureMap.entrySet() )
		{
			ids[ i ] = idmap.getVertexId( e.getKey() );
			ds.writeUTF( e.getValue() );
			++i;
		}

		oos.writeObject( ids );
		oos.writeObject( bs.toByteArray() );
	}

	@Override
	public void readFeatureMap(
			final FileIdToGraphMap< V, ? > idmap,
			final Map< V, String > featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		final int[] ids = ( int[] ) ois.readObject();
		final byte[] bsbytes = ( byte[] ) ois.readObject();
		final DataInputStream ds = new DataInputStream( new ByteArrayInputStream( bsbytes ) );

		featureMap.clear();
		final V ref = idmap.vertexRef();
		for ( final int id : ids )
			featureMap.put( idmap.getVertex( id, ref ), ds.readUTF() );
		idmap.releaseRef( ref );
	}
}
