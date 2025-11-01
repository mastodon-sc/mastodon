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
package org.mastodon.app.views;

import java.util.Map;

import org.mastodon.app.AppModel;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for factories that create Mastodon views.
 * <p>
 * This interface does not extend {@link SciJavaPlugin}. A specific app will
 * have a collection of factories, that implement this interface, and another
 * marker interface specific to the app, that extends {@link SciJavaPlugin}, so
 * that the app can discover only the factories that it uses.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <T>
 *            the type of view created by this factory.
 */
public interface MastodonViewFactory< T extends MastodonFrameView2, AM extends AppModel< AM, ?, ?, ?, ? > > extends SciJavaPlugin
{

	/**
	 * Key to the view type name. Value is a string.
	 */
	static final String VIEW_TYPE_KEY = "Type";

	/**
	 * Key that specifies whether the colorbar is visible.
	 */
	public static final String COLORBAR_VISIBLE_KEY = "ColorbarVisible";

	/**
	 * Key that specifies the colorbar position. Values are {@link Position}
	 * enum values.
	 */
	public static final String COLORBAR_POSITION_KEY = "ColorbarPosition";

	/**
	 * Key that specifies the name of the feature color mode to use for coloring
	 * scheme based on feature color modes. A non-<code>null</code> value means
	 * the coloring scheme is based on feature values.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #TAG_SET_KEY
	 * @see #TRACK_COLORING_KEY
	 * 
	 */
	public static final String FEATURE_COLOR_MODE_KEY = "FeatureColorMode";

	/**
	 * Key that specifies whether we do not use a special coloring scheme on the
	 * view. If <code>true</code>, then we do not use a special coloring scheme.
	 *
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #TRACK_COLORING_KEY
	 */
	public static final String NO_COLORING_KEY = "NoColoring";

	/**
	 * Key that specifies whether we use an automatic color scheme that assigns
	 * the same color to all the spots and links of one track.
	 *
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #NO_COLORING_KEY
	 */
	public static final String TRACK_COLORING_KEY = "ColorByTrack";

	/**
	 * Key that specifies the name of the tag-set to use for coloring scheme
	 * based on tag-sets. A non-<code>null</code> value means the coloring
	 * scheme is based on tag-sets.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #TRACK_COLORING_KEY
	 */
	public static final String TAG_SET_KEY = "TagSet";

	/**
	 * Key to the parameter that stores the frame position for
	 * {@link MastodonFrameView}s. Value is an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 */
	public static final String FRAME_POSITION_KEY = "FramePosition";

	/**
	 * Key that specifies whether the settings panel is visible or not.
	 */
	public static final String SETTINGS_PANEL_VISIBLE_KEY = "SettingsPanelVisible";

	/**
	 * Key to the lock group id. Value is an int.
	 */
	public static final String GROUP_HANDLE_ID_KEY = "LockGroupId";

	/**
	 * Key that specifies settings specific to the branch-graph view in a common
	 * view. Values are <code>Map&lt;String, Object&gt;</code>.
	 */
	public static final String BRANCH_GRAPH = "BranchGraph";

	/**
	 * Creates a new view for the specified app model.
	 * <p>
	 * The new view has default GUI state and is not shown.
	 *
	 * @param appModel
	 *            the app model.
	 *
	 * @return a new view.
	 */
	T create( AM appModel );

	/**
	 * Creates and shows a new view for the specified project model, and restore
	 * the GUI state stored in the specified map.
	 *
	 * @param appModel
	 *            the app model.
	 * @param guiState
	 *            the GUI state map.
	 * @return a new view.
	 */
	T show( AM appModel, Map< String, Object > guiState );

	/**
	 * Restores the GUI state stored in the specified map for the specified
	 * view.
	 *
	 * @param view
	 *            the view.
	 * @param guiState
	 *            the GUI state map.
	 */
	void restoreGuiState( T view, Map< String, Object > guiState );
	/**
	 * Serializes the current GUI state of the specified view in a map.
	 *
	 * @param view
	 *            the view.
	 * @return a new map.
	 */
	Map< String, Object > getGuiState( T view );

	/**
	 * Returns the class of the view created by this factory.
	 * <p>
	 * This class is used as key in several maps or to get the right factory
	 * when deserializing GUI state.
	 *
	 * @return the view class.
	 */
	Class< T > getViewClass();

	/**
	 * Returns the name of the command that will use this factory.
	 *
	 * @return the command name.
	 */
	String getCommandName();

	/**
	 * Returns the list of default keystrokes of the command.
	 *
	 * @return the default keystrokes0
	 */
	String[] getCommandKeys();

	/**
	 * Returns the description of the command.
	 *
	 * @return the description.
	 */
	String getCommandDescription();

	/**
	 * Returns the text of the command to appear in menus.
	 *
	 * @return the menu text for the command.
	 */
	String getCommandMenuText();

}
