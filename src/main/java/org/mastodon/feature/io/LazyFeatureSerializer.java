/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
