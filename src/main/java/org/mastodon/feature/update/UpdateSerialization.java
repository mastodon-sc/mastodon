package org.mastodon.feature.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;

class UpdateSerialization
{

	static < O > Update< O > deserialize(
			final RefCollection< O > pool,
			final FileIdToObjectMap< O > idmap,
			final ObjectInputStream ois )
			throws IOException, ClassNotFoundException
	{
		final RefSet< O > modified = RefCollections.createRefSet( pool );
		deserialize( modified, idmap, ois );
		final RefSet< O > neighbors = RefCollections.createRefSet( pool );
		deserialize( neighbors, idmap, ois );
		return new Update<>( modified, neighbors );
	}

	private static < O > void deserialize(
			final RefSet< O > collection,
			final FileIdToObjectMap< O > idmap,
			final ObjectInputStream ois )
			throws IOException
	{
		// NUMBER OF ENTRIES
		final int size = ois.readInt();

		// ENTRIES
		final O ref = idmap.createRef();
		for ( int i = 0; i < size; i++ )
		{
			final int key = ois.readInt();
			collection.add( idmap.getObject( key, ref ) );
		}
		idmap.releaseRef( ref );
	}

	static < O > void serialize(
			final Update< O > update,
			final ObjectToFileIdMap< O > idmap,
			final ObjectOutputStream oos )
			throws IOException
	{
		serialize( update.get(), idmap, oos );
		serialize( update.getNeighbors(), idmap, oos );
	}

	private static < O > void serialize(
			final RefSet< O > collection,
			final ObjectToFileIdMap< O > idmap,
			final ObjectOutputStream oos )
			throws IOException
	{
		// NUMBER OF ENTRIES
		oos.writeInt( collection.size() );

		// ENTRIES
		try
		{
			collection.forEach( ( final O key ) -> {
				try
				{
					oos.writeInt( idmap.getId( key ) );
				}
				catch ( final IOException e )
				{
					throw new UncheckedIOException( e );
				}
			} );
		}
		catch ( final UncheckedIOException e )
		{
			throw e.getCause();
		}
	}
}
