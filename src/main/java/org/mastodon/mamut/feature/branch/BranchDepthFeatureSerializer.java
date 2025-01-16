/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Plugin( type = FeatureSerializer.class )
public class BranchDepthFeatureSerializer implements BranchFeatureSerializer< BranchDepthFeature, BranchSpot, Spot >
{

	@Override
	public BranchDepthFeature.Spec getFeatureSpec()
	{
		return BranchDepthFeature.SPEC;
	}

	@Override
	public BranchDepthFeature deserialize( final FileIdToObjectMap< Spot > idmap, final ObjectInputStream ois,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws ClassNotFoundException, IOException
	{
		// Read the map link -> val.
		final IntPropertyMap< Spot > linkMap = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( linkMap );
		propertyMapSerializer.readPropertyMap( idmap, ois );

		// Map to branch-link -> val.
		return new BranchDepthFeature( BranchFeatureSerializer.mapToBranchSpotMap( linkMap, branchGraph ) );
	}

	@Override
	public void serialize( final BranchDepthFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws IOException
	{
		final IntPropertyMap< Spot > linkMap = BranchFeatureSerializer.branchSpotMapToMap( feature.map, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( linkMap );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}

}
