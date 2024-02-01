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
package org.mastodon.mamut.io.importer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedLinkFeatures;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedSpotFeatures;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedSpotFeatures.Spec;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

public class TrackMateImporterTest
{

	/**
	 * Path to the TrackMate file.
	 */
	private static final String TRACKMATE_FILE = TrackMateImporterTest.class.getResource( "FakeTracks.xml" ).getFile();

	private static final int EXPECTED_N_SPOTS = 84;

	private static final int EXPECTED_N_EDGES = 82;

	private static final int EXPECTED_N_TRACKS = 2;

	private static final List< String > EXPECTED_SPOT_FEATURE_PROJECTIONS = Arrays.asList( new String[] {
			"QUALITY",
			"POSITION_X",
			"POSITION_Y",
			"POSITION_Z",
			"POSITION_T",
			"FRAME",
			"RADIUS",
			"VISIBILITY",
			"MANUAL_SPOT_COLOR",
			"MEAN_INTENSITY_CH1",
			"MEDIAN_INTENSITY_CH1",
			"MIN_INTENSITY_CH1",
			"MAX_INTENSITY_CH1",
			"TOTAL_INTENSITY_CH1",
			"STD_INTENSITY_CH1",
			"CONTRAST_CH1",
			"SNR_CH1"
	} );

	private static final List< Dimension > EXPECTED_SPOT_PROJECTION_DIMENSIONS = Arrays.asList( new Dimension[] {
			Dimension.QUALITY,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.TIME,
			Dimension.NONE,
			Dimension.LENGTH,
			Dimension.NONE,
			Dimension.NONE,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.NONE,
			Dimension.NONE } );

	private static final boolean[] EXPECTED_SPOT_ISINT = new boolean[] {
			false,
			false,
			false,
			false,
			false,
			true,
			false,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false };

	private static final String TARGET_SPOT_LABEL = "ID7931";

	private static final Map< String, Double > EXPECTED_SPOT_FEATURE_VALUES = new HashMap<>();
	static
	{
		EXPECTED_SPOT_FEATURE_VALUES.put( "STD_INTENSITY_CH1", 69.99796221830115 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "QUALITY", 69.92462921142578 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_T", 0.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MIN_INTENSITY_CH1", 1.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "TOTAL_INTENSITY_CH1", 2860.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "CONTRAST_CH1", 0.5622686532835961 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "SNR_CH1", 0.7948717476507929 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "FRAME", 0. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MEDIAN_INTENSITY_CH1", 48.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "VISIBILITY", 1. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "RADIUS", 2.5 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_X", 64.00558680057657 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_Y", 3.9587411612103076 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MEAN_INTENSITY_CH1", 77.29729729729732 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_Z", 0.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MAX_INTENSITY_CH1", 254.0 );
	}

	private static final List< String > EXPECTED_LINK_FEATURE_PROJECTIONS = Arrays.asList( new String[] {
			"SPOT_SOURCE_ID",
			"SPOT_TARGET_ID",
			"LINK_COST",
			"DIRECTIONAL_CHANGE_RATE",
			"SPEED",
			"DISPLACEMENT",
			"EDGE_TIME",
			"EDGE_X_LOCATION",
			"EDGE_Y_LOCATION",
			"EDGE_Z_LOCATION",
			"MANUAL_EGE_COLOR" } );

	private static final int TARGET_LINK_SOURCE_ID = 19398;

	private static final List< Dimension > EXPECTED_LINK_PROJECTION_DIMENSIONS = Arrays.asList( new Dimension[] {
			Dimension.NONE,
			Dimension.NONE,
			Dimension.COST,
			Dimension.ANGLE_RATE,
			Dimension.VELOCITY,
			Dimension.LENGTH,
			Dimension.TIME,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.NONE } );

	private static final Map< String, Double > EXPECTED_LINK_FEATURE_VALUES = new HashMap<>();
	static
	{
		EXPECTED_LINK_FEATURE_VALUES.put( "SPOT_SOURCE_ID", 19398. );
		EXPECTED_LINK_FEATURE_VALUES.put( "SPOT_TARGET_ID", 13497. );
		EXPECTED_LINK_FEATURE_VALUES.put( "LINK_COST", 19.672424744484722 );
		EXPECTED_LINK_FEATURE_VALUES.put( "DIRECTIONAL_CHANGE_RATE", 0.10416278911558505 );
		EXPECTED_LINK_FEATURE_VALUES.put( "SPEED", 4.435360723152597 );
		EXPECTED_LINK_FEATURE_VALUES.put( "DISPLACEMENT", 4.435360723152597 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_TIME", 28.5 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_X_LOCATION", 20.62675333332116 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_Y_LOCATION", 11.44490312857285 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_Z_LOCATION", 0.0 );
	}

	private static final boolean[] EXPECTED_LINK_ISINT = new boolean[] {
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true };

	@Test
	public void test() throws Exception
	{
		try (final Context context = new Context())
		{
			final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
			//			final WindowManager windowManager = new WindowManager( context );
			final TrackMateImporter importer = new TrackMateImporter( new File( TRACKMATE_FILE ) );
			final MamutProject project = importer.createProject();
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			importer.readModel( model, featureSpecsService );

			// Check that the whole model is there.
			final ModelGraph graph = model.getGraph();
			assertEquals( "Unexpected number of vertices.", EXPECTED_N_SPOTS, graph.vertices().size() );
			assertEquals( "Unexpected number of edges.", EXPECTED_N_EDGES, graph.edges().size() );
			final RefSet< Spot > roots = RootFinder.getRoots( graph );
			assertEquals( "Unexpected number of tracks.", EXPECTED_N_TRACKS, roots.size() );

			// Check that we have the expected features.
			final FeatureModel featureModel = model.getFeatureModel();
			final Collection< FeatureSpec< ?, ? > > specs = featureModel.getFeatureSpecs();

			final Spec specSpots = new TrackMateImportedSpotFeatures.Spec();
			assertTrue( "The feature model should contain the specs for TrackMateImportedSpotFeatures.",
					specs.contains( specSpots ) );
			final org.mastodon.mamut.io.importer.trackmate.TrackMateImportedLinkFeatures.Spec specLinks =
					new TrackMateImportedLinkFeatures.Spec();
			assertTrue( "The feature model should contain the specs for TrackMateImportedLinkFeatures.",
					specs.contains( specLinks ) );

			// Inspect spot feature projections.
			@SuppressWarnings( "unchecked" )
			final Feature< Spot > spotFeature = ( Feature< Spot > ) featureModel.getFeature( specSpots );
			inspectFeatureProjections( spotFeature, EXPECTED_SPOT_FEATURE_PROJECTIONS,
					EXPECTED_SPOT_PROJECTION_DIMENSIONS, EXPECTED_SPOT_ISINT );

			// Inspect link feature projections.
			@SuppressWarnings( "unchecked" )
			final Feature< Link > linkFeature = ( Feature< Link > ) featureModel.getFeature( specLinks );
			inspectFeatureProjections( linkFeature, EXPECTED_LINK_FEATURE_PROJECTIONS,
					EXPECTED_LINK_PROJECTION_DIMENSIONS, EXPECTED_LINK_ISINT );

			// Check some spot values.
			boolean tested = false;
			for ( final Spot spot : graph.vertices() )
			{
				if ( spot.getLabel().equals( TARGET_SPOT_LABEL ) )
				{
					testSpotValues( spotFeature, spot, EXPECTED_SPOT_FEATURE_VALUES );
					tested = true;
				}
			}
			assertTrue( "Did not test spot feature values: could not find spot with label " + TARGET_SPOT_LABEL,
					tested );

			// Check some link values.
			final FeatureProjectionSpec specSourceID = linkFeature.getSpec().getProjectionSpecs().stream()
					.filter( fs -> fs.projectionName.equals( "SPOT_SOURCE_ID" ) )
					.findFirst()
					.get();

			tested = false;
			for ( final Link link : graph.edges() )
			{
				if ( linkFeature.project( FeatureProjectionKey.key( specSourceID ) ).value( link )
						== TARGET_LINK_SOURCE_ID )
				{
					testSpotValues( linkFeature, link, EXPECTED_LINK_FEATURE_VALUES );
					tested = true;
				}
			}
			assertTrue( "Did not test link feature values: could not find link with source ID" + TARGET_LINK_SOURCE_ID,
					tested );

		}
	}

	private < O > void testSpotValues( final Feature< O > feature, final O obj,
			final Map< String, Double > expectedSpotFeatureValues )
	{
		for ( final FeatureProjection< O > projection : feature.projections() )
		{
			final String projectionName = projection.getKey().getSpec().projectionName;
			if ( !expectedSpotFeatureValues.containsKey( projectionName ) )
				continue;

			final Double expectedValue = expectedSpotFeatureValues.get( projectionName ).doubleValue();
			assertNotNull( "Unexpected projection: " + projectionName, expectedValue );

			assertEquals( "Unexpected value for projection " + projectionName, expectedValue.doubleValue(),
					projection.value( obj ), 1e-9 );
		}
	}

	private static void inspectFeatureProjections(
			final Feature< ? > feature,
			final List< String > expectedProjectionKeys,
			final List< Dimension > expectedProjectionDimensions,
			final boolean[] expectedProjectionIsint )
	{
		final Set< ? > sp = feature.projections();
		@SuppressWarnings( "unchecked" )
		final Set< FeatureProjection< ? > > projections = ( Set< FeatureProjection< ? > > ) sp;
		assertEquals( "Unexpected number of spot feature projections.", expectedProjectionKeys.size(),
				projections.size() );
		for ( final FeatureProjection< ? > projection : projections )
			assertTrue( "Unexpected projection spec: " + projection.getKey(),
					expectedProjectionKeys.contains( projection.getKey().toString() ) );

		// Inspect feature projection units and multiplicity.
		for ( final FeatureProjectionSpec projSpec : feature.getSpec().getProjectionSpecs() )
		{
			final String key = projSpec.getKey();
			final int index = expectedProjectionKeys.indexOf( key );
			assertTrue( "Feature projection spec is unexpected: " + key, index >= 0 );

			assertEquals( "Unexpected dimension for projection " + key + ".",
					expectedProjectionDimensions.get( index ), projSpec.projectionDimension );

			assertEquals( "Unexpected name for projection " + key + ".",
					expectedProjectionKeys.get( index ), projSpec.projectionName );

			// Int or Double?
			final FeatureProjection< ? > projection = feature.project( FeatureProjectionKey.key( projSpec ) );
			if ( expectedProjectionIsint[ index ] )
				MatcherAssert.assertThat( projection, instanceOf( IntFeatureProjection.class ) );
		}
	}
}
