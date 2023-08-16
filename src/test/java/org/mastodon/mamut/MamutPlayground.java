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
package org.mastodon.mamut;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotCenterIntensityFeature;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MamutPlayground
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final Context context = new Context();
		final MamutProject project = MamutProjectIO.load( "samples/mamutproject.mastodon" );
		final MamutAppModel appModel = ProjectLoader.open( project, context );
		final Model model = appModel.getModel();

		System.out.println( "\n\n\n___________________________________\nData loaded.\n" );

		final MamutFeatureComputerService featureComputerService =
				MamutFeatureComputerService.newInstance( context );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( appModel.getSharedBdvData() );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features =
				featureComputerService.compute( SpotCenterIntensityFeature.SPEC );

		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );
		printForTarget( Spot.class, model.getGraph().vertices(), specsService, features );
		printForTarget( Link.class, model.getGraph().edges(), specsService, features );
	}

	private static < T > void printForTarget( final Class< T > target, final Collection< T > collection,
			final FeatureSpecsService specsService, final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{
		System.out.println( "\n\nFeatures that have " + target.getSimpleName() + " as target:" );
		final List< FeatureSpec< ?, T > > specs = specsService.getSpecs( target );
		for ( final FeatureSpec< ?, T > spec : specs )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< T > feature = ( Feature< T > ) featureModel.get( spec );
			if ( null == feature )
			{
				System.out.println( "\n - Feature " + spec.getKey() + " is not computed." );
				continue;
			}
			final Set< FeatureProjection< T > > projections = feature.projections();
			if ( null == projections )
				continue;
			System.out.println( "\n - Feature " + spec.getKey() + ". Has " + projections.size() + " projections:" );
			for ( final FeatureProjection< T > projection : projections )
			{
				System.out.println( "   - Projection " + projection.getKey() );
				for ( final T obj : collection )
					System.out.println( "       - " + obj + ": " + projection.value( obj ) );
			}
		}
	}
}
