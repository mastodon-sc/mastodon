package org.mastodon.revised.model.mamut.trackmate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.algorithm.ConnectedComponents;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

import bdv.tools.bookmarks.Bookmarks;
import fiji.plugin.mamut.SourceSettings;
import fiji.plugin.mamut.io.MamutXmlReader;
import fiji.plugin.mamut.providers.MamutEdgeAnalyzerProvider;
import fiji.plugin.mamut.providers.MamutSpotAnalyzerProvider;
import fiji.plugin.mamut.providers.MamutTrackAnalyzerProvider;
import mpicbg.spim.data.SpimDataException;

public class MaMuTExporterTest
{

	/**
	 * Path to the Mastodon file.
	 */
	private static final String MASTODON_FILE = MaMuTExporterTest.class.getResource( "mamutproject" ).getFile();

	/**
	 * Where to export.
	 */
	private static final String EXPORT_FILE = "mamutExport.xml";

	/**
	 * TrackMate features that are automatically added by TrackMate during
	 * export.
	 */
	private static final Collection< String > SPOT_FEATURES_TO_IGNORE = Arrays.asList(
			"QUALITY", "POSITION_X", "POSITION_Y", "POSITION_Z", "POSITION_T", "FRAME", "RADIUS", "VISIBILITY" );

	@Test
	public void test() throws IOException, SpimDataException
	{
		try
		{
			final Model model = export();
			reloadAndTestAgainst( model );
		}
		finally
		{
			new File( EXPORT_FILE ).delete();
		}
	}

	private void reloadAndTestAgainst( final Model sourceModel )
	{
		// We load the data directly with TrackMate!

		final MamutXmlReader reader = new MamutXmlReader( new File( EXPORT_FILE ) );
		final boolean readingOk = reader.isReadingOk();
		assertTrue( "Could not read exported file: " + reader.getErrorMessage(), readingOk );

		final fiji.plugin.trackmate.Model exportedModel = reader.getModel();

		// Check number of spots in whole model.
		assertEquals( "Unexpected numbe of spots in the exported model.",
				sourceModel.getGraph().vertices().size(), exportedModel.getSpots().getNSpots( false ) );

		final NavigableSet< Integer > timepoints = exportedModel.getSpots().keySet();
		for ( final Integer tp : timepoints )
		{
			final int nSpots = exportedModel.getSpots().getNSpots( tp, false );
			final int expected = sourceModel.getSpatioTemporalIndex().getSpatialIndex( tp ).size();
			assertEquals( "Did not found the same number of spots in frame " + tp, expected, nSpots );
		}

		// Check number of links in whole model.
		assertEquals( "Unexpected of links in the exported model.",
				sourceModel.getGraph().edges().size(), exportedModel.getTrackModel().edgeSet().size() );

		/*
		 * Check number of tracks. Careful, in TrackMate a track with less than
		 * 2 spots is not a track.
		 */
		final Set< RefSet< Spot > > tracks = new ConnectedComponents<>( sourceModel.getGraph(), 2 ).get();
		assertEquals( "Unexpected of tracks in the exported model.",
				tracks.size(), exportedModel.getTrackModel().nTracks( false ) );

		// Check spot features.
		final FeatureModel featureModel = sourceModel.getFeatureModel();
		checkFeatures(
				featureModel,
				Spot.class,
				SPOT_FEATURES_TO_IGNORE,
				exportedModel.getFeatureModel().getSpotFeatures() );

		// Check link features.
		checkFeatures(
				featureModel,
				Link.class,
				Collections.emptyList(),
				exportedModel.getFeatureModel().getEdgeFeatures() );

		// Check tracks features.
		assertTrue( "Export track feature collection should be empty.",
				exportedModel.getFeatureModel().getTrackFeatures().isEmpty() );

		// Check all spot values.
		int ntested = 0;
		for ( final Spot spot : sourceModel.getGraph().vertices() )
		{
			// Find the corresponding spot in the export model.
			for ( final fiji.plugin.trackmate.Spot tmSpot : exportedModel.getSpots().iterable( false ) )
			{
				// Inefficient but hey.
				if ( tmSpot.getName().equals( spot.getLabel() ) )
				{
					ntested++;
					
					for ( int d = 0; d < 3; d++ )
						assertEquals( "Spot position " + ( 'X' + d ) + " does not match exported value.",
								spot.getDoublePosition( d ), tmSpot.getDoublePosition( d ), 1e-9 );

					assertEquals( "Spot frame does not match exported value.",
							spot.getTimepoint(), tmSpot.getFeature( fiji.plugin.trackmate.Spot.FRAME ).intValue() );

					final List< FeatureSpec< ?, ? > > spotFeatures = sourceModel.getFeatureModel().getFeatureSpecs().stream()
							.filter( f -> f.getTargetClass().isAssignableFrom( Spot.class ) )
							.collect( Collectors.toList() );
					for ( final FeatureSpec< ?, ? > featureSpec : spotFeatures )
					{
						final Feature< ? > feature = sourceModel.getFeatureModel().getFeature( featureSpec );
						if ( feature.projections() == null )
							continue;

						for ( final FeatureProjection< ? > projection : feature.projections() )
						{
							@SuppressWarnings( "unchecked" )
							final FeatureProjection< Spot > fp = ( FeatureProjection< Spot > ) projection;
							final String name = getSanitizedFeatureKey( feature, projection );
							assertEquals( "Unexpected feature value for " + spot + " for feature " + name,
									fp.value( spot ), tmSpot.getFeatures().get( name ), 1e-9 );
						}
					}
				}
			}
		}
		assertEquals( "Could not test all spots in the source model.",
				sourceModel.getGraph().vertices().size(), ntested );

		// Check some link values.
		ntested = 0;
		for ( final Link link : sourceModel.getGraph().edges() )
		{
			// Find the corresponding link in the export model.
			for ( final DefaultWeightedEdge tmLink : exportedModel.getTrackModel().edgeSet() )
			{
				// Inefficient but hey.
				if ( exportedModel.getTrackModel().getEdgeSource( tmLink ).getName().equals( link.getSource().getLabel() )
						&& exportedModel.getTrackModel().getEdgeTarget( tmLink ).getName().equals( link.getTarget().getLabel() ) )
				{
					ntested++;
					
					final List< FeatureSpec< ?, ? > > linkFeatures = sourceModel.getFeatureModel().getFeatureSpecs().stream()
							.filter( f -> f.getTargetClass().isAssignableFrom( Link.class ) )
							.collect( Collectors.toList() );
					for ( final FeatureSpec< ?, ? > featureSpec : linkFeatures )
					{
						final Feature< ? > feature = sourceModel.getFeatureModel().getFeature( featureSpec );
						if ( feature.projections() == null )
							continue;

						for ( final FeatureProjection< ? > projection : feature.projections() )
						{
							@SuppressWarnings( "unchecked" )
							final FeatureProjection< Link > fp = ( FeatureProjection< Link > ) projection;
							final String name = getSanitizedFeatureKey( feature, projection );
							assertEquals( "Unexpected feature value for " + link + " for feature " + name,
									fp.value( link ), exportedModel.getFeatureModel().getEdgeFeature( tmLink, name ), 1e-9 );

						}
					}
				}
			}
		}
		assertEquals( "Could not test all links in the source model.",
				sourceModel.getGraph().edges().size(), ntested );
		
		/*
		 * Test whether MaMuT can open the image data.
		 */

		final SourceSettings settings = new SourceSettings();
		reader.readSettings( settings, null, null, new MamutSpotAnalyzerProvider(), new MamutEdgeAnalyzerProvider(), new MamutTrackAnalyzerProvider() );
		File imageFile = new File( settings.imageFolder, settings.imageFileName );
		if ( !imageFile.exists() )
		{
			// Then try relative path
			imageFile = new File( new File( EXPORT_FILE ).getParent(), settings.imageFileName );
		}
		assertNotNull( "Cannot find the image data file: " + settings.imageFileName
				+ " in " + settings.imageFolder + " nor in " + new File( EXPORT_FILE ), imageFile );

		// And the bookmarks.
		reader.readBookmarks( new Bookmarks() );
	}

	private Model export() throws IOException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final MamutProject project = new MamutProjectIO().load( MASTODON_FILE );
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		/*
		 * 1.1a. Recompute all features.
		 */

		final Context context = windowManager.getContext();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Collection< FeatureSpec< ?, ? > > featureKeys = featureComputerService.getFeatureSpecs();
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute( featureKeys );

		featureModel.pauseListeners();
		featureModel.clear();
		for ( final FeatureSpec< ?, ? > spec : features.keySet() )
			featureModel.declareFeature( features.get( spec ) );
		featureModel.resumeListeners();

		/*
		 * 2. Export it to a MaMuT file.
		 */

		final File targetFile = new File( EXPORT_FILE );
		MamutExporter.export( targetFile, model, project );

		/*
		 * 3. Return the Mastodon model for comparison.
		 */

		return model;
	}

	private static final void checkFeatures(
			final FeatureModel featureModel, 
			final Class< ? > targetClass, 
			final Collection< String > featuresToIgnore,
			final Collection< String > exportedFeatures )
	{
		final Collection< FeatureSpec< ?, ? > > featureSpecs = featureModel.getFeatureSpecs();
		final List< FeatureSpec< ?, ? > > filteredFeatureSpecs = featureSpecs.stream()
				.filter( fs -> fs.getTargetClass().isAssignableFrom( targetClass ) )
				.collect( Collectors.toList() );

		// Build names of expected feature keys.
		final List< String > featureDeclaration = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > fs : filteredFeatureSpecs )
		{
			final Feature< ? > feature = featureModel.getFeature( fs );
			if ( null == feature.projections() )
				continue;

			for ( final FeatureProjection< ? > projection : feature.projections() )
				featureDeclaration.add( getSanitizedFeatureKey( feature, projection ) );
		}

		// Check that we have the TrackMate basic features.
		for ( final String featureKey : featuresToIgnore )
			assertTrue( "Export is missing basic TrackMate feature " + featureKey + ".",
					exportedFeatures.contains( featureKey ) );

		// Remove them.
		exportedFeatures.removeAll( featuresToIgnore );

		// Check that we also have the Mastodon features.
		assertEquals( "Unexpected number of spot features.", featureDeclaration.size(), exportedFeatures.size() );
		for ( final String featureKey : featureDeclaration )
			assertTrue( "Could not retrieve feature with key " + featureKey + " in the exported model.",
					exportedFeatures.contains( featureKey ) );
	}

	private static final String getSanitizedFeatureKey( final Feature< ? > feature, final FeatureProjection< ? > projection )
	{
		final String pname = projection.getKey().getSpec().projectionName;
		final String fname = feature.getSpec().getKey();
		final String name = MamutExporter.isScalarFeature( feature.getSpec() )
				? MamutExporter.getProjectionExportName( fname )
				: MamutExporter.getProjectionExportName( fname, pname, projection.getKey().getSourceIndices() );
		return MamutExporter.sanitize( name );
	}
}
