package org.mastodon.app.ui;

import org.mastodon.revised.trackscheme.ScreenTransform;

import net.imglib2.realtransform.AffineTransform3D;

/**
 * Collection of constants and utilities related to de/serializing a GUI state.
 */
public class MastodonViewStateSerialization
{

	public static final String WINDOW_TAG = "Window";

	/**
	 * Key to the view type name. Value is a string.
	 */
	public static final String VIEW_TYPE_KEY = "Type";

	/**
	 * Key to the parameter that stores the frame position for
	 * {@link MastodonFrameView}s. Value is and <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 */
	public static final String FRAME_POSITION_KEY = "FramePosition";

	/**
	 * Key for the transform in a BDV view. Value is an
	 * {@link AffineTransform3D} instance.
	 */
	public static final String BDV_TRANSFORM_KEY = "BdvTransform";

	/**
	 * Key for the time-point displayed in the view. Values is a single
	 * <code>int</code> value.
	 */
	public static final String TIMEPOINT_KEY = "CurrentTimepoint";

	/**
	 * Key for the group displayed in a BDV view. Value is a single
	 * <code>int </code> values.
	 */
	public static final String GROUP_KEY = "CurrentGroup";

	/**
	 * Key for the source displayed in a BDV view. Value is a single
	 * <code>int</code> values.
	 */
	public static final String SOURCE_KEY = "CurrentSource";

	/**
	 * Key for the interpolation mode used to display pixels in BDV. Value is a
	 * string with the interpolation mode name.
	 */
	public static final String INTERPOLATION_KEY = "Interpolation";

	/**
	 * Key for the display mode in BDV. Value is a string with the display mode
	 * name.
	 */
	public static final String DISPLAY_MODE_KEY = "DisplayMode";

	/**
	 * Key for the transform in a TrackScheme view. Value is a
	 * {@link ScreenTransform} instance.
	 */
	public static final String TRACKSCHEME_TRANSFORM_KEY = "TrackSchemeTransform";

	/**
	 * Key that specifies whether we do not use a special coloring scheme on the
	 * view. If <code>true</code>, then we do not use a special coloring scheme.
	 * 
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 */
	public static final String NO_COLORING_KEY = "NoColoring";

	/**
	 * Key that specifies the name of the tag-set to use for coloring scheme
	 * based on tag-sets. A non-<code>null</code> value means the coloring
	 * scheme is based on tag-sets.
	 * 
	 * @see #NO_COLORING_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 */
	public static final String TAG_SET_KEY = "TagSet";

	/**
	 * Key that specifies the name of the feature color mode to use for coloring
	 * scheme based on feature color modes. A non-<code>null</code> value means
	 * the coloring scheme is based on feature values.
	 * 
	 * @see #NO_COLORING_KEY
	 * @see #TAG_SET_KEY
	 */
	public static final String FEATURE_COLOR_MODE_KEY = "FeatureColorMode";

}
