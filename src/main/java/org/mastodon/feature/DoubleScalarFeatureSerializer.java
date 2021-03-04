/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.properties.DoublePropertyMap;

public abstract class DoubleScalarFeatureSerializer< F extends DoubleScalarFeature< O >, O > implements FeatureSerializer< F, O >
{

	@Override
	public void serialize( final F feature, final ObjectToFileIdMap< O > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final FeatureSpec< ? extends Feature< O >, O > spec = feature.getSpec();
		final FeatureProjectionSpec projectionSpec = spec.getProjectionSpecs().iterator().next();

		final String key = spec.getKey();
		final String info = spec.getInfo();
		final Dimension dimension = projectionSpec.projectionDimension;
		final String units = feature.projections().iterator().next().units();

		oos.writeUTF( key );
		oos.writeUTF( info );
		oos.writeObject( dimension );
		oos.writeUTF( units );

		final DoublePropertyMap< O > map = feature.values;
		final DoublePropertyMapSerializer< O > mapSerializer = new DoublePropertyMapSerializer<>( map );
		mapSerializer.writePropertyMap( idmap, oos );
	}

	protected DeserializedStruct read( final FileIdToObjectMap< O > idmap, final RefCollection< O > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final String key = ois.readUTF();
		final String info = ois.readUTF();
		final Dimension dimension = ( Dimension ) ois.readObject();
		final String units = ois.readUTF();

		final DoublePropertyMap< O > map = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMapSerializer< O > mapSerializer = new DoublePropertyMapSerializer<>( map );
		mapSerializer.readPropertyMap( idmap, ois );

		return new DeserializedStruct( key, info, dimension, units, map );
	}

	protected class DeserializedStruct
	{

		public final String key;

		public final String info;

		public final Dimension dimension;

		public final String units;

		public final DoublePropertyMap< O > map;

		private DeserializedStruct( final String key, final String info, final Dimension dimension, final String units, final DoublePropertyMap< O > map )
		{
			this.key = key;
			this.info = info;
			this.dimension = dimension;
			this.units = units;
			this.map = map;
		}
	}
}
