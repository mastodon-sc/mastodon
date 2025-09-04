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
package org.mastodon.app;

import java.util.Map;

import org.mastodon.app.ui.MastodonFrameView2;
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
 * @param <M>
 *            the type of app model used in the application.
 * @param <G>
 *            the type of graph used in the model.
 * @param <V>
 *            the type of vertex in the graph.
 * @param <E>
 *            the type of edge in the graph.
 */
public interface MastodonViewFactory< T extends MastodonFrameView2 >
{

	/**
	 * Key to the view type name. Value is a string.
	 */
	static final String VIEW_TYPE_KEY = "Type";

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
	T create( AppModel< ?, ?, ?, ?, T, ? > appModel );

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
	T show( AppModel< ?, ?, ?, ?, T, ? > appModel, Map< String, Object > guiState );

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
