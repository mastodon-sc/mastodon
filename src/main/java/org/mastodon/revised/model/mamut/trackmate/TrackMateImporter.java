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

	public static class TrackMateModel
	{
		public final Model model;

		public final Map< String, DoublePropertyMap< Spot > > spotDoubleFeatures;

		public final Map< String, IntPropertyMap< Spot > > spotIntFeatures;

		public final Map< String, DoublePropertyMap< Link > > linkDoubleFeatures;

		public final Map< String, IntPropertyMap< Link > > linkIntFeatures;

		public TrackMateModel(
				final Model model,
				final Map< String, DoublePropertyMap< Spot > > spotDoubleFeatures,
				final Map< String, IntPropertyMap< Spot > > spotIntFeatures,
				final Map< String, DoublePropertyMap< Link > > linkDoubleFeatures,
				final Map< String, IntPropertyMap< Link > > linkIntFeatures )
		{
			this.model = model;
			this.spotDoubleFeatures = spotDoubleFeatures;
			this.spotIntFeatures = spotIntFeatures;
			this.linkDoubleFeatures = linkDoubleFeatures;
			this.linkIntFeatures = linkIntFeatures;
		}
	}

	/**
	 * Imports the specified TrackMate file as a Mastodon {@link Model}.
	 *
	 * @param file
	 *            the path to the TrackMate file.
	 * @return a new model.
	 * @throws JDOMException
	 *             if an error happens while parsing the XML file.
	 * @throws IOException
	 *             if an IO error prevents the XML file to be parsed.
	 */
	public static TrackMateModel importModel( final File file ) throws JDOMException, IOException
	{
		final Model model = new Model();
		final ModelGraph graph = model.getGraph();

		final SAXBuilder sb = new SAXBuilder();
		final Document document = sb.build( file );
		final Element root = document.getRootElement();
		final Element modelEl = root.getChild( MODEL_TAG );
		if ( null == modelEl ) { return null; }

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
		final Map< String, DoublePropertyMap< Link > > edgeDoubleFeatureMap = new HashMap<>();
		final Map< String, IntPropertyMap< Link > > edgeIntFeatureMap = new HashMap<>();
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
				edgeIntFeatureMap.put( featureKey, feature );
			}
			else
			{
				final DoublePropertyMap< Link > feature = new DoublePropertyMap<>( graph.edges(), Double.NaN );
				edgeDoubleFeatureMap.put( featureKey, feature );
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
							final DoublePropertyMap< Spot > feature = spotDoubleFeatureMap.get( featureKey );
							feature.set( spot, val );
						}
					}
					for ( final String featureKey : spotIntFeatureMap.keySet() )
					{
						final String attributeValue = spotEl.getAttributeValue( featureKey );
						if ( null != attributeValue )
						{
							final int val = Integer.parseInt( attributeValue );
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
					final Link link = graph.addEdge( source, target, edgeRef ).init();

					// Edge features.
					for ( final String featureKey : edgeDoubleFeatureMap.keySet() )
					{
						final String attributeValue = edgeEl.getAttributeValue( featureKey );
						if ( null != attributeValue )
						{
							final double val = Double.parseDouble( attributeValue );
							final DoublePropertyMap< Link > feature = edgeDoubleFeatureMap.get( featureKey );
							feature.set( link, val );
						}
					}
					for ( final String featureKey : edgeIntFeatureMap.keySet() )
					{
						final String attributeValue = edgeEl.getAttributeValue( featureKey );
						if (null != attributeValue)
						{
							final int val = Integer.parseInt( attributeValue );
							final IntPropertyMap< Link > feature = edgeIntFeatureMap.get( featureKey );
							feature.set( link, val );
						}
					}
				}
			}

			return new TrackMateModel( model, spotDoubleFeatureMap, spotIntFeatureMap, edgeDoubleFeatureMap, edgeIntFeatureMap );
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( putRef );
			graph.releaseRef( sourceRef );
			graph.releaseRef( targetRef );
			graph.releaseRef( edgeRef );
		}
	}
}
