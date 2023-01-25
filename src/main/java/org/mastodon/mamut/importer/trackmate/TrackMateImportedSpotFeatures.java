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
package org.mastodon.mamut.importer.trackmate;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class TrackMateImportedSpotFeatures extends TrackMateImportedFeatures< Spot >
{

	public static final String KEY = "TrackMate Spot features";

	private static final String HELP_STRING =
			"Stores the spot feature values imported from a TrackMate or MaMuT file.";

	private final Spec spec = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< TrackMateImportedSpotFeatures, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					TrackMateImportedSpotFeatures.class,
					Spot.class,
					Multiplicity.SINGLE );
		}
	}

	@Override
	void store( final String key, final Dimension dimension, final String units,
			final DoublePropertyMap< Spot > values )
	{
		super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
	}

	@Override
	void store( final String key, final Dimension dimension, final String units, final IntPropertyMap< Spot > values )
	{
		super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return spec;
	}
}
