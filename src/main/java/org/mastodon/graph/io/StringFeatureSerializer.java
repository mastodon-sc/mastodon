package org.mastodon.graph.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.mastodon.graph.io.RawFeatureIO.Serializer;
import org.mastodon.graph.io.RawGraphIO.FileIdToObjectMap;
import org.mastodon.graph.io.RawGraphIO.ObjectToFileIdMap;

public class StringFeatureSerializer< O > implements Serializer< Map< O, String >, O >
{
	@Override
	public void writeFeatureMap(
			final ObjectToFileIdMap< O > idmap,
			final Map< O, String > featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final int numFeatures = featureMap.size();
		final int[] ids = new int[ numFeatures ];
		final ByteArrayOutputStream bs = new ByteArrayOutputStream();
		final DataOutputStream ds = new DataOutputStream( bs );

		int i = 0;
		for ( final Entry< O, String > e : featureMap.entrySet() )
		{
			ids[ i ] = idmap.getId( e.getKey() );
			ds.writeUTF( e.getValue() );
			++i;
		}

		oos.writeObject( ids );
		oos.writeObject( bs.toByteArray() );
	}

	@Override
	public void readFeatureMap(
			final FileIdToObjectMap< O > idmap,
			final Map< O, String > featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		final int[] ids = ( int[] ) ois.readObject();
		final byte[] bsbytes = ( byte[] ) ois.readObject();
		final DataInputStream ds = new DataInputStream( new ByteArrayInputStream( bsbytes ) );

		featureMap.clear();
		final O ref = idmap.createRef();
		for ( final int id : ids )
			featureMap.put( idmap.getObject( id, ref ), ds.readUTF() );
		idmap.releaseRef( ref );
	}
}
