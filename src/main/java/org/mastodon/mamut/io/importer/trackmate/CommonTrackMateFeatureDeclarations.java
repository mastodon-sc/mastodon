package org.mastodon.mamut.io.importer.trackmate;

import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_SHORT_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_TAG;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

public class CommonTrackMateFeatureDeclarations
{

	public static final List< CommonTrackMateFeatureDeclaration > spotFeatureDeclarations = Arrays.asList( new CommonTrackMateFeatureDeclaration[] {
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.VISIBILITY_FEATURE_NAME, "Visibility", "Visibility", "NONE", true ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.POSITION_X_FEATURE_NAME, "X", "X", "POSITION", false ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.POSITION_Y_FEATURE_NAME, "Y", "Y", "POSITION", false ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.POSITION_Z_FEATURE_NAME, "Z", "Z", "POSITION", false ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.POSITION_T_FEATURE_NAME, "T", "T", "TIME", false ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.FRAME_FEATURE_NAME, "Frame", "Frame", "NONE", true ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.RADIUS_FEATURE_NAME, "Radius", "R", "LENGTH", false ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.QUALITY_FEATURE_NAME, "Quality", "Quality", "QUALITY", false ),
	} );

	public static final List< CommonTrackMateFeatureDeclaration > edgeFeatureDeclarations = Arrays.asList( new CommonTrackMateFeatureDeclaration[] {
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.EDGE_SOURCE_ATTRIBUTE, "Source spot ID", "Source ID", "NONE", true ),
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.EDGE_TARGET_ATTRIBUTE, "Target spot ID", "Target ID", "NONE", true ),
	} );

	public static final List< CommonTrackMateFeatureDeclaration > trackFeatureDeclarations = Arrays.asList( new CommonTrackMateFeatureDeclaration[] {
			new CommonTrackMateFeatureDeclaration( TrackMateXMLKeys.TRACK_ID_ATTRIBUTE, "Track ID", "ID", "NONE", true ),
	} );

	public static class CommonTrackMateFeatureDeclaration
	{
		public final String key;

		public final String name;

		public final String shortName;

		public final String dimension;

		public final boolean isInt;

		public CommonTrackMateFeatureDeclaration( final String key, final String name, final String shortName, final String dimension, final boolean isInt )
		{
			this.key = key;
			this.name = name;
			this.shortName = shortName;
			this.dimension = dimension;
			this.isInt = isInt;
		}
		
		public Element toElement()
		{
			final Element fel = new Element( FEATURE_TAG );
			fel.setAttribute( FEATURE_ATTRIBUTE, key );
			fel.setAttribute( FEATURE_NAME_ATTRIBUTE, name );
			fel.setAttribute( FEATURE_SHORT_NAME_ATTRIBUTE, shortName );
			fel.setAttribute( FEATURE_DIMENSION_ATTRIBUTE, dimension );
			fel.setAttribute( FEATURE_ISINT_ATTRIBUTE, "" + isInt );
			return fel;
		}
	}
}
