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

import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_DIMENSION_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_ISINT_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_SHORT_NAME_ATTRIBUTE;
import static org.mastodon.mamut.io.importer.trackmate.TrackMateXMLKeys.FEATURE_TAG;

import java.util.Arrays;
import java.util.Collections;
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

	/**
	 * List of Mastodon spot projection keys, translated to the TrackMate export
	 * name, that are not needed in the exported XML file, as they are already
	 * exported via TrackMate builtin features.
	 */
	public static final List< String > redundantSpotProjectionKeys = Arrays.asList(
			"Spot_position_X", "Spot_position_Y", "Spot_position_Z", "Spot_radius", "Spot_frame" );

	/**
	 * List of Mastodon link projection keys, translated to the TrackMate export
	 * name, that are not needed in the exported XML file, as they are already
	 * exported via TrackMate builtin features.
	 */
	public static final List< String > redundantLinkProjectionKeys = Arrays.asList(
			"Link_target_IDs_Source_spot_id", "Link_target_IDs_Target_spot_id" );

	/**
	 * List of Mastodon track projection keys, translated to the TrackMate
	 * export name, that are not needed in the exported XML file, as they are
	 * already exported via TrackMate builtin features.
	 */
	public static final List< String > redundantTrackProjectionKeys = Collections.emptyList();
}
