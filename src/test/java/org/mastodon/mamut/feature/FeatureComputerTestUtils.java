/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import net.imglib2.util.Cast;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

public class FeatureComputerTestUtils
{

	public static < T > Feature< T > getFeature( Context context, Model model, FeatureSpec< ? extends Feature< T >, T > spec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, model );
		return Cast.unchecked( featureComputerService.compute( true, spec ).get( spec ) );
	}

	public static < T > FeatureProjection< T > getFeatureProjection( Context context, Model model,
			FeatureSpec< ? extends Feature< T >, T > spec, FeatureProjectionSpec featureProjectionSpec )
	{
		Feature< T > feature = getFeature( context, model, spec );
		return feature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	private static MamutFeatureComputerService getMamutFeatureComputerService( Context context, Model model )
	{
		final MamutFeatureComputerService featureComputerService = MamutFeatureComputerService.newInstance( context );
		featureComputerService.setModel( model );
		return featureComputerService;
	}
}
