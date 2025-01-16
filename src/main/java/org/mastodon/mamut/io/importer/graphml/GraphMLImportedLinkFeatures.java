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
package org.mastodon.mamut.io.importer.graphml;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedFeatures;
import org.mastodon.mamut.model.Link;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class GraphMLImportedLinkFeatures extends TrackMateImportedFeatures< Link >
{

	public static final String KEY = "GraphML Link features";

	private static final String HELP_STRING =
			"Stores the link feature values imported from a GraphML file.";

	private final Spec spec = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< GraphMLImportedLinkFeatures, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					GraphMLImportedLinkFeatures.class,
					Link.class,
					Multiplicity.SINGLE );
		}
	}

	@Override
	public FeatureProjectionKey store( final String key, final Dimension dimension, final String units, final DoublePropertyMap< Link > values )
	{
		final FeatureProjectionKey projectionKey = super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
		return projectionKey;
	}

	@Override
	public FeatureProjectionKey store( final String key, final Dimension dimension, final String units, final IntPropertyMap< Link > values )
	{
		final FeatureProjectionKey projectionKey = super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
		return projectionKey;
	}

	@Override
	public FeatureSpec< ? extends Feature< Link >, Link > getSpec()
	{
		return spec;
	}
}
