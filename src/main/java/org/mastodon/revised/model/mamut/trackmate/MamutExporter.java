package org.mastodon.revised.model.mamut.trackmate;

import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.ANALYZER_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.ANALYZER_KEY_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.ANALYZER_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.BOOKMARKS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_ANALYZERS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_SOURCE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_TARGET_ANALYZER_VALUE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_TARGET_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_NAME_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_SHORT_NAME_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FILENAME_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FILTERED_TRACKS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FILTER_FEATURE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FILTER_IS_ABOVE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FILTER_VALUE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FOLDER_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FRAME_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FRAME_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.GUI_STATE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.HEIGHT_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.ID_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.IMAGE_DATA_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.INITIAL_SPOT_FILTER_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.LABEL_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.MODEL_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.NFRAMES_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.NSLICES_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.PIXEL_HEIGHT_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.PIXEL_WIDTH_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_T_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_X_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_Y_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_Z_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.QUALITY_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.RADIUS_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SETTINGS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SETUP_ASSIGNMENTS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPATIAL_UNITS_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_ANALYZERS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_NSPOTS_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_ELEMENT_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_FILTER_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_FRAME_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TIME_UNITS_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACKMATE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_ANALYZERS_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_FILTER_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_ID_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_ID_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_NAME_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.VERSION_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.VISIBILITY_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.VOXEL_DEPTH_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.WIDTH_ATTRIBUTE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.IntScalarFeature;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.spatial.SpatioTemporalIndex;

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

	private MamutExporter( final Model model, final MamutProject project )
	{
		this.model = model;
		this.project = project;
		this.root = new Element( TRACKMATE_TAG );
		root.setAttribute( VERSION_ATTRIBUTE, "3.6.0" );
		this.eig = new JamaEigenvalueDecomposition( 3 );
		this.cov = new double[ 3 ][ 3 ];
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
		final String spaceUnits = getSpatialUnits();
		final String timeUnits = getTimeUnits();

		final Element modelElement = new Element( MODEL_TAG );
		modelElement.setAttribute( SPATIAL_UNITS_ATTRIBUTE, spaceUnits );
		modelElement.setAttribute( TIME_UNITS_ATTRIBUTE, timeUnits );

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
			if ( null != ( vs = vsEl.getChild( XmlKeys.VIEWSETUP_VOXELSIZE_TAG ) ) && null != ( uel = vs.getChild( XmlKeys.VOXELDIMENSIONS_SIZE_TAG ) ) )
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
		final Comparator< Spot > labelComparator = ( o1, o2 ) -> Integer.compare( o1.getInternalPoolIndex(), o2.getInternalPoolIndex() );
		roots.sort( labelComparator );

		/*
		 * We will iterate the graph, cross component by cross component, to
		 * serialize the tracks.
		 */
		final DepthFirstSearch< Spot, Link > search = new DepthFirstSearch<>( model.getGraph(), SearchDirection.UNDIRECTED );
		final RefSet< Spot > toSkip = RefCollections.createRefSet( model.getGraph().vertices() );
		final RefList< Spot > iteratedRoots = RefCollections.createRefList( model.getGraph().vertices() );

		for ( final Spot root : roots )
		{
			// Skip over the roots that were path of a track already dealt with.
			if ( toSkip.contains( root ) )
				continue;

			// Create the track element.
			final Element trackElement = trackToXml( root );
			final SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > > searchListener = new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
			{

				@Override
				public void processVertexLate( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
				{
					/*
					 * 1 root = 1 track, unless a track has several roots. Add
					 * the iterated vertex to the list of root to skip if
					 * needed.
					 */
					if ( vertex.incomingEdges().isEmpty() )
						toSkip.add( vertex );
				}

				@Override
				public void processVertexEarly( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
				{}

				@Override
				public void processEdge( final Link edge, final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
				{
					// Add iterated edge to the track element.
					final Element edgeElement = edgeToXml( edge, from.getInternalPoolIndex(), to.getInternalPoolIndex() );
					trackElement.addContent( edgeElement );
				}

				@Override
				public void crossComponent( final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
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
		spotCollectionElement.setAttribute( SPOT_COLLECTION_NSPOTS_ATTRIBUTE, Integer.toString( model.getGraph().vertices().size() ) );

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
		for ( final TimePoint tp : tps )
		{

			final Element frameSpotsElement = new Element( SPOT_FRAME_COLLECTION_TAG );
			frameSpotsElement.setAttribute( FRAME_ATTRIBUTE, tp.getName() );

			for ( final Spot spot : spots.getSpatialIndex( tp.getId() ) )
			{
				final Element spotElement = spotToXml( spot );
				frameSpotsElement.addContent( spotElement );
			}
			spotCollectionElement.addContent( frameSpotsElement );
		}

		return spotCollectionElement;
	}

	private Element edgeToXml( final Link edge, final int sourceSpotID, final int targetSpotID )
	{
		final Collection< Attribute > attributes = new ArrayList<>();

		// Source and target ID.
		attributes.add( new Attribute( EDGE_SOURCE_ATTRIBUTE, Integer.toString( sourceSpotID ) ) );
		attributes.add( new Attribute( EDGE_TARGET_ATTRIBUTE, Integer.toString( targetSpotID ) ) );

		// Link features.
		final FeatureModel fm = model.getFeatureModel();
		Set< Feature< ?, ? > > features = fm.getFeatureSet( Link.class );
		if ( null == features )
			features = Collections.emptySet();

		for ( final Feature< ?, ? > feature : features )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Link, ? > f = ( Feature< Link, PropertyMap< Link, ? > > ) feature;
			final Map< String, FeatureProjection< Link > > projections = f.getProjections();
			for ( final String projectionKey : projections.keySet() )
				attributes.add( new Attribute( sanitize( projectionKey ), Double.toString( projections.get( projectionKey ).value( edge ) ) ) );
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
//		final FeatureModel fm = model.getFeatureModel();
		Set< Feature< ?, ? > > features = null;
		if ( null == features )
			features = Collections.emptySet();

		for ( final Feature< ?, ? > feature : features )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Spot, ? > f = ( Feature< Spot, PropertyMap< Spot, ? > > ) feature;
			final Map< String, FeatureProjection< Spot > > projections = f.getProjections();
			for ( final String projectionKey : projections.keySet() )
				attributes.add( new Attribute( sanitize( projectionKey ), Double.toString( projections.get( projectionKey ).value( root ) ) ) );
		}

		final Element trackElement = new Element( TRACK_TAG );
		trackElement.setAttributes( attributes );
		return trackElement;
	}

	private Element spotToXml( final Spot spot )
	{
		final Collection< Attribute > attributes = new ArrayList<>();

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

		// Spot features.
		final FeatureModel fm = model.getFeatureModel();
		Set< Feature< ?, ? > > features = fm.getFeatureSet( Spot.class );
		if ( null == features )
			features = Collections.emptySet();

		for ( final Feature< ?, ? > feature : features )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Spot, ? > f = ( Feature< Spot, PropertyMap< Spot, ? > > ) feature;
			final Map< String, FeatureProjection< Spot > > projections = f.getProjections();
			for ( final String projectionKey : projections.keySet() )
				attributes.add( new Attribute( sanitize( projectionKey ), Double.toString( projections.get( projectionKey ).value( spot ) ) ) );
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

	private void appendFeaturesDeclarationOfClass( final Class< ? > clazz, final Element featuresElement, final String classFeatureDeclarationTag )
	{
		final String spaceUnits = getSpatialUnits();
		final String timeUnits = getTimeUnits();
		final FeatureModel fm = model.getFeatureModel();
		Set< Feature< ?, ? > > features = fm.getFeatureSet( clazz );
		if ( null == features )
			features = Collections.emptySet();

		final Element classFeaturesElement = new Element( classFeatureDeclarationTag );
		for ( final Feature< ?, ? > feature : features )
		{
			/*
			 * If we have ONE feature mapped on an int projection map, then we
			 * can use the ISINT flag of TrackMate safely.
			 */
			final String isint;
			if ( feature instanceof IntScalarFeature )
				isint = "true";
			else
				isint = " false";

			// We actually export feature projections.
			final Map< String, ? > p = feature.getProjections();
			@SuppressWarnings( "unchecked" )
			final Map< String, FeatureProjection< ? > > projections = ( Map< String, FeatureProjection< ? > > ) p;
			for ( final String projectionKey : projections.keySet() )
			{
				final Element fel = new Element( FEATURE_TAG );
				fel.setAttribute( FEATURE_ATTRIBUTE, sanitize( projectionKey ) );
				// Mastodon does not support feature name yet.
				fel.setAttribute( FEATURE_NAME_ATTRIBUTE, projectionKey );
				fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, projectionKey );
				final String units = projections.get( projectionKey ).units();
				fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE, unitsToDimension( units, spaceUnits, timeUnits ) );
				fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, isint );
				classFeaturesElement.addContent( fel );
			}
		}
		featuresElement.addContent( classFeaturesElement );
	}

	private String getSpatialUnits()
	{
		// Read space units from dataset xml if we can.
		String spaceUnits = "pixel";
		final Document document = getSAXParsedDocument( project.getDatasetXmlFile().getAbsolutePath() );
		final List< Element > viewSetupsElements = document
				.getRootElement()
				.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
				.getChild( XmlKeys.VIEWSETUPS_TAG )
				.getChildren( XmlKeys.VIEWSETUP_TAG );
		for ( final Element vsEl : viewSetupsElements )
		{
			final Element vs, uel;
			if ( null != ( vs = vsEl.getChild( XmlKeys.VIEWSETUP_VOXELSIZE_TAG ) ) && null != ( uel = vs.getChild( XmlKeys.VOXELDIMENSIONS_UNIT_TAG ) ) )
			{
				spaceUnits = uel.getValue();
				break;
			}
		}
		return spaceUnits;
	}

	private String getTimeUnits()
	{
		// BDV does not let you save frame interval in physical units.
		final String timeUnits = "frame";
		return timeUnits;
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

	private static final String sanitize( final String tag )
	{
		return tag.replace( ' ', '_' );
	}

	/**
	 * Tries to recover the dimension from the unit string and the spatial and
	 * time units.
	 * 
	 * @param units
	 *            the unit string.
	 * @param spaceUnits
	 *            the spatial units.
	 * @param timeUnits
	 *            the time units.
	 * @return a dimension string that can be parsed by MaMuT.
	 */
	private static final String unitsToDimension( final String units, final String spaceUnits, final String timeUnits )
	{
		if ( units.equals( "Quality" ) )
			return "QUALITY";
		else if ( units.equals( "Counts" ) )
			return "INTENSITY";
		else if ( units.equals( "Counts^2" ) )
			return "INTENSITY_SQUARED";
		else if ( units.equals( spaceUnits ) )
			return "LENGTH";
		else if ( units.equals( spaceUnits + "/" + timeUnits ) )
			return "VELOCITY";
		else if ( units.equals( timeUnits ) )
			return "TIME";
		else if ( units.equals( "Radians" ) )
			return "ANGLE";
		else if ( units.equals( "/" + timeUnits ) )
			return "RATE";
		else
			return "NONE";
	}

	public static final void export( final File target, final Model model, final MamutProject project ) throws IOException
	{
		final MamutExporter exporter = new MamutExporter( model, project );
		exporter.appendModel();
		exporter.appendSettings();
		exporter.appendGuiState();
		exporter.write( target );
	}

	public static void main( final String[] args ) throws IOException
	{
		final String projectFolder = "samples/mamutproject";
		final String bdvFile = "samples/datasethdf5.xml";
		final MamutProject project = new MamutProject( new File( projectFolder ), new File( bdvFile ) );
		final Model model = new Model();
		model.loadRaw( project );
		final File target = new File( "samples/mamutExport.xml" );
		export( target, model, project );
	}
}
