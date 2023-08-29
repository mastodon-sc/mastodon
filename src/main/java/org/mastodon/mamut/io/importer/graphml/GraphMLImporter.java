package org.mastodon.mamut.io.importer.graphml;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mastodon.collection.ObjectRefMap;
import org.mastodon.collection.RefMaps;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedFeatures;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

public class GraphMLImporter extends ModelImporter
{

	private static final String GRAPHML_TAG = "graphml";
	private static final String KEY_TAG = "key";
	private static final String GRAPH_TAG = "graph";
	private static final String NODE_TAG = "node";
	private static final String ID_TAG = "id";
	private static final String DATA_TAG = "data";
	private static final String EDGE_TAG = "edge";
	private static final String SOURCE_TAG = "source";
	private static final String TARGET_TAG = "target";

	/**
	 * Imports the data stored in the specified GraphML file into the model of
	 * the specified project model. The X, Y, Z coordinates are scaled by the
	 * pixel sizes read from the setup with the specified id.
	 * <p>
	 * The GraphML file must contain keys for properties with
	 * <code>attr.name</code> 'x', 'y', 'z' and 'frame' to be properly imported.
	 * If it contains the key with <code>attr.name</code> 'label', is is
	 * imported as spot labels.
	 * <p>
	 * Other property keys are imported as projections in a new feature. Right
	 * now, only properties declared for nodes are imported this way. The
	 * properties are imported in Mastodon with no dimension.
	 * 
	 * @param graphMLFile
	 *            the GraphML file.
	 * @param pm
	 *            the project model.
	 * @param spotRadius
	 *            the desired spot radius for imported nodes.
	 * @param setupID
	 *            the setup ID to read pixel size from.
	 * @throws IOException
	 *             if the GraphML file misses some of the required information.
	 */
	public static final void importGraphML( final String graphMLFile, final ProjectModel pm, final int setupID, final double spotRadius ) throws IOException
	{
		final Model model = pm.getModel();
		final SourceAndConverter< ? > sac = pm.getSharedBdvData().getSources().get( setupID );
		final Source< ? > source = sac.getSpimSource();
		final double[] pixelSizes = new double[ 3 ];
		source.getVoxelDimensions().dimensions( pixelSizes );
		new GraphMLImporter( graphMLFile, model ).read( pixelSizes, spotRadius );
	}

	/**
	 * Imports the data stored in the specified GraphML file into the specified
	 * model. The X, Y, Z coordinates are supposed to be already scaled.
	 * <p>
	 * The GraphML file must contain keys for properties with
	 * <code>attr.name</code> 'x', 'y', 'z' and 'frame' to be properly imported.
	 * If it contains the key with <code>attr.name</code> 'label', is is
	 * imported as spot labels.
	 * <p>
	 * Other property keys are imported as projections in a new feature. Right
	 * now, only properties declared for nodes are imported this way. The
	 * properties are imported in Mastodon with no dimension.
	 * 
	 * @param graphMLFile
	 *            the GraphML file.
	 * @param model
	 *            the model to import data in.
	 * @param spotRadius
	 *            the desired spot radius for imported nodes.
	 * @throws IOException
	 *             if the GraphML file misses some of the required information.
	 */
	public static final void importGraphML( final String graphMLFile, final Model model, final double spotRadius ) throws IOException
	{
		new GraphMLImporter( graphMLFile, model ).read( new double[] { 1., 1., 1. }, spotRadius );
	}

	private final String graphMLFile;

	private final Model model;

	private GraphMLImporter( final String graphMLFile, final Model model )
	{
		super( model );
		this.graphMLFile = graphMLFile;
		this.model = model;
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private void read( final double[] pixelSizes, final double radius ) throws IOException
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();
		final Link eref = graph.edgeRef();

		startImport();
		try
		{
			/*
			 * Get root element.
			 */

			final Document document = new SAXBuilder().build( graphMLFile );
			final Element rootEl = document.getRootElement();
			final Namespace ns = rootEl.getNamespace();
			if ( !rootEl.getName().equals( GRAPHML_TAG ) )
				throw new IOException( "Could not import GraphML file. No <" + GRAPHML_TAG + "> element found." );

			/*
			 * Parse key elements.
			 */

			final List< Element > keyEls = rootEl.getChildren( KEY_TAG, ns );
			GraphMLKey xKey = null;
			GraphMLKey yKey = null;
			GraphMLKey zKey = null;
			GraphMLKey frameKey = null;
			GraphMLKey labelKey = null;
			final Map< String, GraphMLKey > featureKeys = new HashMap<>();
			for ( final Element keyEl : keyEls )
			{
				final GraphMLKey key = GraphMLKey.fromXML( keyEl );
				switch ( key.name.toLowerCase() )
				{
				case "x":
					xKey = key;
					continue;
				case "y":
					yKey = key;
					continue;
				case "z":
					zKey = key;
					continue;
				case "frame":
					frameKey = key;
					continue;
				case "label":
					labelKey = key;
					continue;
				default:
					featureKeys.put( key.id, key );
				}
			}
			final List< GraphMLKey > mandatoryKeys = Arrays.asList( xKey, yKey, zKey, frameKey );
			final List< String > keyNames = Arrays.asList( "x", "y", "z", "frame" );
			for ( int i = 0; i < mandatoryKeys.size(); i++ )
			{
				if ( mandatoryKeys.get( i ) == null )
					throw new IOException( "Could not find the key element that specifies the mandatory information for " + keyNames.get( i ) );
			}

			/*
			 * Create features for misc keys.
			 */

			// Create the feature storage objects.
			final GraphMLImportedSpotFeatures spotFeatures = new GraphMLImportedSpotFeatures();
			final GraphMLImportedLinkFeatures linkFeatures = new GraphMLImportedLinkFeatures();
			final Map< String, FeatureProjectionKey > projectionKeyMap = new HashMap<>();
			for ( final GraphMLKey key : featureKeys.values() )
			{
				final Dimension dimension = Dimension.NONE;
				// Units are not stored in the GraphML file format.
				final String units = dimension.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
				final TrackMateImportedFeatures< ? > feature;
				switch ( key.target )
				{
				case EDGE:
					feature = linkFeatures;
					break;
				case NODE:
					feature = spotFeatures;
					break;
				default:
					throw new IllegalArgumentException( "Unknown target type for GraphML: " + key.target );
				}

				final FeatureProjectionKey projectionKey;
				switch ( key.type )
				{
				case FLOAT:
					projectionKey = feature.store( key.name, dimension, units, new DoublePropertyMap( graph.vertices(), Double.NaN ) );
					break;
				case INT:
					projectionKey = feature.store( key.name, dimension, units, new IntPropertyMap( graph.vertices(), Integer.MIN_VALUE ) );
					break;
				default:
					throw new IllegalArgumentException( "Unknown value type for GraphML: " + key.type );
				}
				projectionKeyMap.put( key.id, projectionKey );
			}

			/*
			 * Import spots.
			 */

			final Element graphEl = rootEl.getChild( GRAPH_TAG, ns );
			if ( graphEl == null )
				throw new IOException( "Could not find the graph element." );

			final ObjectRefMap< String, Spot > spotMap = RefMaps.createObjectRefMap( graph.vertices() );
			final double[] pos = new double[ 3 ]; // pixel coordinates
			for ( final Element nodeEl : graphEl.getChildren( NODE_TAG, ns ) )
			{
				// Gather position & time-point.
				Arrays.fill( pos, Double.NaN );
				int frame = -1;
				String label = null;
				final String id = nodeEl.getAttributeValue( ID_TAG );
				for ( final Element dataEl : nodeEl.getChildren( DATA_TAG, ns ) )
				{
					final String keyId = dataEl.getAttributeValue( KEY_TAG );
					if ( keyId.equals( xKey.id ) )
						pos[ 0 ] = Double.parseDouble( dataEl.getText() );
					else if ( keyId.equals( yKey.id ) )
						pos[ 1 ] = Double.parseDouble( dataEl.getText() );
					else if ( keyId.equals( zKey.id ) )
						pos[ 2 ] = Double.parseDouble( dataEl.getText() );
					else if ( keyId.equals( frameKey.id ) )
						frame = Integer.parseInt( dataEl.getText() );
					else if ( keyId.equals( labelKey.id ) )
						label = dataEl.getText();
				}
				if ( frame < 0 )
					throw new IOException( "Could not find the frame infomation (key=" + frameKey.id + ") for node with id " + id );

				if ( Arrays.stream( pos ).anyMatch( Double::isNaN ) )
					throw new IOException( "Could not find the X, Y, Z infomation (keys=" + xKey.id + ", " + yKey.id + ", " + zKey.id
							+ ") for node with id " + id );

				// Map to physical coordinates.
				for ( int d = 0; d < pos.length; d++ )
					pos[ d ] *= pixelSizes[ d ];

				// Create spot.
				final Spot spot = graph.addVertex( ref1 ).init( frame, pos, radius );
				if ( label != null )
					spot.setLabel( label );

				spotMap.put( id, ref1 );

				// Misc spot features.
				for ( final Element dataEl : nodeEl.getChildren( DATA_TAG, ns ) )
				{
					final String keyId = dataEl.getAttributeValue( KEY_TAG );
					final GraphMLKey key = featureKeys.get( keyId );
					if ( key == null )
						continue;

					final FeatureProjectionKey projectionKey = projectionKeyMap.get( keyId );
					switch ( key.type )
					{
					case INT:
					{
						final IntPropertyMap< Spot > map = spotFeatures.getIntPropertyMap( projectionKey );
						final int val = Integer.parseInt( dataEl.getText() );
						map.set( spot, val );
						break;
					}
					case FLOAT:
					{
						final DoublePropertyMap< Spot > map = spotFeatures.getDoublePropertyMap( projectionKey );
						final double val = Double.parseDouble( dataEl.getText() );
						map.set( spot, val );
						break;
					}
					default:
						throw new IllegalArgumentException( "Unknown value type for GraphML: " + key.type );
					}
				}
			}

			/*
			 * Import links.
			 */

			for ( final Element edgeEl : graphEl.getChildren( EDGE_TAG, ns ) )
			{
				final String sourceID = edgeEl.getAttributeValue( SOURCE_TAG );
				final Spot source = spotMap.get( sourceID, ref1 );
				if ( source == null )
					throw new IOException( "Could not find the source node with id " + sourceID + " in the node list." );

				final String targetID = edgeEl.getAttributeValue( TARGET_TAG );
				final Spot target = spotMap.get( targetID, ref2 );
				if ( target == null )
					throw new IOException( "Could not find the source node with id " + targetID + " in the node list." );

				graph.addEdge( source, target, eref ).init();
			}

			/*
			 * Feed property maps to feature model.
			 */

			featureModel.pauseListeners();
			featureModel.declareFeature( spotFeatures );
			featureModel.declareFeature( linkFeatures );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
			graph.releaseRef( eref );
			featureModel.resumeListeners();
			finishImport();
		}
	}
}
