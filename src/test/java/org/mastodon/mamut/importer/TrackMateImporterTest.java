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
package org.mastodon.mamut.importer;

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
import org.mastodon.mamut.importer.trackmate.TrackMateImportedLinkFeatures;
import org.mastodon.mamut.importer.trackmate.TrackMateImportedSpotFeatures;
import org.mastodon.mamut.importer.trackmate.TrackMateImportedSpotFeatures.Spec;
import org.mastodon.mamut.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
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
			"CELL_DIVISION_TIME",
			"MEAN_INTENSITY01",
			"MEAN_INTENSITY02",
			"MEAN_INTENSITY03",
			"MEAN_INTENSITY04",
			"MEAN_INTENSITY05",
			"MEAN_INTENSITY06",
			"MEAN_INTENSITY07",
			"MEAN_INTENSITY08",
			"MEAN_INTENSITY09",
			"MEAN_INTENSITY10",
			"MANUAL_COLOR",
			"MEAN_INTENSITY",
			"MEDIAN_INTENSITY",
			"MIN_INTENSITY",
			"MAX_INTENSITY",
			"TOTAL_INTENSITY",
			"STANDARD_DEVIATION",
			"ESTIMATED_DIAMETER",
			"CONTRAST",
			"SNR" } );

	private static final List< Dimension > EXPECTED_SPOT_PROJECTION_DIMENSIONS = Arrays.asList( new Dimension[] {
			Dimension.QUALITY,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.TIME,
			Dimension.NONE,
			Dimension.LENGTH,
			Dimension.NONE,
			Dimension.TIME,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.NONE,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.INTENSITY,
			Dimension.LENGTH,
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
			false,
			false,
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

	private static final String TARGET_SPOT_LABEL = "ID12168";

	private static final Map< String, Double > EXPECTED_SPOT_FEATURE_VALUES = new HashMap<>();
	static
	{
		EXPECTED_SPOT_FEATURE_VALUES.put( "ID", 12168. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "QUALITY", 69.52396392822266 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_T", 26.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MAX_INTENSITY", 250.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MEAN_INTENSITY01", 102.64864864864865 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "FRAME", 26. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MEDIAN_INTENSITY", 89.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "VISIBILITY", 1. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MEAN_INTENSITY", 102.64864864864865 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "TOTAL_INTENSITY", 3798.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "ESTIMATED_DIAMETER", 2.000000006274173 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "RADIUS", 2.5 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "SNR", 0.8749354370590104 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_X", 48.48500802831536 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_Y", 82.31747794676691 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "STANDARD_DEVIATION", 75.39349228334417 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "CONTRAST", 0.4734305928474442 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MANUAL_COLOR", -10921639. );
		EXPECTED_SPOT_FEATURE_VALUES.put( "MIN_INTENSITY", 2.0 );
		EXPECTED_SPOT_FEATURE_VALUES.put( "POSITION_Z", 0.0 );
	}

	private static final List< String > EXPECTED_LINK_FEATURE_PROJECTIONS = Arrays.asList( new String[] {
			"DIRECTIONAL_CHANGE_RATE",
			"SPOT_SOURCE_ID",
			"SPOT_TARGET_ID",
			"LINK_COST",
			"EDGE_TIME",
			"EDGE_X_LOCATION",
			"EDGE_Y_LOCATION",
			"EDGE_Z_LOCATION",
			"VELOCITY",
			"DISPLACEMENT",
			"MANUAL_COLOR" } );

	private static final int TARGET_LINK_SOURCE_ID = 9606;

	private static final List< Dimension > EXPECTED_LINK_PROJECTION_DIMENSIONS = Arrays.asList( new Dimension[] {
			Dimension.RATE,
			Dimension.NONE,
			Dimension.NONE,
			Dimension.NONE,
			Dimension.TIME,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.POSITION,
			Dimension.VELOCITY,
			Dimension.LENGTH,
			Dimension.NONE } );

	private static final Map< String, Double > EXPECTED_LINK_FEATURE_VALUES = new HashMap<>();
	static
	{
		EXPECTED_LINK_FEATURE_VALUES.put( "SPOT_SOURCE_ID", 9606. );
		EXPECTED_LINK_FEATURE_VALUES.put( "SPOT_TARGET_ID", 9714. );
		EXPECTED_LINK_FEATURE_VALUES.put( "DIRECTIONAL_CHANGE_RATE", 0.0684556910686953 );
		EXPECTED_LINK_FEATURE_VALUES.put( "LINK_COST", 24.91997727918449 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_TIME", 20.5 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_X_LOCATION", 59.4344391448265 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_Y_LOCATION", 69.9907062102883 );
		EXPECTED_LINK_FEATURE_VALUES.put( "EDGE_Z_LOCATION", 0.0 );
		EXPECTED_LINK_FEATURE_VALUES.put( "VELOCITY", 4.991991314013326 );
		EXPECTED_LINK_FEATURE_VALUES.put( "DISPLACEMENT", 4.991991314013326 );
	}

	private static final boolean[] EXPECTED_LINK_ISINT = new boolean[] {
			false,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true };

	@Test
	public void test()
	{
		final Context context = new Context();
		final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
//		final WindowManager windowManager = new WindowManager( context );
		try
		{
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
			final org.mastodon.mamut.importer.trackmate.TrackMateImportedLinkFeatures.Spec specLinks = new TrackMateImportedLinkFeatures.Spec();
			assertTrue( "The feature model should contain the specs for TrackMateImportedLinkFeatures.",
					specs.contains( specLinks ) );

			// Inspect spot feature projections.
			@SuppressWarnings( "unchecked" )
			final Feature< Spot > spotFeature = ( Feature< Spot > ) featureModel.getFeature( specSpots );
			inspectFeatureProjections( spotFeature, EXPECTED_SPOT_FEATURE_PROJECTIONS, EXPECTED_SPOT_PROJECTION_DIMENSIONS, EXPECTED_SPOT_ISINT );

			// Inspect link feature projections.
			@SuppressWarnings( "unchecked" )
			final Feature< Link > linkFeature = ( Feature< Link > ) featureModel.getFeature( specLinks );
			inspectFeatureProjections( linkFeature, EXPECTED_LINK_FEATURE_PROJECTIONS, EXPECTED_LINK_PROJECTION_DIMENSIONS, EXPECTED_LINK_ISINT );

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
			assertTrue( "Did not test spot feature values: could not find spot with label " + TARGET_SPOT_LABEL, tested );

			// Check some link values.
			final FeatureProjectionSpec specSourceID = linkFeature.getSpec().getProjectionSpecs().stream()
					.filter( fs -> fs.projectionName.equals( "SPOT_SOURCE_ID" ) )
					.findFirst()
					.get();

			tested = false;
			for ( final Link link : graph.edges() )
			{
				if ( linkFeature.project( FeatureProjectionKey.key( specSourceID ) ).value( link ) == TARGET_LINK_SOURCE_ID )
				{
					testSpotValues( linkFeature, link, EXPECTED_LINK_FEATURE_VALUES );
					tested = true;
				}
			}
			assertTrue( "Did not test link feature values: could not find link with source ID" + TARGET_LINK_SOURCE_ID, tested );

		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	private < O > void testSpotValues( final Feature< O > feature, final O obj, final Map< String, Double > expectedSpotFeatureValues )
	{
		for ( final FeatureProjection< O > projection : feature.projections() )
		{
			final String projectionName = projection.getKey().getSpec().projectionName;
			if ( !expectedSpotFeatureValues.containsKey( projectionName ) )
				continue;

			final Double expectedValue = expectedSpotFeatureValues.get( projectionName ).doubleValue();
			assertNotNull( "Unexpected projection: " + projectionName, expectedValue );

			assertEquals( "Unexpected value for projection " + projectionName, expectedValue.doubleValue(), projection.value( obj ), 1e-9 );
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
		assertEquals( "Unexpected number of spot feature projections.", expectedProjectionKeys.size(), projections.size() );
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
