package org.mastodon.feature.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.io.ObjectToFileIdMap;

/**
 * Used to serialize features that are computed on the fly and therefore do not
 * have a property map that can be serialized.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class LazyFeatureSerializer
{

	/**
	 * Serialize a feature via its collection of projections, over a specified
	 * collection of objects.
	 * <p>
	 * Serialization is the result of the concatenation of:
	 * <ul>
	 * <li><i>No</i> the number of objects serialized (<code>int</code>).
	 * <li><i>Np</i> the number of projections serialized (<code>int</code>).
	 * There will be <i>No x Np</i> values to read.
	 * <li>a list of <i>Np</i> blocks, one per projection, made of:
	 * <ul>
	 * <li>the projection name (<code>UTF string</code>).
	 * <li>the projection dimension (<code>UTF string</code>).
	 * <li>the projection units (<code>UTF string</code>).
	 * <li>a block of object id x projection value, alternating <i>No</i> times:
	 * <ul>
	 * <li>the object file id (<code>int</code>).
	 * <li>the projection value for this object (<code>double</code>).
	 * </ul>
	 * </ul>
	 * </ul>
	 * 
	 * 
	 * @param <O>
	 *            the type of objects to serialize.
	 * @param feature
	 *            the feature to serialize.
	 * @param objs
	 *            the collection of objects to serialize.
	 * @param idmap
	 *            the map linking object to their file if.
	 * @param oos
	 *            an object output stream to write to.
	 * @throws IOException
	 *             if problems arise while writing the file.
	 */
	public static < O > void serialize(
			final Feature< O > feature,
			final Collection< O > objs,
			final ObjectToFileIdMap< O > idmap,
			final ObjectOutputStream oos ) throws IOException
	{
		// NUMBER OF ENTRIES
		oos.writeInt( objs.size() );

		final Set< FeatureProjection< O > > projs = feature.projections();

		// NUMBER OF PROJECTIONS.
		oos.writeInt( projs.size() );

		// PER PROJ
		for ( final FeatureProjection< O > proj : projs )
		{
			final FeatureProjectionKey key = proj.getKey();

			// PROJECTION NAME.
			oos.writeUTF( key.toString() );

			// PROJECTION DIMENSION.
			oos.writeUTF( key.getSpec().projectionDimension.name() );

			// UNITS.
			oos.writeUTF( proj.units() );

			// ENTRIES.
			try
			{
				objs.forEach( o -> {
					try
					{
						oos.writeInt( idmap.getId( o ) );
						oos.writeDouble( proj.value( o ) );
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
}
