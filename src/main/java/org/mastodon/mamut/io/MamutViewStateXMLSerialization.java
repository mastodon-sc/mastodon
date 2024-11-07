/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io;

import static org.mastodon.mamut.views.MamutBranchView.BRANCH_GRAPH;
import static org.mastodon.mamut.views.MamutView.COLORBAR_POSITION_KEY;
import static org.mastodon.mamut.views.MamutView.COLORBAR_VISIBLE_KEY;
import static org.mastodon.mamut.views.MamutView.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.views.MamutView.FRAME_POSITION_KEY;
import static org.mastodon.mamut.views.MamutView.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.views.MamutView.NO_COLORING_KEY;
import static org.mastodon.mamut.views.MamutView.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.views.MamutView.TAG_SET_KEY;
import static org.mastodon.mamut.views.MamutView.TRACK_COLORING_KEY;
import static org.mastodon.mamut.views.MamutViewFactory.VIEW_TYPE_KEY;
import static org.mastodon.mamut.views.bdv.MamutViewBdvFactory.BDV_STATE_KEY;
import static org.mastodon.mamut.views.bdv.MamutViewBdvFactory.BDV_TRANSFORM_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_TRANSFORM_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_X_AXIS_FEATURE_IS_EDGE_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_X_AXIS_FEATURE_SPEC_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_X_AXIS_INCOMING_EDGE_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_X_AXIS_FEATURE_PROJECTION_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_SHOW_EDGES_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_Y_AXIS_FEATURE_IS_EDGE_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_Y_AXIS_FEATURE_SPEC_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_Y_AXIS_INCOMING_EDGE_KEY;
import static org.mastodon.mamut.views.grapher.GrapherGuiState.GRAPHER_Y_AXIS_FEATURE_PROJECTION_KEY;
import static org.mastodon.mamut.views.table.MamutViewTableFactory.TABLE_DISPLAYED;
import static org.mastodon.mamut.views.table.MamutViewTableFactory.TABLE_ELEMENT;
import static org.mastodon.mamut.views.table.MamutViewTableFactory.TABLE_NAME;
import static org.mastodon.mamut.views.table.MamutViewTableFactory.TABLE_VISIBLE_POS;
import static org.mastodon.mamut.views.trackscheme.MamutViewTrackSchemeFactory.TRACKSCHEME_TRANSFORM_KEY;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;
import org.mastodon.mamut.MamutViews;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.views.MamutViewI;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.views.trackscheme.ScreenTransform;

import mpicbg.spim.data.XmlHelpers;
import net.imglib2.realtransform.AffineGet;

/**
 * Utility class that can transform a GUI state
 * <code>Map&lt; String, Object &gt;</code> to XML and vice versa.
 */
public class MamutViewStateXMLSerialization
{

	private static final String WINDOW_TAG = "Window";

	/**
	 * Key that specifies the name of the chosen context provider. Values are
	 * strings.
	 * 
	 * FIXME Right now the chosen context provider is not serialized or
	 * deserialized. Deserializing it will be a bit tricky: when we recreate
	 * views, it is possible that a context chooser it set on a context provider
	 * that has not been created yet.
	 */
	private static final String CHOSEN_CONTEXT_PROVIDER_KEY = "ContextProvider";

	public static Element toXml( final Map< String, Object > guiState )
	{
		final Element element = new Element( WINDOW_TAG );
		toXml( guiState, element );
		return element;
	}

	private static void toXml( final Map< String, Object > map, final Element element )
	{
		for ( final Entry< String, Object > entry : map.entrySet() )
		{
			final Element el = toXml( entry.getKey(), entry.getValue() );
			element.addContent( el );
		}
	}

	@SuppressWarnings( "unchecked" )
	static Element toXml( final String key, final Object value )
	{
		final Element el;
		if ( value instanceof Integer )
			el = XmlHelpers.intElement( key, ( Integer ) value );
		else if ( value instanceof int[] )
			el = XmlHelpers.intArrayElement( key, ( int[] ) value );
		else if ( value instanceof Double )
			el = XmlHelpers.doubleElement( key, ( Double ) value );
		else if ( value instanceof double[] )
			el = XmlHelpers.doubleArrayElement( key, ( double[] ) value );
		else if ( value instanceof AffineGet )
			el = XmlHelpers.affineTransform3DElement( key, ( AffineGet ) value );
		else if ( value instanceof Boolean )
			el = XmlHelpers.booleanElement( key, ( Boolean ) value );
		else if ( value instanceof String )
		{
			el = new Element( key );
			el.setText( value.toString() );
		}
		else if ( value instanceof ScreenTransform )
		{
			final ScreenTransform t = ( ScreenTransform ) value;
			el = XmlHelpers.doubleArrayElement( key, new double[] {
					t.getMinX(),
					t.getMaxX(),
					t.getMinY(),
					t.getMaxY(),
					t.getScreenWidth(),
					t.getScreenHeight()
			} );
		}
		else if ( value instanceof org.mastodon.views.grapher.datagraph.ScreenTransform )
		{
			final org.mastodon.views.grapher.datagraph.ScreenTransform t =
					( org.mastodon.views.grapher.datagraph.ScreenTransform ) value;
			el = XmlHelpers.doubleArrayElement( key, new double[] {
					t.getMinX(),
					t.getMaxX(),
					t.getMinY(),
					t.getMaxY(),
					t.getScreenWidth(),
					t.getScreenHeight()
			} );
		}
		else if ( value instanceof Position )
		{
			el = new Element( key );
			el.setText( ( ( Position ) value ).name() );
		}
		else if ( value instanceof Element )
		{
			el = new Element( key );
			el.setContent( ( Element ) value );
		}
		else if ( value instanceof Map )
		{
			el = new Element( key );
			toXml( ( Map< String, Object > ) value, el );
		}
		else if ( value instanceof List )
		{
			el = new Element( key );
			final List< Object > os = ( List< Object > ) value;
			for ( final Object o : os )
			{
				final Element child = toXml( key, o );
				el.addContent( child );
			}
		}
		else
		{
			System.err.println( "Do not know how to serialize object " + value + " for key " + key + "." );
			el = null;
		}
		return el;
	}

	/**
	 * Deserializes a GUI state from XML and recreate view windows as specified.
	 * 
	 * @param windowsEl
	 *            the XML element that stores the GUI state of a view.
	 * @param windowManager
	 *            the application {@link WindowManager}.
	 */
	public static void fromXml( final Element windowsEl, final WindowManager windowManager )
	{
		final MamutViews viewFactories = windowManager.getViewFactories();
		final Collection< Class< ? extends MamutViewI > > classes = viewFactories.getKeys();

		final List< Element > viewEls = windowsEl.getChildren( WINDOW_TAG );
		for ( final Element viewEl : viewEls )
		{
			final Map< String, Object > guiState = xmlToMap( viewEl );
			final String typeStr = ( String ) guiState.get( VIEW_TYPE_KEY );

			// First check that we know of the view type in the window manager.
			Class< ? extends MamutViewI > klass = null;
			for ( final Class< ? extends MamutViewI > cl : classes )
			{
				if ( cl.getSimpleName().equals( typeStr ) )
				{
					klass = cl;
					break;
				}
			}
			if ( klass == null )
			{
				System.err.println( "Deserializing GUI state: Unknown view type: " + typeStr + "." );
				continue;
			}

			// Create, register the view and sets its GUI state.
			windowManager.createView( klass, guiState );
		}
	}

	private static Map< String, Object > xmlToMap( final Element viewEl )
	{
		final Map< String, Object > guiState = new HashMap<>();
		final List< Element > children = viewEl.getChildren();
		for ( final Element el : children )
		{
			final String key = el.getName();
			final Object value;
			switch ( key )
			{
			case BDV_STATE_KEY:
				value = el;
				break;
			case BDV_TRANSFORM_KEY:
				value = XmlHelpers.getAffineTransform3D( viewEl, key );
				break;
			case FRAME_POSITION_KEY:
				final int[] pos = XmlHelpers.getIntArray( viewEl, key );
				value = sanitize( pos );
				break;
			case TAG_SET_KEY:
			case FEATURE_COLOR_MODE_KEY:
			case VIEW_TYPE_KEY:
			case CHOSEN_CONTEXT_PROVIDER_KEY:
			case GRAPHER_X_AXIS_FEATURE_SPEC_KEY:
			case GRAPHER_X_AXIS_FEATURE_PROJECTION_KEY:
			case GRAPHER_Y_AXIS_FEATURE_SPEC_KEY:
			case GRAPHER_Y_AXIS_FEATURE_PROJECTION_KEY:
				value = el.getTextTrim();
				break;
			case TRACKSCHEME_TRANSFORM_KEY:
			{
				final double[] arr = XmlHelpers.getDoubleArray( viewEl, key );
				value = new ScreenTransform( arr[ 0 ], arr[ 1 ], arr[ 2 ], arr[ 3 ], ( int ) arr[ 4 ],
						( int ) arr[ 5 ] );
				break;
			}
			case GRAPHER_TRANSFORM_KEY:
			{
				final double[] arr = XmlHelpers.getDoubleArray( viewEl, key );
				value = new org.mastodon.views.grapher.datagraph.ScreenTransform( arr[ 0 ], arr[ 1 ], arr[ 2 ],
						arr[ 3 ], ( int ) arr[ 4 ], ( int ) arr[ 5 ] );
				break;
			}
			case NO_COLORING_KEY:
			case SETTINGS_PANEL_VISIBLE_KEY:
			case COLORBAR_VISIBLE_KEY:
			case GRAPHER_SHOW_EDGES_KEY:
			case GRAPHER_X_AXIS_FEATURE_IS_EDGE_KEY:
			case GRAPHER_X_AXIS_INCOMING_EDGE_KEY:
			case GRAPHER_Y_AXIS_FEATURE_IS_EDGE_KEY:
			case GRAPHER_Y_AXIS_INCOMING_EDGE_KEY:
			case TRACK_COLORING_KEY:
				value = XmlHelpers.getBoolean( viewEl, key );
				break;
			case COLORBAR_POSITION_KEY:
				final String str = XmlHelpers.getText( viewEl, key );
				value = Position.valueOf( str );
				break;
			case GROUP_HANDLE_ID_KEY:
			{
				value = XmlHelpers.getInt( viewEl, key );
				break;
			}
			case TABLE_ELEMENT:
			{
				final List< Element > els = el.getChildren();
				final List< Map< String, Object > > maps = new ArrayList<>( els.size() );
				for ( final Element child : els )
				{
					final String name = child.getChildTextTrim( TABLE_NAME );
					final int[] tablePos = XmlHelpers.getIntArray( child, TABLE_VISIBLE_POS );
					final Map< String, Object > m = new HashMap<>();
					m.put( TABLE_NAME, name );
					m.put( TABLE_VISIBLE_POS, tablePos );
					maps.add( m );
				}
				value = maps;
				break;
			}
			case TABLE_DISPLAYED:
				value = XmlHelpers.getText( viewEl, TABLE_DISPLAYED );
				break;
			case BRANCH_GRAPH:
				value = xmlToMap( el );
				break;
			default:
				System.out.println( "Unknown GUI config parameter: " + key + " found in GUI file." );
				continue;
			}
			guiState.put( key, value );
		}
		return guiState;
	}

	private static final int MIN_WIDTH = 200;

	private static final int MIN_HEIGHT = MIN_WIDTH;

	/**
	 * Makes sure the specified position array won't end in creating windows
	 * off-screen. We impose that a window is fully on *one* screen and not
	 * split over severals. We also impose a minimal size for the windows.
	 * <p>
	 * The pos array is { x, y, width, height }.
	 * 
	 * @param pos
	 *            the position array.
	 * @return the same position array.
	 */
	private static int[] sanitize( final int[] pos )
	{
		assert pos.length == 4;
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if ( null == ge )
			return pos;
		final GraphicsDevice sd[] = ge.getScreenDevices();
		if ( sd.length < 1 )
			return pos;

		// Window min size.
		pos[ 2 ] = Math.max( MIN_WIDTH, pos[ 2 ] );
		pos[ 3 ] = Math.max( MIN_HEIGHT, pos[ 3 ] );

		for ( final GraphicsDevice gd : sd )
		{
			final Rectangle bounds = gd.getDefaultConfiguration().getBounds();
			if ( bounds.contains( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] ) )
				// Fully in a screen, nothing to do.
				return pos;

			if ( bounds.contains( pos[ 0 ], pos[ 1 ] ) )
			{
				/*
				 * This window is on this screen, but exits it. First resize it
				 * so that it is not bigger than the screen.
				 */
				pos[ 2 ] = Math.min( bounds.width, pos[ 2 ] );
				pos[ 3 ] = Math.min( bounds.height, pos[ 3 ] );

				/*
				 * Then move it back so that its bottom right corner is in the
				 * screen.
				 */
				if ( pos[ 0 ] + pos[ 2 ] > bounds.x + bounds.width )
					pos[ 0 ] -= ( pos[ 0 ] - bounds.x + pos[ 2 ] - bounds.width );

				if ( pos[ 1 ] + pos[ 3 ] > bounds.y + bounds.height )
					pos[ 1 ] -= ( pos[ 1 ] - bounds.y + pos[ 3 ] - bounds.height );

				return pos;
			}
		}

		/*
		 * Ok we did not find a screen in which this window is. So we will put
		 * it in the first screen.
		 */
		final Rectangle bounds = sd[ 0 ].getDefaultConfiguration().getBounds();
		pos[ 0 ] = Math.max( bounds.x,
				Math.min( bounds.x + bounds.width - pos[ 2 ], pos[ 0 ] ) );
		pos[ 1 ] = Math.max( bounds.y,
				Math.min( bounds.y + bounds.height - pos[ 3 ], pos[ 1 ] ) );

		if ( bounds.contains( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] ) )
			// Fully in a screen, nothing to do.
			return pos;

		/*
		 * This window is on this screen, but exits it. First resize it so that
		 * it is not bigger than the screen.
		 */
		pos[ 2 ] = Math.min( bounds.width, pos[ 2 ] );
		pos[ 3 ] = Math.min( bounds.height, pos[ 3 ] );

		/*
		 * Then move it back so that its bottom right corner is in the screen.
		 */
		pos[ 0 ] -= ( pos[ 0 ] - bounds.x + pos[ 2 ] - bounds.width );
		pos[ 1 ] -= ( pos[ 1 ] - bounds.y + pos[ 3 ] - bounds.height );

		return pos;
	}
}
