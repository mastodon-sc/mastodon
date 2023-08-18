/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.mamut.MamutBranchView.BRANCH_GRAPH;
import static org.mastodon.mamut.MamutView.COLORBAR_POSITION_KEY;
import static org.mastodon.mamut.MamutView.COLORBAR_VISIBLE_KEY;
import static org.mastodon.mamut.MamutView.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.MamutView.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutView.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutView.NO_COLORING_KEY;
import static org.mastodon.mamut.MamutView.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutView.TAG_SET_KEY;
import static org.mastodon.mamut.MamutViewBdv.BDV_STATE_KEY;
import static org.mastodon.mamut.MamutViewBdv.BDV_TRANSFORM_KEY;
import static org.mastodon.mamut.MamutViewGrapher.GRAPHER_TRANSFORM_KEY;
import static org.mastodon.mamut.MamutViewTable.TABLE_DISPLAYED;
import static org.mastodon.mamut.MamutViewTable.TABLE_ELEMENT;
import static org.mastodon.mamut.MamutViewTable.TABLE_NAME;
import static org.mastodon.mamut.MamutViewTable.TABLE_SELECTION_ONLY;
import static org.mastodon.mamut.MamutViewTable.TABLE_VISIBLE_POS;
import static org.mastodon.mamut.MamutViewTrackScheme.TRACKSCHEME_TRANSFORM_KEY;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JViewport;

import org.jdom2.Element;
import org.mastodon.app.IMastodonView;
import org.mastodon.app.ui.IMastodonFrameView;
import org.mastodon.mamut.MamutBranchViewBdv;
import org.mastodon.mamut.MamutBranchViewTrackScheme;
import org.mastodon.mamut.MamutView;
import org.mastodon.mamut.MamutViewBdv;
import org.mastodon.mamut.MamutViewGrapher;
import org.mastodon.mamut.MamutViewTable;
import org.mastodon.mamut.MamutViewTrackScheme;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.TrackSchemePanel;

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
	 * Key that specifies the name of the chosen context provider. Values are
	 * strings.
	 */
	static final String CHOSEN_CONTEXT_PROVIDER_KEY = "ContextProvider";

	/**
	 * Serializes a GUI state map into a XML element.
	 *
	 * @param guiState
	 *            the GUI state to serialize.
	 * @return a new XML element.
	 */
	static < V extends IMastodonFrameView & IMastodonView > Element toXml( final V view )
	{
		final Map< String, Object > guiState = getGuiState( view );
		final Element element = new Element( WINDOW_TAG );
		toXml( guiState, element );
		return element;
	}

	static void toXml( final Map< String, Object > map, final Element element )
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
	 * Wraps GUI state of a {@link MamutView} into a map.
	 * 
	 * @param view
	 *            the view.
	 * @return a new {@link Map}.
	 */
	private static < V extends IMastodonFrameView & IMastodonView > Map< String, Object > getGuiState( final V view )
	{
		final Map< String, Object > guiState = new LinkedHashMap<>();

		// View type.
		guiState.put( VIEW_TYPE_KEY, view.getClass().getSimpleName() );

		// Frame position and size.
		final Rectangle bounds = view.getFrame().getBounds();
		guiState.put( FRAME_POSITION_KEY, new int[] {
				( int ) bounds.getMinX(),
				( int ) bounds.getMinY(),
				( int ) bounds.getWidth(),
				( int ) bounds.getHeight() } );

		// Lock groups.
		guiState.put( GROUP_HANDLE_ID_KEY, view.getGroupHandle().getGroupId() );

		// Settings panel visibility.
		guiState.put( SETTINGS_PANEL_VISIBLE_KEY, view.getFrame().isSettingsPanelVisible() );

		// View-specifics.
		if ( view instanceof MamutViewBdv )
			getGuiStateBdv( ( MamutViewBdv ) view, guiState );
		else if ( view instanceof MamutBranchViewBdv )
			getGuiStateBranchBdv( ( MamutBranchViewBdv ) view, guiState );
		else if ( view instanceof MamutViewTrackScheme )
			getGuiStateTrackScheme( ( MamutViewTrackScheme ) view, guiState );
		else if ( view instanceof MamutBranchViewTrackScheme )
			getGuiStateBranchTrackScheme( ( MamutBranchViewTrackScheme ) view, guiState );
		else if ( view instanceof MamutViewTable )
			getGuiStateTable( ( MamutViewTable ) view, guiState );
		else if ( view instanceof MamutViewGrapher )
			getGuiStateGrapher( ( MamutViewGrapher ) view, guiState );

		return guiState;
	}

	private static void getGuiStateGrapher( final MamutViewGrapher view, final Map< String, Object > guiState )
	{
		final DataDisplayPanel< Spot, Link > dataDisplayPanel = view.getDataDisplayPanel();

		// Transform.
		final org.mastodon.views.grapher.datagraph.ScreenTransform t = dataDisplayPanel.getScreenTransform().get();
		guiState.put( GRAPHER_TRANSFORM_KEY, t );

		// Coloring.
		final ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );

		// Colorbar.
		final ColorBarOverlay colorBarOverlay = view.getColorBarOverlay();
		getColorBarOverlayState( colorBarOverlay, guiState );

		// Context provider.
		guiState.put( CHOSEN_CONTEXT_PROVIDER_KEY, view.getContextChooser().getChosenProvider().getName() );
	}

	/**
	 * Stores the {@link MamutViewTable} GUI state in the specified map.
	 * 
	 * @param view
	 *            the {@link MamutViewTable}.
	 * @param guiState
	 *            the map to store info into.
	 */
	private static void getGuiStateTable( final MamutViewTable view, final Map< String, Object > guiState )
	{
		// Selection table or not.
		guiState.put( TABLE_SELECTION_ONLY, view.isSelectionTable() );

		// Currently displayed table.
		final FeatureTagTablePanel< ? > currentlyDisplayedTable = view.getFrame().getCurrentlyDisplayedTable();
		String displayedTableName = "";

		// Table visible rectangles.
		final List< FeatureTagTablePanel< ? > > tables = view.getFrame().getTables();
		final List< String > names = view.getFrame().getTableNames();
		final List< Map< String, Object > > tableGuiStates = new ArrayList<>( names.size() );
		for ( int i = 0; i < names.size(); i++ )
		{
			final String name = names.get( i );
			final FeatureTagTablePanel< ? > table = tables.get( i );

			if ( table == currentlyDisplayedTable )
				displayedTableName = name;

			final JViewport viewportVertex = table.getScrollPane().getViewport();
			final Point tableRect = viewportVertex.getViewPosition();

			final LinkedHashMap< String, Object > tableGuiState = new LinkedHashMap<>();
			tableGuiState.put( TABLE_NAME, name );
			tableGuiState.put( TABLE_VISIBLE_POS, new int[] {
					tableRect.x,
					tableRect.y } );

			tableGuiStates.add( tableGuiState );
		}
		guiState.put( TABLE_ELEMENT, tableGuiStates );
		guiState.put( TABLE_DISPLAYED, displayedTableName );

		// Coloring for core graph.
		final ColoringModel coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );

		// Coloring for branch-graph.
		final ColoringModel branchColoringModel = view.getBranchColoringModel();
		final Map< String, Object > branchGraphMap = new HashMap<>();
		getColoringState( branchColoringModel, branchGraphMap );
		guiState.put( BRANCH_GRAPH, branchGraphMap );

		// Context provider.
		guiState.put( CHOSEN_CONTEXT_PROVIDER_KEY, view.getContextChooser().getChosenProvider().getName() );
	}

	/**
	 * Stores the {@link MamutViewTrackScheme} GUI state in the specified map.
	 * 
	 * @param view
	 *            the {@link MamutViewTrackScheme}.
	 * @param guiState
	 *            the map to store info into.
	 */
	private static void getGuiStateTrackScheme( final MamutViewTrackScheme view, final Map< String, Object > guiState )
	{
		final TrackSchemePanel trackschemePanel = view.getTrackschemePanel();

		// Edit position to reflect the fact that we store the TrackScheme panel
		// width and height.
		final Point point = view.getFrame().getLocation();
		guiState.put( FRAME_POSITION_KEY, new int[] {
				point.x,
				point.y,
				trackschemePanel.getDisplay().getWidth(),
				trackschemePanel.getDisplay().getHeight() } );

		// Transform.
		final ScreenTransform t = trackschemePanel.getScreenTransform().get();
		guiState.put( TRACKSCHEME_TRANSFORM_KEY, t );

		// Coloring.
		final ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );

		// Colorbar.
		final ColorBarOverlay colorBarOverlay = view.getColorBarOverlay();
		getColorBarOverlayState( colorBarOverlay, guiState );

		// Context provider.
		guiState.put( CHOSEN_CONTEXT_PROVIDER_KEY, view.getContextChooser().getChosenProvider().getName() );
	}

	/**
	 * Stores the {@link MamutBranchViewTrackScheme} GUI state in the specified
	 * map.
	 * 
	 * @param view
	 *            the {@link MamutBranchViewTrackScheme}.
	 * @param guiState
	 *            the map to store info into.
	 */
	private static void getGuiStateBranchTrackScheme( final MamutBranchViewTrackScheme view,
			final Map< String, Object > guiState )
	{
		final TrackSchemePanel trackschemePanel = view.getFrame().getTrackschemePanel();

		// Edit position to reflect the fact that we store the TrackScheme panel
		// width and height.
		final Point point = view.getFrame().getLocation();
		guiState.put( FRAME_POSITION_KEY, new int[] {
				point.x,
				point.y,
				trackschemePanel.getDisplay().getWidth(),
				trackschemePanel.getDisplay().getHeight() } );

		// Transform.
		final ScreenTransform t = trackschemePanel.getScreenTransform().get();
		guiState.put( TRACKSCHEME_TRANSFORM_KEY, t );

		// Coloring.
		final ColoringModel coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );

		// Colorbar.
		final ColorBarOverlay colorBarOverlay = view.getColorBarOverlay();
		getColorBarOverlayState( colorBarOverlay, guiState );
	}

	/**
	 * Stores the {@link MamutViewBdv} GUI state in the specified map.
	 * 
	 * @param view
	 *            the {@link MamutViewBdv}.
	 * @param guiState
	 *            the map to store info into.
	 */
	private static void getGuiStateBdv( final MamutViewBdv view, final Map< String, Object > guiState )
	{
		// Viewer state.
		final Element stateEl = view.getViewerPanelMamut().stateToXml();
		guiState.put( BDV_STATE_KEY, stateEl );
		// Transform.
		final AffineTransform3D t = new AffineTransform3D();
		view.getViewerPanelMamut().state().getViewerTransform( t );
		guiState.put( BDV_TRANSFORM_KEY, t );
		// Coloring.
		final ColoringModel coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );
		// Colorbar.
		final ColorBarOverlay colorBarOverlay = view.getColorBarOverlay();
		getColorBarOverlayState( colorBarOverlay, guiState );
	}

	/**
	 * Stores the {@link MamutBranchViewBdv} GUI state in the specified map.
	 * 
	 * @param view
	 *            the {@link MamutViewBdv}.
	 * @param guiState
	 *            the map to store info into.
	 */
	private static void getGuiStateBranchBdv( final MamutBranchViewBdv view, final Map< String, Object > guiState )
	{
		// Viewer state.
		final Element stateEl = view.getViewerPanelMamut().stateToXml();
		guiState.put( BDV_STATE_KEY, stateEl );
		// Transform.
		final AffineTransform3D t = new AffineTransform3D();
		view.getViewerPanelMamut().state().getViewerTransform( t );
		guiState.put( BDV_TRANSFORM_KEY, t );
		// Coloring.
		final ColoringModel coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );
		// Colorbar.
		final ColorBarOverlay colorBarOverlay = view.getColorBarOverlay();
		getColorBarOverlayState( colorBarOverlay, guiState );
	}

	/**
	 * Reads the coloring state of a view and stores it into the specified map.
	 * 
	 * @param coloringModel
	 *            the coloring model to read from.
	 * @param guiState
	 *            the map to store it to.
	 */
	private static void getColoringState( final ColoringModel coloringModel, final Map< String, Object > guiState )
	{
		final boolean noColoring = coloringModel.noColoring();
		guiState.put( NO_COLORING_KEY, noColoring );
		if ( !noColoring )
			if ( coloringModel.getTagSet() != null )
				guiState.put( TAG_SET_KEY, coloringModel.getTagSet().getName() );
			else if ( coloringModel.getFeatureColorMode() != null )
				guiState.put( FEATURE_COLOR_MODE_KEY, coloringModel.getFeatureColorMode().getName() );
	}

	private static void getColorBarOverlayState( final ColorBarOverlay colorBarOverlay,
			final Map< String, Object > guiState )
	{
		guiState.put( COLORBAR_VISIBLE_KEY, colorBarOverlay.isVisible() );
		guiState.put( COLORBAR_POSITION_KEY, colorBarOverlay.getPosition() );
	}

	/**
	 * Deserializes a GUI state from XML and recreate view windows as specified.
	 * 
	 * @param windowsEl
	 *            the XML element that stores the GUI state of a view.
	 * @param windowManager
	 *            the application {@link WindowManager}.
	 */
	static void fromXml( final Element windowsEl, final WindowManager windowManager )
	{
		// To deal later with context providers.
		final Map< String, ContextProvider< Spot > > contextProviders = new HashMap<>();
		final Map< ContextChooser< Spot >, String > contextChosers = new HashMap<>();

		final List< Element > viewEls = windowsEl.getChildren( WINDOW_TAG );
		for ( final Element viewEl : viewEls )
		{
			final Map< String, Object > guiState = xmlToMap( viewEl );
			final String typeStr = ( String ) guiState.get( VIEW_TYPE_KEY );
			switch ( typeStr )
			{
			case "MamutViewBdv":
			{
				try
				{
					final MamutViewBdv bdv = windowManager.createBigDataViewer( guiState );

					// Store context provider.
					contextProviders.put( bdv.getContextProvider().getName(), bdv.getContextProvider() );
				}
				catch ( final IllegalArgumentException iae )
				{
					System.err.println( "Info: Failed restoring state of a BigDataViewer window, thus not showing it.\n"
							+ "      You may want to resave your project to replace the previous (failing) state with the current (okay) state." );
				}
				break;
			}

			case "MamutBranchViewBdv":
			{
				windowManager.createBranchBigDataViewer( guiState );
				break;
			}

			case "MamutViewTrackScheme":
			{
				final MamutViewTrackScheme ts = windowManager.createTrackScheme( guiState );

				// Deal with context chooser.
				final String desiredProvider = ( String ) guiState.get( CHOSEN_CONTEXT_PROVIDER_KEY );
				if ( null != desiredProvider )
					contextChosers.put( ts.getContextChooser(), desiredProvider );
				break;
			}

			case "MamutBranchViewTrackScheme":
			{
				windowManager.createBranchTrackScheme( guiState );
				break;
			}

			case "MamutBranchViewTrackSchemeHierarchy":
			{
				windowManager.createHierarchyTrackScheme( guiState );
				break;
			}

			case "MamutViewTable":
			{
				final MamutViewTable table = windowManager.createTable( guiState );

				// Deal with context chooser.
				final String desiredProvider = ( String ) guiState.get( CHOSEN_CONTEXT_PROVIDER_KEY );
				if ( null != desiredProvider )
					contextChosers.put( table.getContextChooser(), desiredProvider );
				break;
			}

			case "MamutViewGrapher":
			{
				final MamutViewGrapher grapher = windowManager.createGrapher( guiState );

				// Deal with context chooser.
				final String desiredProvider = ( String ) guiState.get( CHOSEN_CONTEXT_PROVIDER_KEY );
				if ( null != desiredProvider )
					contextChosers.put( grapher.getContextChooser(), desiredProvider );
				break;
			}

			default:
				System.err.println( "Deserializing GUI state: Unknown window type: " + typeStr + "." );
				continue;
			}
		}

		/*
		 * Loop again on context choosers and try to give them their desired
		 * context provider.
		 */

		for ( final ContextChooser< Spot > contextChooser : contextChosers.keySet() )
		{
			final String desiredContextProvider = contextChosers.get( contextChooser );
			final ContextProvider< Spot > contextProvider = contextProviders.get( desiredContextProvider );
			if ( null != contextProvider )
				contextChooser.choose( contextProvider );
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
			case TABLE_SELECTION_ONLY:
			case NO_COLORING_KEY:
			case SETTINGS_PANEL_VISIBLE_KEY:
			case COLORBAR_VISIBLE_KEY:
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
				System.err.println( "Unknown GUI config parameter: " + key + " found in GUI file." );
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
