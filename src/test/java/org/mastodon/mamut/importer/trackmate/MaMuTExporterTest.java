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

import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_ELEMENT_KEY;
import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_FILENAME_ATTRIBUTE_NAME;
import static fiji.plugin.trackmate.io.TmXmlKeys.IMAGE_FOLDER_ATTRIBUTE_NAME;
import static fiji.plugin.trackmate.io.TmXmlKeys.SETTINGS_ELEMENT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.algorithm.ConnectedComponents;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import bdv.tools.bookmarks.Bookmarks;
import bdv.viewer.ViewerOptions;
import fiji.plugin.mamut.SourceSettings;
import fiji.plugin.mamut.io.MamutXmlReader;
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

	private static final Collection< String > EDGE_FEATURES_TO_IGNORE = Arrays.asList(
			"LINK_COST", "SPOT_SOURCE_ID", "SPOT_TARGET_ID" );

	// track features added when exporting to TrackMate.
	private static final Collection< String > TRACK_FEATURES_ADDED = Arrays.asList(
			"TRACK_INDEX", "TRACK_ID" );

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
		assertEquals( "Unexpected number of spots in the exported model.",
				sourceModel.getGraph().vertices().size(), exportedModel.getSpots().getNSpots( false ) );

		final NavigableSet< Integer > timepoints = exportedModel.getSpots().keySet();
		for ( final Integer tp : timepoints )
		{
			final int nSpots = exportedModel.getSpots().getNSpots( tp, false );
			final int expected = sourceModel.getSpatioTemporalIndex().getSpatialIndex( tp ).size();
			assertEquals( "Did not found the same number of spots in frame " + tp, expected, nSpots );
		}

		// Check number of links in whole model.
		assertEquals( "Unexpected number of links in the exported model.",
				sourceModel.getGraph().edges().size(), exportedModel.getTrackModel().edgeSet().size() );

		/*
		 * Check number of tracks. Careful, in TrackMate a track with less than
		 * 2 spots is not a track.
		 */
		final Set< RefSet< Spot > > tracks = new ConnectedComponents<>( sourceModel.getGraph(), 2 ).get();
		assertEquals( "Unexpected number of tracks in the exported model.",
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
				EDGE_FEATURES_TO_IGNORE,
				exportedModel.getFeatureModel().getEdgeFeatures() );

		// Check tracks features.

		assertTrue( exportedModel.getFeatureModel().getTrackFeatures().size() == TRACK_FEATURES_ADDED.size()
				&& exportedModel.getFeatureModel().getTrackFeatures().containsAll( TRACK_FEATURES_ADDED )
				&& TRACK_FEATURES_ADDED.containsAll( exportedModel.getFeatureModel().getTrackFeatures() ) );

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
		 * Test whether MaMuT can open the image data. We retrieve the image
		 * file path from the exported MaMuT file.
		 */

		final File file = new File( EXPORT_FILE );
		final SAXBuilder sb = new SAXBuilder();
		Element root = null;
		try
		{
			final Document document = sb.build( file );
			root = document.getRootElement();
		}
		catch ( final JDOMException e )
		{
			fail( "Problem parsing " + file.getName() + ", it is not a valid TrackMate XML file.\nError message is:\n"
					+ e.getLocalizedMessage() );
		}
		catch ( final IOException e )
		{
			fail( "Problem reading " + file.getName()
					+ ".\nError message is:\n" + e.getLocalizedMessage() );
		}

		final Element settingsElement = root.getChild( SETTINGS_ELEMENT_KEY );
		if ( null == settingsElement )
			fail( "Could not find a " + SETTINGS_ELEMENT_KEY + " element in the exported file." );

		final Element imageInfoElement = settingsElement.getChild( IMAGE_ELEMENT_KEY );
		final String filename = imageInfoElement.getAttributeValue( IMAGE_FILENAME_ATTRIBUTE_NAME );
		final String folder = imageInfoElement.getAttributeValue( IMAGE_FOLDER_ATTRIBUTE_NAME );
		if ( null == filename || filename.isEmpty() )
			fail( "Cannot find image file name in xml file.\n" );

		final SourceSettings settings = new SourceSettings( folder, filename );
		reader.readSourceSettings();

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

	private static void loadProject( final Context context, final MamutProject project, final Model model ) throws IOException
	{
		try ( final MamutProject.ProjectReader reader = project.openForReading() )
		{
			final RawGraphIO.FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
			MamutRawFeatureModelIO.deserialize( context, model, idmap, reader );
		}
		catch ( final ClassNotFoundException e )
		{
			throw new RuntimeException( e );
		}
	}


	private Model export() throws IOException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( MASTODON_FILE );

		final String spimDataXmlFilename = project.getDatasetXmlFile().getAbsolutePath();
		final SharedBigDataViewerData sharedBdvData = SharedBigDataViewerData.fromSpimDataXmlFile( spimDataXmlFilename, new ViewerOptions(), () -> {} );

		final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
		loadProject( context, project, model );

		/*
		 * 1.1a. Recompute all features.
		 */

		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Collection< FeatureSpec< ?, ? > > featureKeys = featureComputerService.getFeatureSpecs();
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( sharedBdvData );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute( featureKeys );

		final FeatureModel featureModel = model.getFeatureModel();
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
