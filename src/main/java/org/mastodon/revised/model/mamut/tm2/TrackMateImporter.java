package org.mastodon.revised.model.mamut.tm2;

import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.FRAME_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.ID_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.LABEL_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.MODEL_TAG;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.RADIUS_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.SPOT_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.SPOT_ELEMENT_TAG;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.SPOT_FRAME_COLLECTION_TAG;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.VISIBILITY_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.X_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.Y_ATTRIBUTE_NAME;
import static org.mastodon.revised.model.mamut.tm2.TrackMateXMLKeys.Z_ATTRIBUTE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefCollections;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

import mpicbg.spim.data.XmlHelpers;

public class TrackMateImporter
{
	public static void importModel( final File file ) throws JDOMException, IOException
	{
		final SAXBuilder sb = new SAXBuilder();
		final Document document = sb.build( file );
		final Element root = document.getRootElement();

		final Element modelEl = root.getChild( MODEL_TAG );
		if ( null == modelEl ) { return; }

		final ModelGraph model = new ModelGraph();

		final Spot ref = model.vertexRef();
		final Spot putRef = model.vertexRef();
		final double[] pos = new double[ 3 ];

		final IntRefMap< Spot > idToSpotIDmap = RefCollections.createIntRefMap( model.vertices(), -1 );

		final Element allSpotsEl = modelEl.getChild( SPOT_COLLECTION_TAG );
		final List< Element > allFramesEl = allSpotsEl.getChildren( SPOT_FRAME_COLLECTION_TAG );
		for ( final Element frameEl : allFramesEl )
		{
			final List< Element > frameSpotsEl = frameEl.getChildren( SPOT_ELEMENT_TAG );
			for ( final Element spotEl : frameSpotsEl )
			{
				final boolean visible = XmlHelpers.getBoolean( spotEl, VISIBILITY_ATTRIBUTE_NAME );
				if ( !visible )
					continue;

				pos[ 0 ] = XmlHelpers.getDouble( spotEl, X_ATTRIBUTE_NAME );
				pos[ 1 ] = XmlHelpers.getDouble( spotEl, Y_ATTRIBUTE_NAME );
				pos[ 2 ] = XmlHelpers.getDouble( spotEl, Z_ATTRIBUTE_NAME );
				final double radius = XmlHelpers.getDouble( spotEl, RADIUS_ATTRIBUTE_NAME );
				final int frame = XmlHelpers.getInt( spotEl, FRAME_ATTRIBUTE_NAME );
				final int id = XmlHelpers.getInt( spotEl, ID_ATTRIBUTE_NAME );
				final String label = XmlHelpers.getText( spotEl, LABEL_ATTRIBUTE_NAME );

				final Spot spot = model.addVertex( ref ).init( frame, pos, radius );
				spot.setLabel( label );
				idToSpotIDmap.put( id, spot, putRef );

			}

		}


	}
}
