package org.mastodon.revised.model.mamut.trackmate;

import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_SOURCE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.EDGE_TARGET_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FEATURE_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.FRAME_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.ID_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.LABEL_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.MODEL_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_X_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_Y_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.POSITION_Z_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.RADIUS_FEATURE_NAME;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_ELEMENT_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_FEATURE_DECLARATION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.SPOT_FRAME_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.TRACK_TAG;
import static org.mastodon.revised.model.mamut.trackmate.TrackMateXMLKeys.VISIBILITY_FEATURE_NAME;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefMaps;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

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

	/**
	 * Imports the specified TrackMate file in a Mastodon {@link Model}.
	 *
	 * @param file
	 *            the path to the TrackMate file.
	 * @param model
	 *            the Model that will receive the imported data.
	 * @return a new model.
	 * @throws JDOMException
	 *             if an error happens while parsing the XML file.
	 * @throws IOException
	 *             if an IO error prevents the XML file to be parsed.
	 */
	public static void importModel( final File file, final Model model ) throws JDOMException, IOException
	{
		final ModelGraph graph = model.getGraph();

		final SAXBuilder sb = new SAXBuilder();
		final Document document = sb.build( file );
		final Element root = document.getRootElement();
		final Element modelEl = root.getChild( MODEL_TAG );
		if ( null == modelEl )
			return;

		/*
		 * Read feature declaration and instantiate mastodon features.
		 */

		final Element featureDeclarationEl = modelEl.getChild( FEATURE_DECLARATION_TAG );

		// Spot features.
		final Element spotFeatureDeclarationEl = featureDeclarationEl.getChild( SPOT_FEATURE_DECLARATION_TAG );
		final List< Element > spotFeatureEls = spotFeatureDeclarationEl.getChildren( FEATURE_TAG );
		final Map< String, DoublePropertyMap< Spot > > spotDoubleFeatureMap = new HashMap<>();
		final Map< String, IntPropertyMap< Spot > > spotIntFeatureMap = new HashMap<>();
		for ( final Element featureEl : spotFeatureEls )
		{
			final String featureKey = featureEl.getAttributeValue( FEATURE_ATTRIBUTE );
//			final String featureName = featureEl.getAttributeValue( FEATURE_NAME_ATTRIBUTE );
//			final String featureShortName = featureEl.getAttributeValue( FEATURE_SHORT_NAME_ATTRIBUTE );
//			final String featureDimension = featureEl.getAttributeValue( FEATURE_DIMENSION_ATTRIBUTE );
			final boolean featureIsInt = Boolean.parseBoolean( featureEl.getAttributeValue( FEATURE_ISINT_ATTRIBUTE ) );
			if ( featureIsInt )
			{
				final IntPropertyMap< Spot > feature = new IntPropertyMap<>( graph.vertices(), Integer.MIN_VALUE );
				spotIntFeatureMap.put( featureKey, feature );
			}
			else
			{
				final DoublePropertyMap< Spot > feature = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
				spotDoubleFeatureMap.put( featureKey, feature );
			}
		}

		final Element edgeFeatureDeclarationEl = featureDeclarationEl.getChild( EDGE_FEATURE_DECLARATION_TAG );
		final List< Element > edgeFeatureEls = edgeFeatureDeclarationEl.getChildren( FEATURE_TAG );
		final Map< String, DoublePropertyMap< Link > > linkDoubleFeatureMap = new HashMap<>();
		final Map< String, IntPropertyMap< Link > > linkIntFeatureMap = new HashMap<>();
		for ( final Element featureEl : edgeFeatureEls )
		{
			final String featureKey = featureEl.getAttributeValue( FEATURE_ATTRIBUTE );
//			final String featureName = featureEl.getAttributeValue( FEATURE_NAME_ATTRIBUTE );
//			final String featureShortName = featureEl.getAttributeValue( FEATURE_SHORT_NAME_ATTRIBUTE );
//			final String featureDimension = featureEl.getAttributeValue( FEATURE_DIMENSION_ATTRIBUTE );
			final boolean featureIsInt = Boolean.parseBoolean( featureEl.getAttributeValue( FEATURE_ISINT_ATTRIBUTE ) );
			if ( featureIsInt )
			{
				final IntPropertyMap< Link > feature = new IntPropertyMap<>( graph.edges(), Integer.MIN_VALUE );
				linkIntFeatureMap.put( featureKey, feature );
			}
			else
			{
				final DoublePropertyMap< Link > feature = new DoublePropertyMap<>( graph.edges(), Double.NaN );
				linkDoubleFeatureMap.put( featureKey, feature );
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
					final boolean visible = Integer.parseInt( spotEl.getAttributeValue( VISIBILITY_FEATURE_NAME ) ) != 0;
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
							final DoublePropertyMap< Spot > pm = spotDoubleFeatureMap.get( featureKey );
							pm.set( spot, val );
						}
					}
					for ( final String featureKey : spotIntFeatureMap.keySet() )
					{
						final String attributeValue = spotEl.getAttributeValue( featureKey );
						if ( null != attributeValue )
						{
							final int val = NumberFormat.getInstance().parse( attributeValue ).intValue();
							final IntPropertyMap< Spot > pm = spotIntFeatureMap.get( featureKey );
							pm.set( spot, val );
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
					final Link link = graph.addEdge( source, target, edgeRef ).init();

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
		for ( final String featureKey : spotDoubleFeatureMap.keySet() )
		{
			final DoublePropertyMap< Spot > pm = spotDoubleFeatureMap.get( featureKey );
			final Map< String, FeatureProjection< Spot > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( pm ) );
			final Feature< Spot, DoublePropertyMap< Spot > > feature = new Feature<>( featureKey, Spot.class, pm, projections );
			featureModel.declareFeature( feature );
		}
		for ( final String featureKey : spotIntFeatureMap.keySet() )
		{
			final IntPropertyMap< Spot > pm = spotIntFeatureMap.get( featureKey );
			final Map< String, FeatureProjection< Spot > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( pm ) );
			final Feature< Spot, IntPropertyMap< Spot > > feature = new Feature<>( featureKey, Spot.class, pm, projections );
			featureModel.declareFeature( feature );
		}
		for ( final String featureKey : linkDoubleFeatureMap.keySet() )
		{
			final DoublePropertyMap< Link > pm = linkDoubleFeatureMap.get( featureKey );
			final Map< String, FeatureProjection< Link > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( pm ) );
			final Feature< Link, DoublePropertyMap< Link > > feature = new Feature<>( featureKey, Link.class, pm, projections );
			featureModel.declareFeature( feature );
		}
		for ( final String featureKey : linkIntFeatureMap.keySet() )
		{
			final IntPropertyMap< Link > pm = linkIntFeatureMap.get( featureKey );
			final Map< String, FeatureProjection< Link > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( pm ) );
			final Feature< Link, IntPropertyMap< Link > > feature = new Feature<>( featureKey, Link.class, pm, projections );
			featureModel.declareFeature( feature );
		}
	}
}
