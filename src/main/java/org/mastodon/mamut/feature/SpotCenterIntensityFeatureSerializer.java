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
package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.SpotCenterIntensityFeature.Spec;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class SpotCenterIntensityFeatureSerializer implements FeatureSerializer< SpotCenterIntensityFeature, Spot >
{

	@Override
	public Spec getFeatureSpec()
	{
		return SpotCenterIntensityFeature.SPEC;
	}

	@Override
	public void serialize( final SpotCenterIntensityFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final int nSources = feature.maps.size();
		oos.writeInt( nSources );
		for ( int i = 0; i < nSources; i++ )
			new DoublePropertyMapSerializer<>( feature.maps.get( i ) ).writePropertyMap( idmap, oos );
	}

	@Override
	public SpotCenterIntensityFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final int nSources = ois.readInt();
		final List< DoublePropertyMap< Spot > > maps = new ArrayList<>( nSources );
		for ( int i = 0; i < nSources; i++ )
		{
			final DoublePropertyMap< Spot > meanMap = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( meanMap ).readPropertyMap( idmap, ois );
			maps.add( meanMap );
		}
		return new SpotCenterIntensityFeature( maps );
	}
}
