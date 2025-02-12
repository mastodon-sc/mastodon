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
package org.mastodon.mamut.io.importer.trackmate;

import static org.mastodon.feature.Multiplicity.SINGLE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.ANALYZER_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.ANALYZER_KEY_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.ANALYZER_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.BOOKMARKS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_ANALYZERS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_SOURCE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_TARGET_ANALYZER_VALUE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.EDGE_TARGET_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_SHORT_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FILENAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FILTERED_TRACKS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FILTER_FEATURE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FILTER_IS_ABOVE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FILTER_VALUE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FOLDER_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FRAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FRAME_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.GUI_STATE_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.HEIGHT_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.ID_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.IMAGE_DATA_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.INITIAL_SPOT_FILTER_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.LABEL_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.MODEL_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.NFRAMES_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.NSLICES_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.PIXEL_HEIGHT_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.PIXEL_WIDTH_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.POSITION_T_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.POSITION_X_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.POSITION_Y_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.POSITION_Z_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.QUALITY_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.RADIUS_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SETTINGS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SETUP_ASSIGNMENTS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPATIAL_UNITS_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_ANALYZERS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_NSPOTS_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_ELEMENT_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_FILTER_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.SPOT_FRAME_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TIME_UNITS_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACKMATE_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_ANALYZERS_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_FEATURE_DECLARATION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_FILTER_COLLECTION_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_ID_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_ID_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.TRACK_TAG;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.VERSION_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.VISIBILITY_FEATURE_NAME;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.VOXEL_DEPTH_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.WIDTH_ATTRIBUTE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.PoolObject;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlKeys;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.XmlIoTimePoints;

/**
 * Class to export a Mastodon project to MaMuT Fiji plugin.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutExporter
{

	private final Model model;

	private final MamutProject project;

	private final Element root;

	/**
	 * Used to retrieve equivalent radius.
	 */
	private final JamaEigenvalueDecomposition eig;

	/**
	 * Used to retrieve equivalent radius.
	 */
	private final double[][] cov;

	private final List< ExportFeatureProjection< Spot > > spotFeatureProjections;

	private final List< ExportFeatureProjection< Link > > linkFeatureProjections;

	private MamutExporter( final Model model, final MamutProject project )
	{
		this.model = model;
		this.project = project;
		this.root = new Element( TRACKMATE_TAG );
		root.setAttribute( VERSION_ATTRIBUTE, "7.0.4" );
		this.eig = new JamaEigenvalueDecomposition( 3 );
		this.cov = new double[ 3 ][ 3 ];

		spotFeatureProjections =
				getExportFeatureProjections( model.getFeatureModel(), Spot.class, TrackMateImportedSpotFeatures.class );
		linkFeatureProjections =
				getExportFeatureProjections( model.getFeatureModel(), Link.class, TrackMateImportedLinkFeatures.class );
	}

	private void write( final File file ) throws IOException
	{
		try (FileOutputStream fos = new FileOutputStream( file ))
		{
			final Document document = new Document( root );
			final XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
			outputter.output( document, fos );
		}
	}

	private void appendModel()
	{
		final Element modelElement = new Element( MODEL_TAG );
		modelElement.setAttribute( SPATIAL_UNITS_ATTRIBUTE, model.getSpaceUnits() );
		modelElement.setAttribute( TIME_UNITS_ATTRIBUTE, model.getTimeUnits() );

		final Element featureDeclarationElement = featuresDeclarationToXml();
		modelElement.addContent( featureDeclarationElement );

		final Element spotElement = spotCollectionToXml();
		modelElement.addContent( spotElement );

		final Element[] tracksElements = trackCollectionToXml();
		for ( final Element element : tracksElements )
			modelElement.addContent( element );

		root.addContent( modelElement );
	}

	private void appendSettings()
	{
		final Element settingsElement = new Element( SETTINGS_TAG );

		final Element imageDataElement = imageDataToXml();
		settingsElement.addContent( imageDataElement );

		final Element initialSpotFilterElement = initialSpotFilterToXml();
		settingsElement.addContent( initialSpotFilterElement );

		final Element spotFilterCollectionElement = new Element( SPOT_FILTER_COLLECTION_TAG );
		settingsElement.addContent( spotFilterCollectionElement );

		final Element trackFilterCollectionElement = new Element( TRACK_FILTER_COLLECTION_TAG );
		settingsElement.addContent( trackFilterCollectionElement );

		final Element analyzerCollection = analyzerCollectionToXml();
		settingsElement.addContent( analyzerCollection );

		root.addContent( settingsElement );
	}

	/**
	 * Try to locates a .settings file for the bdv file and import the content
	 * that MaMuT can recognize (setup assignments and bookmarks).
	 */
	private void appendGuiState()
	{
		final String fs = project.getDatasetXmlFile().getAbsolutePath();
		final int ixml = fs.lastIndexOf( ".xml" );
		final String settingsFileStr = fs.substring( 0, ixml ) + ".settings" + fs.substring( ixml );
		final File settingsFile = new File( settingsFileStr );
		if ( settingsFile.exists() && settingsFile.isFile() && settingsFile.canRead() )
		{
			final SAXBuilder sax = new SAXBuilder();
			try
			{
				final Element guiStateElement = new Element( GUI_STATE_TAG );
				final Document doc = sax.build( settingsFile );
				final Element root = doc.getRootElement();
				final Element setupAssignmentsElement = root.getChild( SETUP_ASSIGNMENTS_TAG ).detach();
				guiStateElement.addContent( setupAssignmentsElement );
				final Element bookmarksElement = root.getChild( BOOKMARKS_TAG ).detach();
				guiStateElement.addContent( bookmarksElement );
				this.root.addContent( guiStateElement );
			}
			catch ( final JDOMException | IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	private Element analyzerCollectionToXml()
	{
		final Element analyzerCollectionElement = new Element( ANALYZER_COLLECTION_TAG );

		final Element spotAnalyzersElement = new Element( SPOT_ANALYZERS_TAG );
		analyzerCollectionElement.addContent( spotAnalyzersElement );

		final Element edgeAnalyzersElement = new Element( EDGE_ANALYZERS_TAG );
		final Element edgeTargetAnalyzerElement = new Element( ANALYZER_TAG );
		edgeTargetAnalyzerElement.setAttribute( ANALYZER_KEY_ATTRIBUTE, EDGE_TARGET_ANALYZER_VALUE );
		edgeAnalyzersElement.addContent( edgeTargetAnalyzerElement );
		analyzerCollectionElement.addContent( edgeAnalyzersElement );

		final Element trackAnalyzersElement = new Element( TRACK_ANALYZERS_TAG );
		analyzerCollectionElement.addContent( trackAnalyzersElement );

		return analyzerCollectionElement;
	}

	private Element initialSpotFilterToXml()
	{
		final Element initialSpotFilterElement = new Element( INITIAL_SPOT_FILTER_TAG );
		initialSpotFilterElement.setAttribute( FILTER_FEATURE_ATTRIBUTE, QUALITY_FEATURE_NAME );
		initialSpotFilterElement.setAttribute( FILTER_VALUE_ATTRIBUTE, Double.toString( 0. ) );
		initialSpotFilterElement.setAttribute( FILTER_IS_ABOVE_ATTRIBUTE, Boolean.toString( true ) );
		return initialSpotFilterElement;
	}

	private Element imageDataToXml()
	{
		final Collection< Attribute > attributes = new ArrayList<>();

		// File path.
		final File datasetXmlFile = project.getDatasetXmlFile();
		attributes.add( new Attribute( FILENAME_ATTRIBUTE, datasetXmlFile.getName() ) );
		String folder = datasetXmlFile.getParentFile().getAbsolutePath();
		try
		{
			folder = datasetXmlFile.getParentFile().getCanonicalPath();
		}
		catch ( final IOException e )
		{}
		attributes.add( new Attribute( FOLDER_ATTRIBUTE, folder ) );

		// Image attributes.
		final Document document = getSAXParsedDocument( project.getDatasetXmlFile().getAbsolutePath() );

		final List< Element > viewSetupsElements = document
				.getRootElement()
				.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
				.getChild( XmlKeys.VIEWSETUPS_TAG )
				.getChildren( XmlKeys.VIEWSETUP_TAG );
		double pixelWidth = 1.;
		double pixelHeight = 1.0;
		double voxelDepth = 1.0;
		int width = 1;
		int height = 1;
		int nslices = 1;
		for ( final Element vsEl : viewSetupsElements )
		{
			final Element vs, uel;
			if ( null != ( vs = vsEl.getChild( XmlKeys.VIEWSETUP_VOXELSIZE_TAG ) )
					&& null != ( uel = vs.getChild( XmlKeys.VOXELDIMENSIONS_SIZE_TAG ) ) )
			{
				final String val = uel.getContent( 0 ).getValue();
				final double[] calibration = Arrays.stream( val.split( " " ) )
						.mapToDouble( Double::parseDouble )
						.toArray();
				pixelWidth = calibration[ 0 ];
				pixelHeight = calibration[ 1 ];
				voxelDepth = calibration[ 2 ];
			}

			final Element sel;
			if ( null != ( sel = vsEl.getChild( XmlKeys.VIEWSETUP_SIZE_TAG ) ) )
			{
				final String val = sel.getContent( 0 ).getValue();
				final int[] sizes = Arrays.stream( val.split( " " ) )
						.mapToInt( Integer::parseInt )
						.toArray();
				width = sizes[ 0 ];
				height = sizes[ 1 ];
				nslices = sizes[ 2 ];
			}
		}
		attributes.add( new Attribute( WIDTH_ATTRIBUTE, Integer.toString( width ) ) );
		attributes.add( new Attribute( HEIGHT_ATTRIBUTE, Integer.toString( height ) ) );
		attributes.add( new Attribute( NSLICES_ATTRIBUTE, Integer.toString( nslices ) ) );
		attributes.add( new Attribute( PIXEL_WIDTH_ATTRIBUTE, Double.toString( pixelWidth ) ) );
		attributes.add( new Attribute( PIXEL_HEIGHT_ATTRIBUTE, Double.toString( pixelHeight ) ) );
		attributes.add( new Attribute( VOXEL_DEPTH_ATTRIBUTE, Double.toString( voxelDepth ) ) );

		final Element timePointsElement = document
				.getRootElement()
				.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
				.getChild( XmlKeys.TIMEPOINTS_TAG );
		int nframes = 1;
		final XmlIoTimePoints xmlIoTimePoints = new XmlIoTimePoints();
		try
		{
			final TimePoints timePoints = xmlIoTimePoints.fromXml( timePointsElement );
			nframes = timePoints.size();
		}
		catch ( final SpimDataException e )
		{
			e.printStackTrace();
		}
		attributes.add( new Attribute( NFRAMES_ATTRIBUTE, Integer.toString( nframes ) ) );

		final Element imageDataElement = new Element( IMAGE_DATA_TAG );
		imageDataElement.setAttributes( attributes );
		return imageDataElement;
	}

	private Element[] trackCollectionToXml()
	{
		/*
		 * Track collection element.
		 */
		final Element allTracksElement = new Element( TRACK_COLLECTION_TAG );

		// Collect roots, as candidates for single tracks.
		final RefList< Spot > roots = RefCollections.createRefList( model.getGraph().vertices() );
		roots.addAll( RootFinder.getRoots( model.getGraph() ) );

		// Sort by ID (not needed but hey).
		final Comparator< Spot > labelComparator = Comparator.comparingInt( PoolObject::getInternalPoolIndex );
		roots.sort( labelComparator );

		/*
		 * We will iterate the graph, cross component by cross component, to
		 * serialize the tracks.
		 */
		final DepthFirstSearch< Spot, Link > search =
				new DepthFirstSearch<>( model.getGraph(), SearchDirection.UNDIRECTED );
		final RefSet< Spot > toSkip = RefCollections.createRefSet( model.getGraph().vertices() );
		final RefList< Spot > iteratedRoots = RefCollections.createRefList( model.getGraph().vertices() );

		for ( final Spot root : roots )
		{
			// Skip over the roots that were path of a track already dealt with.
			if ( toSkip.contains( root ) )
				continue;

			// Create the track element.
			final Element trackElement = trackToXml( root );
			final SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > > searchListener =
					new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
					{

						@Override
						public void processVertexLate( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
						{
							/*
							 * 1 root = 1 track, unless a track has several
							 * roots. Add the iterated vertex to the list of
							 * root to skip if needed.
							 */
							if ( vertex.incomingEdges().isEmpty() )
								toSkip.add( vertex );
						}

						@Override
						public void processVertexEarly( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
						{}

						@Override
						public void processEdge( final Link edge, final Spot from, final Spot to,
								final DepthFirstSearch< Spot, Link > search )
						{
							// Add iterated edge to the track element.
							final Element edgeElement =
									edgeToXml( edge, from.getInternalPoolIndex(), to.getInternalPoolIndex() );
							trackElement.addContent( edgeElement );
						}

						@Override
						public void crossComponent( final Spot from, final Spot to,
								final DepthFirstSearch< Spot, Link > search )
						{}
					};
			search.setTraversalListener( searchListener );
			search.start( root );

			// Don't serialize empty track (no edges).
			if ( trackElement.getContentSize() > 0 )
			{
				allTracksElement.addContent( trackElement );
				iteratedRoots.add( root );
			}
		}

		/*
		 * Filtered track collection element.
		 */

		final Element filteredTracksElement = new Element( FILTERED_TRACKS_TAG );
		for ( final Spot spot : iteratedRoots )
		{
			final Element filteredTrackID = new Element( TRACK_ID_TAG );
			filteredTrackID.setAttribute( TRACK_ID_ATTRIBUTE, Integer.toString( spot.getInternalPoolIndex() ) );
			filteredTracksElement.addContent( filteredTrackID );
		}

		return new Element[] { allTracksElement, filteredTracksElement };
	}

	private Element spotCollectionToXml()
	{
		final Element spotCollectionElement = new Element( SPOT_COLLECTION_TAG );
		spotCollectionElement.setAttribute( SPOT_COLLECTION_NSPOTS_ATTRIBUTE,
				Integer.toString( model.getGraph().vertices().size() ) );

		// Read time points from dataset xml.
		List< TimePoint > tps = null;
		try
		{
			final Document document = getSAXParsedDocument( project.getDatasetXmlFile().getAbsolutePath() );
			final Element timePointsElement = document
					.getRootElement()
					.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
					.getChild( XmlKeys.TIMEPOINTS_TAG );
			final XmlIoTimePoints xmlIoTimePoints = new XmlIoTimePoints();
			final TimePoints timePoints = xmlIoTimePoints.fromXml( timePointsElement );
			tps = timePoints.getTimePointsOrdered();
		}
		catch ( final SpimDataException e )
		{
			e.printStackTrace();
		}

		final SpatioTemporalIndex< Spot > spots = model.getSpatioTemporalIndex();
		for ( int tpIndex = 0; tpIndex < tps.size(); tpIndex++ )
		{
			final TimePoint tp = tps.get( tpIndex );

			final Element frameSpotsElement = new Element( SPOT_FRAME_COLLECTION_TAG );
			frameSpotsElement.setAttribute( FRAME_ATTRIBUTE, tp.getName() );

			for ( final Spot spot : spots.getSpatialIndex( tpIndex ) )
			{
				final Element spotElement = spotToXml( spot );
				frameSpotsElement.addContent( spotElement );
			}
			spotCollectionElement.addContent( frameSpotsElement );
		}

		return spotCollectionElement;
	}

	/**
	 * Collection of link feature names that we want to export in the mamut
	 * file, but computed from the link data currently set in Mastodon. We used
	 * this collection to avoid exporting a feature with identical name in the
	 * case a feature imported as the same name.
	 */
	private final static Set< String > IMPORTED_LINK_BUILTIN_FEATURES = new HashSet<>( Arrays.asList(
			EDGE_SOURCE_ATTRIBUTE, EDGE_TARGET_ATTRIBUTE ) );

	private Element edgeToXml( final Link edge, final int sourceSpotID, final int targetSpotID )
	{
		final Collection< Attribute > attributes = new ArrayList<>();

		// Source and target ID.
		attributes.add( new Attribute( EDGE_SOURCE_ATTRIBUTE, Integer.toString( sourceSpotID ) ) );
		attributes.add( new Attribute( EDGE_TARGET_ATTRIBUTE, Integer.toString( targetSpotID ) ) );

		// Link features.
		for ( final ExportFeatureProjection< Link > p : linkFeatureProjections )
		{
			final String attName;
			final String origName = p.attributeName;
			/*
			 * If the model to export was imported from a TrackMate or a MaMuT
			 * file, it will already contain features with the same name that
			 * the builtin feature we added just above. Which will cause an
			 * error.
			 * 
			 * To avoid this, rename these imported features.
			 */
			if ( IMPORTED_LINK_BUILTIN_FEATURES.contains( origName ) )
			{
				final String importedAttName = "IMPORTED_" + origName;
				attName = importedAttName;
			}
			else if ( origName.startsWith( "IMPORTED_" ) )
			{
				// We skip features that have been re-imported.
				continue;
			}
			else
			{
				// All good.
				attName = origName;
			}

			attributes.add( new Attribute(
					attName,
					Double.toString( p.projection.value( edge ) ) ) );
		}

		final Element edgeElement = new Element( EDGE_TAG );
		edgeElement.setAttributes( attributes );
		return edgeElement;
	}

	private Element trackToXml( final Spot root )
	{
		final Collection< Attribute > attributes = new ArrayList<>();

		// Track name.
		attributes.add( new Attribute( TRACK_NAME_ATTRIBUTE, root.getLabel() ) );

		// Track ID.
		attributes.add( new Attribute( TRACK_ID_ATTRIBUTE, Integer.toString( root.getInternalPoolIndex() ) ) );

		// Other track features.
		// TODO: when we compute and store track features, modify this.

		final Element trackElement = new Element( TRACK_TAG );
		trackElement.setAttributes( attributes );
		return trackElement;
	}

	/**
	 * Collection of spot feature names that we want to export in the mamut
	 * file, but computed from the actual position, etc, currently set in
	 * Mastodon. We used this collection to avoid exporting a feature with
	 * identical name in the case a feature imported as the same name.
	 */
	private final static Set< String > IMPORTED_SPOT_BUILTIN_FEATURES = new HashSet<>( Arrays.asList( ID_FEATURE_NAME, LABEL_FEATURE_NAME, POSITION_X_FEATURE_NAME,
			POSITION_Y_FEATURE_NAME, POSITION_Z_FEATURE_NAME, FRAME_FEATURE_NAME,
			POSITION_T_FEATURE_NAME, QUALITY_FEATURE_NAME, VISIBILITY_FEATURE_NAME,
			RADIUS_FEATURE_NAME ) );

	private Element spotToXml( final Spot spot )
	{
		final List< Attribute > attributes = new ArrayList<>();

		// Id.
		attributes.add( new Attribute( ID_FEATURE_NAME, Integer.toString( spot.getInternalPoolIndex() ) ) );
		// Name.
		attributes.add( new Attribute( LABEL_FEATURE_NAME, spot.getLabel() ) );
		// Position.
		attributes.add( new Attribute( POSITION_X_FEATURE_NAME, Double.toString( spot.getDoublePosition( 0 ) ) ) );
		attributes.add( new Attribute( POSITION_Y_FEATURE_NAME, Double.toString( spot.getDoublePosition( 1 ) ) ) );
		attributes.add( new Attribute( POSITION_Z_FEATURE_NAME, Double.toString( spot.getDoublePosition( 2 ) ) ) );
		// Frame and time.
		attributes.add( new Attribute( FRAME_FEATURE_NAME, Integer.toString( spot.getTimepoint() ) ) );
		attributes.add( new Attribute( POSITION_T_FEATURE_NAME, Double.toString( spot.getTimepoint() ) ) );
		// Quality.
		attributes.add( new Attribute( QUALITY_FEATURE_NAME, Double.toString( -1. ) ) );
		// Visibility.
		attributes.add( new Attribute( VISIBILITY_FEATURE_NAME, Integer.toString( 1 ) ) );

		// Radius. We have to scale it by transform norm because in MaMuT they
		// are before rendering.
		spot.getCovariance( cov );
		eig.decomposeSymmetric( cov );
		final double meanRadius = Arrays.stream( eig.getRealEigenvalues() ).map( Math::sqrt ).average().getAsDouble();
		attributes.add( new Attribute( RADIUS_FEATURE_NAME, Double.toString( meanRadius ) ) );

		for (final ExportFeatureProjection< Spot > p : spotFeatureProjections )
		{
			final String attName;
			final String origName = p.attributeName;
			/*
			 * If the model to export was imported from a TrackMate or a MaMuT
			 * file, it will already contain features with the same name that
			 * the builtin feature we added just above. Which will cause an
			 * error.
			 * 
			 * To avoid this, rename these imported features.
			 */
			if ( IMPORTED_SPOT_BUILTIN_FEATURES.contains( origName ) )
			{
				final String importedAttName = "IMPORTED_" + origName;
				attName = importedAttName;
			}
			else if ( origName.startsWith( "IMPORTED_" ) )
			{
				// We skip features that have been re-imported.
				continue;
			}
			else
			{
				// All good.
				attName = origName;
			}
			
			attributes.add( new Attribute(
					attName,
					Double.toString( p.projection.value( spot ) ) ) );
		}

		final Element spotElement = new Element( SPOT_ELEMENT_TAG );
		spotElement.setAttributes( attributes );
		return spotElement;
	}

	private Element featuresDeclarationToXml()
	{
		final Element featuresElement = new Element( FEATURE_DECLARATION_TAG );
		appendFeaturesDeclarationOfClass( Spot.class, featuresElement, SPOT_FEATURE_DECLARATION_TAG );
		appendFeaturesDeclarationOfClass( Link.class, featuresElement, EDGE_FEATURE_DECLARATION_TAG );
		// Create an empty declaration for track features, for now.
		appendFeaturesDeclarationOfClass( Boolean.class, featuresElement, TRACK_FEATURE_DECLARATION_TAG );
		return featuresElement;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private < T > void appendFeaturesDeclarationOfClass( final Class< T > clazz, final Element featuresElement,
			final String classFeatureDeclarationTag )
	{
		final List< ExportFeatureProjection< T > > projections;
		if ( clazz.equals( Spot.class ) )
			projections = ( List ) spotFeatureProjections;
		else if ( clazz.equals( Link.class ) )
			projections = ( List ) linkFeatureProjections;
		else
			projections = Collections.emptyList();

		final Element classFeaturesElement = new Element( classFeatureDeclarationTag );
		for ( final ExportFeatureProjection< T > p : projections )
		{
			final String isint = ( p.projection instanceof IntFeatureProjection )
					? "true"
					: "false";

			final Element fel = new Element( FEATURE_TAG );
			fel.setAttribute( FEATURE_ATTRIBUTE, p.attributeName );
			// Mastodon does not support feature name yet.
			fel.setAttribute( FEATURE_NAME_ATTRIBUTE, p.featureName );
			fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, p.featureShortName );
			final String units = p.projection.units();
			fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE,
					unitsToDimension( units, model.getSpaceUnits(), model.getTimeUnits() ) );
			fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, isint );
			classFeaturesElement.addContent( fel );
		}
		featuresElement.addContent( classFeaturesElement );
	}

	private static Document getSAXParsedDocument( final String fileName )
	{
		final SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try
		{
			document = builder.build( fileName );
		}
		catch ( JDOMException | IOException e )
		{
			e.printStackTrace();
		}
		return document;
	}

	/**
	 * Tries to recover the TrackMate dimension from the unit string and the
	 * spatial and time units.
	 *
	 * @param units
	 *            the unit string.
	 * @param spaceUnits
	 *            the spatial units.
	 * @param timeUnits
	 *            the time units.
	 * @return a dimension string that can be parsed by MaMuT.
	 */
	private static String unitsToDimension( final String units, final String spaceUnits, final String timeUnits )
	{
		if ( units.equals( Dimension.COUNTS_UNITS ) )
			return "INTENSITY";
		else if ( units.equals( Dimension.COUNTS_SQUARED_UNITS ) )
			return "INTENSITY_SQUARED";
		else if ( units.equals( Dimension.LENGTH.getUnits( spaceUnits, timeUnits ) ) )
			return "LENGTH";
		else if ( units.equals( Dimension.VELOCITY.getUnits( spaceUnits, timeUnits ) ) )
			return "VELOCITY";
		else if ( units.equals( Dimension.ANGLE.getUnits( spaceUnits, timeUnits ) ) )
			return "ANGLE";
		else if ( units.equals( Dimension.TIME.getUnits( spaceUnits, timeUnits ) ) )
			return "TIME";
		else if ( units.equals( Dimension.RATE.getUnits( spaceUnits, timeUnits ) ) )
			return "RATE";
		else
			return "NONE";
	}

	/**
	 * Creates list of exported feature projections for all feature projections
	 * in {@code featureModel} matching the given {@code target} class.
	 * <p>
	 * Special care are taken to avoid feature name inflation when exporting
	 * re-imported features from a TrackMate file, and conflict between
	 * re-import features and computed features.
	 *
	 * @param featureModel
	 *            the feature model from which to read feature specs and values.
	 * @param target
	 *            the class of the data items (spots or links) for which the
	 *            features we want to export are defined.
	 * @param trackMateImporterFeatureClass
	 *            the class of the TrackMate feature imported in Mastodon for
	 *            the target data item class.
	 * @param <T>
	 *            the type of the data item.
	 * @return a new list of exported feature projections.
	 */
	public static < T > List< ExportFeatureProjection< T > > getExportFeatureProjections(
			final FeatureModel featureModel,
			final Class< T > target,
			final Class< ? > trackMateImporterFeatureClass )
	{
		/*
		 * First iteration: collect all projection names that are NOT coming
		 * from a re-imported TrackMate feature.
		 */

		final HashSet< String > featureModelProjections = new HashSet<>();
		for ( final FeatureSpec< ?, ? > fspec : featureModel.getFeatureSpecs() )
		{
			if ( fspec.getTargetClass().equals( target ) )
			{
				@SuppressWarnings( "unchecked" )
				final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( fspec );
				final String fname = fspec.getKey();
				if ( null == feature.projections() )
					continue;

				if ( trackMateImporterFeatureClass.isInstance( feature ) )
					continue;

				for ( final FeatureProjection< T > projection : feature.projections() )
				{
					final String pname = projection.getKey().getSpec().getKey();
					final String name = isScalarFeature( fspec )
							? getProjectionExportName( fname )
							: getProjectionExportName( fname, pname, projection.getKey().getSourceIndices() );
					featureModelProjections.add( sanitize( name ) );
				}
			}
		}

		/*
		 * Second iteration: build the list of feature projection to export,
		 * filtering if there are some duplicates due to re-import.
		 */

		final ArrayList< ExportFeatureProjection< T > > list = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > fspec : featureModel.getFeatureSpecs() )
		{
			if ( fspec.getTargetClass().equals( target ) )
			{
				@SuppressWarnings( "unchecked" )
				final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( fspec );
				final String fname = fspec.getKey();
				if ( null == feature.projections() )
					continue;

				/*
				 * Are we trying to export a feature that results from an import
				 * from a TrackMate file?
				 */
				final boolean reExport = trackMateImporterFeatureClass.isInstance( feature );

				for ( final FeatureProjection< T > projection : feature.projections() )
				{
					final String pname = projection.getKey().getSpec().getKey();

					final String name = isScalarFeature( fspec )
							? getProjectionExportName( fname )
							: getProjectionExportName( fname, pname, projection.getKey().getSourceIndices() );

					/*
					 * Avoid re-exporting features that were imported from
					 * TrackMate or MaMuT, with added
					 * 'TrackMateImportedFeatures_' etc. We export them with
					 * their original name. If we don't do that, succeeding
					 * exports/imports result in the feature name being
					 * inflated: <pre> Track N spots --> Track_N_spots -->
					 * TrackMate_Spot_features_Track_N_spots -->
					 * TrackMate_Spot_features_TrackMate_Spot_features_Track_N_spots
					 * --> ...
					 */
					final String croppedName = reExport
							? name.substring( 1 + TrackMateImportedSpotFeatures.KEY.length() )
							: name;

					/*
					 * To avoid name clash between re-imported feature and
					 * feature that were computed in Mastodon: Do not export
					 * this projection if: 1. It is a re-imported feature. 2. We
					 * have one with the same name in the feature model.
					 */

					if ( reExport && featureModelProjections.contains( sanitize( croppedName ) ) )
						continue;

					list.add( new ExportFeatureProjection<>( projection, sanitize( croppedName ), croppedName,
							croppedName ) );
				}
			}
		}
		return list;
	}

	public static class ExportFeatureProjection< T >
	{
		public final FeatureProjection< T > projection;

		public final String attributeName;

		public final String featureName;

		public final String featureShortName;

		ExportFeatureProjection( final FeatureProjection< T > projection, final String attributeName,
				final String featureName, final String featureShortName )
		{
			this.projection = projection;
			this.attributeName = attributeName;
			this.featureName = featureName;
			this.featureShortName = featureShortName;
		}
	}

	/**
	 * Produces a set of attribute names for exported feature projections. This
	 * is to filter these from re-import in {@link TrackMateImporter}
	 *
	 * @param specsService
	 *            the feature specification service.
	 * @param numSources
	 *            the number of views, setup or channel in the dataset.
	 * @param target
	 *            the class of the data item for which we are exporting feature
	 *            projections.
	 * @param <T>
	 *            the type of the data item for which we are exporting feature
	 *            projections.
	 * @return a new set of attribute names to be used as feature keys.
	 */
	public static < T > Set< String > getLikelyExportedFeatureProjections( final FeatureSpecsService specsService,
			final int numSources, final Class< T > target )
	{
		final HashSet< String > names = new HashSet<>();
		if ( null == specsService )
			return names;

		final List< FeatureSpec< ?, T > > fspecs = specsService.getSpecs( target );
		for ( final FeatureSpec< ?, T > fspec : fspecs )
		{
			final String fname = fspec.getKey();
			if ( isScalarFeature( fspec ) )
			{
				names.add( sanitize( getProjectionExportName( fname ) ) );
				continue;
			}
			for ( final FeatureProjectionSpec pspec : fspec.getProjectionSpecs() )
			{
				final String pname = pspec.getKey();
				switch ( fspec.getMultiplicity() )
				{
				case SINGLE:
					names.add( sanitize( getProjectionExportName( fname, pname ) ) );
					break;
				case ON_SOURCES:
					for ( int i = 0; i < numSources; i++ )
						names.add( sanitize( getProjectionExportName( fname, pname, i ) ) );
					break;
				case ON_SOURCE_PAIRS:
					for ( int i = 0; i < numSources; i++ )
						for ( int j = 0; j < numSources; j++ )
							names.add( sanitize( getProjectionExportName( fname, pname, i, j ) ) );
					break;
				}
			}
		}
		return names;
	}

	static boolean isScalarFeature( final FeatureSpec< ?, ? > spec )
	{
		return spec.getMultiplicity() == SINGLE && spec.getProjectionSpecs().size() == 1;
	}

	static String sanitize( final String tag )
	{
		return tag.replace( ' ', '_' );
	}

	static String getProjectionExportName( final String fname )
	{
		return fname;
	}

	static String getProjectionExportName( final String fname, final String pname, final int... sourceIndices )
	{
		final StringBuilder sb = new StringBuilder( fname ).append( " " ).append( pname );
		if ( sourceIndices != null )
		{
			for ( final int sourceIndex : sourceIndices )
			{
				sb.append( " ch" );
				sb.append( sourceIndex );
			}
		}
		return sb.toString();
	}

	public static final void export( final File target, final Model model, final MamutProject project )
			throws IOException
	{
		final MamutExporter exporter = new MamutExporter( model, project );
		exporter.appendModel();
		exporter.appendSettings();
		exporter.appendGuiState();
		exporter.write( target );
	}
}
