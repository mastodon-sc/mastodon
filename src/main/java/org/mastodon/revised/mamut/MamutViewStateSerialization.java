package org.mastodon.revised.mamut;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;
import org.mastodon.app.ui.MastodonFrameView;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.ui.coloring.ColoringModel;

import bdv.viewer.state.ViewerState;
import mpicbg.spim.data.XmlHelpers;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Collection of constants and utilities related to de/serializing a GUI state.
 */
class MamutViewStateSerialization
{

	 static final String WINDOW_TAG = "Window";

	/**
	 * Key to the view type name. Value is a string.
	 */
	 static final String VIEW_TYPE_KEY = "Type";

	/**
	 * Key to the parameter that stores the frame position for
	 * {@link MastodonFrameView}s. Value is and <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 */
	 static final String FRAME_POSITION_KEY = "FramePosition";

	/**
	 * Key for the transform in a BDV view. Value is an
	 * {@link AffineTransform3D} instance.
	 */
	 static final String BDV_TRANSFORM_KEY = "BdvTransform";

	/**
	 * Key for the time-point displayed in the view. Values is a single
	 * <code>int</code> value.
	 */
	 static final String TIMEPOINT_KEY = "CurrentTimepoint";

	/**
	 * Key for the group displayed in a BDV view. Value is a single
	 * <code>int </code> values.
	 */
	 static final String GROUP_KEY = "CurrentGroup";

	/**
	 * Key for the source displayed in a BDV view. Value is a single
	 * <code>int</code> values.
	 */
	 static final String SOURCE_KEY = "CurrentSource";

	/**
	 * Key for the interpolation mode used to display pixels in BDV. Value is a
	 * string with the interpolation mode name.
	 */
	 static final String INTERPOLATION_KEY = "Interpolation";

	/**
	 * Key for the display mode in BDV. Value is a string with the display mode
	 * name.
	 */
	 static final String DISPLAY_MODE_KEY = "DisplayMode";

	/**
	 * Key for the transform in a TrackScheme view. Value is a
	 * {@link ScreenTransform} instance.
	 */
	 static final String TRACKSCHEME_TRANSFORM_KEY = "TrackSchemeTransform";

	/**
	 * Key that specifies whether we do not use a special coloring scheme on the
	 * view. If <code>true</code>, then we do not use a special coloring scheme.
	 *
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 */
	 static final String NO_COLORING_KEY = "NoColoring";

	/**
	 * Key that specifies the name of the tag-set to use for coloring scheme
	 * based on tag-sets. A non-<code>null</code> value means the coloring
	 * scheme is based on tag-sets.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 */
	 static final String TAG_SET_KEY = "TagSet";

	/**
	 * Key that specifies the name of the feature color mode to use for coloring
	 * scheme based on feature color modes. A non-<code>null</code> value means
	 * the coloring scheme is based on feature values.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #TAG_SET_KEY
	 */
	 static final String FEATURE_COLOR_MODE_KEY = "FeatureColorMode";

	/**
	 * Serializes a GUI state map into a XML element.
	 *
	 * @param guiState
	 *            the GUI state to serialize.
	 * @return a new XML element.
	 */

	static Element toXml( final MamutView< ?, ?, ? > view )
	{
		final Map< String, Object > guiState = getGuiState( view );

		final Element element = new Element( WINDOW_TAG );
		for ( final Entry< String, Object > entry : guiState.entrySet() )
		{
			final Object value = entry.getValue();
			final Element el;
			if ( value instanceof Integer )
				el = XmlHelpers.intElement( entry.getKey(), ( Integer ) entry.getValue() );
			else if ( value instanceof int[] )
				el = XmlHelpers.intArrayElement( entry.getKey(), ( int[] ) entry.getValue() );
			else if ( value instanceof Double )
				el = XmlHelpers.doubleElement( entry.getKey(), ( Double ) entry.getValue() );
			else if ( value instanceof double[] )
				el = XmlHelpers.doubleArrayElement( entry.getKey(), ( double[] ) entry.getValue() );
			else if ( value instanceof AffineGet )
				el = XmlHelpers.affineTransform3DElement( entry.getKey(), ( AffineGet ) entry.getValue() );
			else if ( value instanceof Boolean )
				el = XmlHelpers.booleanElement( entry.getKey(), ( Boolean ) value );
			else if ( value instanceof String )
			{
				el = new Element( entry.getKey() );
				el.setText( value.toString() );
			}
			else if ( value instanceof ScreenTransform )
			{
				final ScreenTransform t = ( ScreenTransform ) value;
				el = XmlHelpers.doubleArrayElement( entry.getKey(), new double[] {
						t.getMinX(),
						t.getMaxX(),
						t.getMinY(),
						t.getMaxY(),
						t.getScreenWidth(),
						t.getScreenHeight()
				} );
			}
			else
			{
				System.err.println( "Do not know how to serialize object " + value + " for key " + entry.getKey() + "." );
				continue;
			}
			element.addContent( el );
		}
		return element;
	}

	private static Map< String, Object > getGuiState( final MamutView< ?, ?, ? > view )
	{
		final Map< String, Object > guiState = new LinkedHashMap<>();
		guiState.put( VIEW_TYPE_KEY, view.getClass().getSimpleName() );
		final Rectangle bounds = view.getFrame().getBounds();
		guiState.put( FRAME_POSITION_KEY, new int[] {
				( int ) bounds.getMinX(),
				( int ) bounds.getMinY(),
				( int ) bounds.getWidth(),
				( int ) bounds.getHeight() } );

		if (view instanceof MamutViewBdv)
			getGuiStateBdv( (MamutViewBdv) view, guiState);
		else if (view instanceof MamutViewTrackScheme)
			getGuiStateTrackScheme( (MamutViewTrackScheme) view, guiState);

		return guiState;
	}

	private static void getGuiStateTrackScheme( final MamutViewTrackScheme view, final Map< String, Object > guiState )
	{
		final TrackSchemePanel trackschemePanel = view.getTrackschemePanel();
		final ColoringModel coloringModel = view.getColoringModel();
		final ScreenTransform t = trackschemePanel.getTransformEventHandler().getTransform().copy();
		guiState.put( TRACKSCHEME_TRANSFORM_KEY, t );
		// Coloring.
		final boolean noColoring = coloringModel.noColoring();
		guiState.put( NO_COLORING_KEY, noColoring );
		if ( !noColoring )
			if ( coloringModel.getTagSet() != null )
				guiState.put( TAG_SET_KEY, coloringModel.getTagSet().getName() );
			else if ( coloringModel.getFeatureColorMode() != null )
				guiState.put( FEATURE_COLOR_MODE_KEY, coloringModel.getFeatureColorMode().getName() );
	}

	private static void getGuiStateBdv( final MamutViewBdv view, final Map< String, Object > guiState )
	{
		final ViewerState state = view.getViewer().getState();
		guiState.put( TIMEPOINT_KEY, state.getCurrentTimepoint() );
		guiState.put( GROUP_KEY, state.getCurrentGroup() );
		guiState.put( SOURCE_KEY, state.getCurrentSource() );
		guiState.put( INTERPOLATION_KEY, state.getInterpolation().name() );
		guiState.put( DISPLAY_MODE_KEY, state.getDisplayMode().name() );
		final AffineTransform3D t = new AffineTransform3D();
		state.getViewerTransform( t );
		guiState.put( BDV_TRANSFORM_KEY, t );
	}

	static void fromXml( final Element windowsEl, final WindowManager windowManager )
	{
		final List< Element > viewEls = windowsEl.getChildren( WINDOW_TAG );
		for ( final Element viewEl : viewEls )
		{
			final Map< String, Object > guiState = xmlToMap( viewEl );
			final String typeStr = ( String ) guiState.get( VIEW_TYPE_KEY );
			switch ( typeStr  )
			{
			case "MamutViewBdv":
			{
				windowManager.createBigDataViewer( guiState);
				break;
			}

			case "MamutViewTrackScheme":
			{
				windowManager.createTrackScheme( guiState);
				break;
			}

			default:
				System.err.println( "Unknown window type: " + typeStr + "." );
				continue;
			}
		}
	}

	private static Map<String, Object> xmlToMap( final Element viewEl)
	{
		final Map< String, Object > guiState = new HashMap<>();
		final List< Element > children = viewEl.getChildren();
		for ( final Element el : children )
		{
			final String key = el.getName();
			final Object value;
			switch ( key )
			{
			case BDV_TRANSFORM_KEY:
				value = XmlHelpers.getAffineTransform3D( viewEl, key );
				break;
			case FRAME_POSITION_KEY:
				value = XmlHelpers.getIntArray( viewEl, key );
				break;
			case TIMEPOINT_KEY:
			case GROUP_KEY:
			case SOURCE_KEY:
				value = XmlHelpers.getInt( viewEl, key );
				break;
			case INTERPOLATION_KEY:
			case DISPLAY_MODE_KEY:
			case TAG_SET_KEY:
			case FEATURE_COLOR_MODE_KEY:
			case VIEW_TYPE_KEY:
				value = el.getTextTrim();
				break;
			case TRACKSCHEME_TRANSFORM_KEY:
				final double[] arr = XmlHelpers.getDoubleArray( viewEl, key );
				value = new ScreenTransform( arr[ 0 ], arr[ 1 ], arr[ 2 ], arr[ 3 ], ( int ) arr[ 4 ], ( int ) arr[ 5 ] );
				break;
			case NO_COLORING_KEY:
				value = XmlHelpers.getBoolean( viewEl, key );
				break;
			default:
				System.err.println( "Unkown GUI config parameter: " + key + " found in GUI file." );
				continue;
			}
			guiState.put( key, value );
		}
		return guiState;
	}
}
