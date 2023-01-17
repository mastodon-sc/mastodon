/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for classes that can de/serialize a feature.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <F>
 *            the type of the feature to serialize.
 * @param <O>
 *            the class of the object the feature is defined for.
 */
public interface FeatureSerializer< F extends Feature< O >, O > extends SciJavaPlugin
{

	/**
	 * Returns the feature specifications of the feature this
	 * {@link FeatureSerializer} can de/serialize.
	 *
	 * @return the feature specifications of the feature this
	 *         {@link FeatureSerializer} can de/serialize.
	 */
	public FeatureSpec< F, O > getFeatureSpec();

	/**
	 * Serializes the feature to the specified output stream.
	 *
	 * @param feature
	 *            the feature to serialize.
	 * @param idmap
	 *            the {@link ObjectToFileIdMap}.
	 * @param oos
	 *            the output stream.
	 * @throws IOException
	 *             if an I/O error occurs while writing the feature file.
	 */
	public void serialize( F feature, ObjectToFileIdMap< O > idmap, ObjectOutputStream oos ) throws IOException;

	/**
	 * Deserializes a feature from the specified input stream. Returns
	 * <code>null</code> if the feature values are computed on the fly and do
	 * not need deseralization.
	 *
	 * @param idmap
	 *            the {@link FileIdToObjectMap}.
	 * @param pool
	 *            the {@link RefCollection} used to create property maps inside
	 *            the feature.
	 * @param ois
	 *            the input stream.
	 * @return a new feature instance or <code>null</code> if the feature values
	 *         are computed on the fly and do not need deseralization.
	 * @throws IOException
	 *             if an I/O error occurs while reading the feature file.
	 * @throws ClassNotFoundException
	 *             if the class of the feature or the class of its target cannot
	 *             be found.
	 */
	public default F deserialize( final FileIdToObjectMap< O > idmap, final RefCollection< O > pool,
			final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		return null;
	}

}
