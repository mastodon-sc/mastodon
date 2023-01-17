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

import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.EDGE_FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.EDGE_SOURCE_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.EDGE_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.EDGE_TARGET_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FEATURE_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FILENAME_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FOLDER_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.FRAME_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.HEIGHT_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.ID_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.IMAGE_DATA_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.LABEL_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.MODEL_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.NFRAMES_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.NSLICES_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.PIXEL_HEIGHT_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.PIXEL_WIDTH_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.POSITION_X_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.POSITION_Y_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.POSITION_Z_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.RADIUS_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SETTINGS_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SPATIAL_UNITS_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SPOT_ELEMENT_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SPOT_FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.SPOT_FRAME_COLLECTION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.TIME_UNITS_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.TRACK_COLLECTION_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.TRACK_TAG;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.VISIBILITY_FEATURE_NAME;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.VOXEL_DEPTH_ATTRIBUTE;
import static org.mastodon.mamut.importer.trackmate.TrackMateXMLKeys.WIDTH_ATTRIBUTE;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefMaps;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Importer for TrackMate (http://imagej.net/TrackMate) files.
 * <p>
 * The importer can read the model as a whole and also import feature values.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class TrackMateImporter
{
	private final File file;

	private final Document document;

	/**
	 * Read the specified TrackMate file.
	 *
	 * @param file
	 *            the path to the TrackMate file.
	 * @throws IOException
	 *             if an error happens while opening or parsing the XML file.
	 */
	public TrackMateImporter( final File file ) throws IOException
	{
		this.file = file;
		try
		{
			document = new SAXBuilder().build( file );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
	}

	/**
	 * Create a {@link MamutProject} with the image data specified in the
	 * TrackMate file. This works only for MaMuT files (where the image data is
	 * a SpimData XML file).
	 *
	 * @return a new project.
	 * @throws IOException
	 *             if valid image data cannot be obtained from the TrackMate
	 *             file.
	 */
	public MamutProject createProject() throws IOException
	{
		final Element root = document.getRootElement();
		final Element settingsEl = root.getChild( SETTINGS_TAG );
		if ( null == settingsEl )
			throw new IOException( "Could not import TrackMate project. No <" + SETTINGS_TAG + "> element found." );

		final Element imageDataEl = settingsEl.getChild( IMAGE_DATA_TAG );
		final String imageFilename = imageDataEl.getAttributeValue( FILENAME_ATTRIBUTE );
		final String imageFolder = imageDataEl.getAttributeValue( FOLDER_ATTRIBUTE );
		File imageFile = new File( imageFolder, imageFilename );
		if ( !imageFile.exists() )
		{
			// Then try relative path
			imageFile = new File( file.getParent(), imageFilename );
			if ( !imageFile.exists() )
			{
				System.err.println( "Warning. Cannot find the image data file: \"" + imageFilename + "\" in \""
						+ imageFolder + "\" nor in \""
						+ file.getParent() + "\". Substituting default void image." );
				imageFile = makDummyImage( imageDataEl );
			}
		}

		final MamutProject project = new MamutProject( null, imageFile );

		// Set project time and space units
		final Element modelEl = root.getChild( MODEL_TAG );
		if ( null != modelEl )
		{
			final String spaceUnits = modelEl.getAttributeValue( SPATIAL_UNITS_ATTRIBUTE );
			if ( spaceUnits != null )
				project.setSpaceUnits( spaceUnits );
			final String timeUnits = modelEl.getAttributeValue( TIME_UNITS_ATTRIBUTE );
			if ( timeUnits != null )
				project.setTimeUnits( timeUnits );
		}

		return project;
	}

	/**
	 * Returns a dummy BDV file, made to reflect the metadata stored in the
	 * <code>ImageData</code> XML element of a TrackMate file.
	 *
	 * @param imageDataEl
	 *            the <code>ImageData</code> XML element.
	 * @return a dummy BDF file.
	 */
	private static File makDummyImage( final Element imageDataEl )
	{
		final String wel = imageDataEl.getAttributeValue( WIDTH_ATTRIBUTE );
		final int width = wel == null ? 1000 : Integer.parseInt( wel );

		final String hel = imageDataEl.getAttributeValue( HEIGHT_ATTRIBUTE );
		final int height = hel == null ? 1000 : Integer.parseInt( hel );

		final String zel = imageDataEl.getAttributeValue( NSLICES_ATTRIBUTE );
		final int depth = zel == null ? 100 : Integer.parseInt( zel );

		final String ntel = imageDataEl.getAttributeValue( NFRAMES_ATTRIBUTE );
		final int nTimepoints = ntel == null ? 100 : Integer.parseInt( ntel );

		final String dxel = imageDataEl.getAttributeValue( PIXEL_WIDTH_ATTRIBUTE );
		final double dx = dxel == null ? 1. : Double.parseDouble( dxel );

		final String dyel = imageDataEl.getAttributeValue( PIXEL_HEIGHT_ATTRIBUTE );
		final double dy = dyel == null ? 1. : Double.parseDouble( dyel );

		final String dzel = imageDataEl.getAttributeValue( VOXEL_DEPTH_ATTRIBUTE );
		final double dz = dzel == null ? 1. : Double.parseDouble( dzel );

		//		final String dtel = imageDataEl.getAttributeValue( TIME_INTERVAL_ATTRIBUTE );
		//		final double dt = dtel == null ? 1. : Double.parseDouble( dtel );

		final String dummyStr = String.format( "x=%d y=%d z=%d sx=%f sy=%f sz=%f t=%d.dummy",
				width, height, depth, dx, dy, dz, nTimepoints );
		return new File( dummyStr );
	}

	/**
	 * Imports the specified TrackMate file into a Mastodon {@link Model}.
	 *
	 * @param model
	 *            the Model that will receive the imported data.
	 * @throws IOException
	 *             if the TrackMate file cannot be imported.
	 */
	public void readModel( final Model model ) throws IOException
	{
		readModel( model, null );
	}

	public void readModel( final Model model, final FeatureSpecsService featureSpecsService ) throws IOException
	{
		new Import( model, featureSpecsService );
	}

	private final class Import extends ModelImporter
	{
		Import( final Model model, final FeatureSpecsService featureSpecsService ) throws IOException
		{
			super( model );
			startImport();

			final ModelGraph graph = model.getGraph();

			final Element root = document.getRootElement();
			final Element modelEl = root.getChild( MODEL_TAG );
			if ( null == modelEl )
				throw new IOException( "Could not import TrackMate project. No <" + MODEL_TAG + "> element found." );

			/*
			 * Units.
			 */

			final String spaceUnits = modelEl.getAttributeValue( SPATIAL_UNITS_ATTRIBUTE );
			final String timeUnits = modelEl.getAttributeValue( TIME_UNITS_ATTRIBUTE );

			/*
			 * Read feature declaration and instantiate mastodon features.
			 */

			final Element featureDeclarationEl = modelEl.getChild( FEATURE_DECLARATION_TAG );

			/*
			 * TODO: could get this from the spimdata XML, for now just we're
			 *       safe for a while with 10...
			 */
			final int expectedNumSources = 10;

			// Spot features.
			final TrackMateImportedSpotFeatures spotFeatures = new TrackMateImportedSpotFeatures();
			final Element spotFeatureDeclarationEl = featureDeclarationEl.getChild( SPOT_FEATURE_DECLARATION_TAG );
			final List< Element > spotFeatureEls = spotFeatureDeclarationEl.getChildren( FEATURE_TAG );
			final Map< String, DoublePropertyMap< Spot > > spotDoubleFeatureMap = new HashMap<>();
			final Map< String, IntPropertyMap< Spot > > spotIntFeatureMap = new HashMap<>();
			final Set< String > ignoredSpotFeatureKeys = MamutExporter
					.getLikelyExportedFeatureProjections( featureSpecsService, expectedNumSources, Spot.class );
			for ( final Element featureEl : spotFeatureEls )
			{
				final String featureKey = featureEl.getAttributeValue( FEATURE_ATTRIBUTE );
				if ( ignoredSpotFeatureKeys.contains( featureKey ) )
					continue;
				//				final String featureName = featureEl.getAttributeValue( FEATURE_NAME_ATTRIBUTE );
				//				final String featureShortName = featureEl.getAttributeValue( FEATURE_SHORT_NAME_ATTRIBUTE );
				final String featureDimension = featureEl.getAttributeValue( FEATURE_DIMENSION_ATTRIBUTE );
				final String units = dimensionToUnits( featureDimension, spaceUnits, timeUnits );
				final boolean featureIsInt =
						Boolean.parseBoolean( featureEl.getAttributeValue( FEATURE_ISINT_ATTRIBUTE ) );
				if ( featureIsInt )
				{
					final IntPropertyMap< Spot > values =
							new IntPropertyMap<>( graph.vertices().getRefPool(), Integer.MIN_VALUE );
					spotFeatures.store( featureKey, dimensionToDimension( featureDimension ), units, values );
					spotIntFeatureMap.put( featureKey, values );
				}
				else
				{
					final DoublePropertyMap< Spot > values =
							new DoublePropertyMap<>( graph.vertices().getRefPool(), Double.NaN );
					spotFeatures.store( featureKey, dimensionToDimension( featureDimension ), units, values );
					spotDoubleFeatureMap.put( featureKey, values );
				}
			}

			// Link features.
			final TrackMateImportedLinkFeatures linkFeatures = new TrackMateImportedLinkFeatures();
			final Element edgeFeatureDeclarationEl = featureDeclarationEl.getChild( EDGE_FEATURE_DECLARATION_TAG );
			final List< Element > edgeFeatureEls = edgeFeatureDeclarationEl.getChildren( FEATURE_TAG );
			final Map< String, DoublePropertyMap< Link > > linkDoubleFeatureMap = new HashMap<>();
			final Map< String, IntPropertyMap< Link > > linkIntFeatureMap = new HashMap<>();
			final Set< String > ignoredLinkFeatureKeys = MamutExporter
					.getLikelyExportedFeatureProjections( featureSpecsService, expectedNumSources, Link.class );
			for ( final Element featureEl : edgeFeatureEls )
			{
				final String featureKey = featureEl.getAttributeValue( FEATURE_ATTRIBUTE );
				if ( ignoredLinkFeatureKeys.contains( featureKey ) )
					continue;
				//				final String featureName = featureEl.getAttributeValue( FEATURE_NAME_ATTRIBUTE );
				//				final String featureShortName = featureEl.getAttributeValue( FEATURE_SHORT_NAME_ATTRIBUTE );
				final String featureDimension = featureEl.getAttributeValue( FEATURE_DIMENSION_ATTRIBUTE );
				final String units = dimensionToUnits( featureDimension, spaceUnits, timeUnits );
				final boolean featureIsInt =
						Boolean.parseBoolean( featureEl.getAttributeValue( FEATURE_ISINT_ATTRIBUTE ) );
				if ( featureIsInt )
				{
					final IntPropertyMap< Link > values =
							new IntPropertyMap<>( graph.edges().getRefPool(), Integer.MIN_VALUE );
					linkFeatures.store( featureKey, dimensionToDimension( featureDimension ), units, values );
					linkIntFeatureMap.put( featureKey, values );
				}
				else
				{
					final DoublePropertyMap< Link > values =
							new DoublePropertyMap<>( graph.edges().getRefPool(), Double.NaN );
					linkFeatures.store( featureKey, dimensionToDimension( featureDimension ), units, values );
					linkDoubleFeatureMap.put( featureKey, values );
				}
			}

			/*
			 * Read model and build the graph.
			 */

			final Spot ref = graph.vertexRef();
			final Spot putRef = graph.vertexRef();
			final Spot sourceRef = graph.vertexRef();
			final Spot targetRef = graph.vertexRef();
			final Link edgeRef = graph.edgeRef();

			try
			{
				final double[] pos = new double[ 3 ];

				// Map spot ID -> Vertex
				final IntRefMap< Spot > idToSpotIDmap = RefMaps.createIntRefMap( graph.vertices(), -1 );

				/*
				 * Import spots.
				 */
				final Element allSpotsEl = modelEl.getChild( SPOT_COLLECTION_TAG );
				final List< Element > allFramesEl = allSpotsEl.getChildren( SPOT_FRAME_COLLECTION_TAG );
				for ( final Element frameEl : allFramesEl )
				{
					final List< Element > frameSpotsEl = frameEl.getChildren( SPOT_ELEMENT_TAG );
					for ( final Element spotEl : frameSpotsEl )
					{
						final boolean visible =
								Integer.parseInt( spotEl.getAttributeValue( VISIBILITY_FEATURE_NAME ) ) != 0;
						if ( !visible )
							continue;

						// Create spot.
						pos[ 0 ] = Double.parseDouble( spotEl.getAttributeValue( POSITION_X_FEATURE_NAME ) );
						pos[ 1 ] = Double.parseDouble( spotEl.getAttributeValue( POSITION_Y_FEATURE_NAME ) );
						pos[ 2 ] = Double.parseDouble( spotEl.getAttributeValue( POSITION_Z_FEATURE_NAME ) );
						final double radius = Double.parseDouble( spotEl.getAttributeValue( RADIUS_FEATURE_NAME ) );
						final int frame = Integer.parseInt( spotEl.getAttributeValue( FRAME_FEATURE_NAME ) );
						final int id = Integer.parseInt( spotEl.getAttributeValue( ID_FEATURE_NAME ) );
						final String label = spotEl.getAttributeValue( LABEL_FEATURE_NAME );

						final Spot spot = graph.addVertex( ref ).init( frame, pos, radius );
						spot.setLabel( label );
						idToSpotIDmap.put( id, spot, putRef );

						// Spot features.
						for ( final String featureKey : spotDoubleFeatureMap.keySet() )
						{
							final String attributeValue = spotEl.getAttributeValue( featureKey );
							if ( null != attributeValue )
							{
								final double val = Double.parseDouble( attributeValue );
								final DoublePropertyMap< Spot > feature = spotDoubleFeatureMap.get( featureKey );
								feature.set( spot, val );
							}
						}
						for ( final String featureKey : spotIntFeatureMap.keySet() )
						{
							final String attributeValue = spotEl.getAttributeValue( featureKey );
							if ( null != attributeValue )
							{
								final int val = NumberFormat.getInstance().parse( attributeValue ).intValue();
								final IntPropertyMap< Spot > feature = spotIntFeatureMap.get( featureKey );
								feature.set( spot, val );
							}
						}
					}
				}

				/*
				 * Import edges.
				 */
				final Element trackCollectionEl = modelEl.getChild( TRACK_COLLECTION_TAG );
				final List< Element > trakEls = trackCollectionEl.getChildren( TRACK_TAG );
				for ( final Element trackEl : trakEls )
				{
					final List< Element > edgeEls = trackEl.getChildren( EDGE_TAG );
					for ( final Element edgeEl : edgeEls )
					{
						// Create links.
						final int sourceID = Integer.parseInt( edgeEl.getAttributeValue( EDGE_SOURCE_ATTRIBUTE ) );
						final Spot source = idToSpotIDmap.get( sourceID, sourceRef );
						final int targetID = Integer.parseInt( edgeEl.getAttributeValue( EDGE_TARGET_ATTRIBUTE ) );
						final Spot target = idToSpotIDmap.get( targetID, targetRef );

						// Protect against link time inversion.
						final Link link;
						if ( source.getTimepoint() < target.getTimepoint() )
							link = graph.addEdge( source, target, edgeRef ).init();
						else
							link = graph.addEdge( target, source, edgeRef ).init();

						// Edge features.
						for ( final String featureKey : linkDoubleFeatureMap.keySet() )
						{
							final String attributeValue = edgeEl.getAttributeValue( featureKey );
							if ( null != attributeValue )
							{
								final double val = Double.parseDouble( attributeValue );
								final DoublePropertyMap< Link > pm = linkDoubleFeatureMap.get( featureKey );
								pm.set( link, val );
							}
						}
						for ( final String featureKey : linkIntFeatureMap.keySet() )
						{
							final String attributeValue = edgeEl.getAttributeValue( featureKey );
							if ( null != attributeValue )
							{
								final int val = NumberFormat.getInstance().parse( attributeValue ).intValue();
								final IntPropertyMap< Link > pm = linkIntFeatureMap.get( featureKey );
								pm.set( link, val );
							}
						}
					}
				}
			}
			catch ( final ParseException e )
			{
				e.printStackTrace();
			}
			finally
			{
				graph.releaseRef( ref );
				graph.releaseRef( putRef );
				graph.releaseRef( sourceRef );
				graph.releaseRef( targetRef );
				graph.releaseRef( edgeRef );
			}

			/*
			 * Feed property maps to feature model.
			 */

			final FeatureModel featureModel = model.getFeatureModel();
			featureModel.pauseListeners();
			featureModel.declareFeature( spotFeatures );
			featureModel.declareFeature( linkFeatures );
			featureModel.resumeListeners();
			finishImport();
		}
	}

	private static final Dimension dimensionToDimension( final String dimension )
	{
		switch ( dimension )
		{
		case "QUALITY":
			return Dimension.QUALITY;
		case "INTENSITY":
			return Dimension.INTENSITY;
		case "INTENSITY_SQUARED":
			return Dimension.INTENSITY_SQUARED;
		case "POSITION":
			return Dimension.POSITION;
		case "LENGTH":
			return Dimension.LENGTH;
		case "VELOCITY":
			return Dimension.VELOCITY;
		case "TIME":
			return Dimension.TIME;
		case "ANGLE":
			return Dimension.ANGLE;
		case "RATE":
			return Dimension.RATE;
		case "STRING":
			return Dimension.STRING;
		case "NONE":
			return Dimension.NONE;
		case "COST":
			return Dimension.COST;
		case "ANGLE_RATE":
			return Dimension.ANGLE_RATE;

		default:
			throw new IllegalArgumentException( "Unkown dimension " + dimension );
		}
	}

	private static final String dimensionToUnits( final String dimension, final String spaceUnits,
			final String timeUnits )
	{
		switch ( dimension )
		{
		case "QUALITY":
			return "Quality";
		case "INTENSITY":
			return "Counts";
		case "INTENSITY_SQUARED":
			return "Counts²";
		case "POSITION":
		case "LENGTH":
			return spaceUnits;
		case "VELOCITY":
			return spaceUnits + "/" + timeUnits;
		case "TIME":
			return timeUnits;
		case "ANGLE":
			return "Radians";
		case "RATE":
			return "/" + timeUnits;
		case "STRING":
		case "NONE":
		default:
			return "";
		}
	}
}
