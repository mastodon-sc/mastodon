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
package org.mastodon.mamut.views;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.scijava.plugin.SciJavaPlugin;

public interface MamutViewFactory< T extends MamutViewI > extends SciJavaPlugin
{

	/**
	 * Key to the view type name. Value is a string.
	 */
	static final String VIEW_TYPE_KEY = "Type";

	/**
	 * Creates a new view for the specified project model.
	 * <p>
	 * The new view has default GUI state and is not shown.
	 * 
	 * @param projectModel
	 *            the project model.
	 * 
	 * @return a new view.
	 */
	public T create( final ProjectModel projectModel );

	/**
	 * Creates and shows a new view for the specified project model, and restore
	 * the GUI state stored in the specified map.
	 * 
	 * @param projectModel
	 *            the project model.
	 * @param guiState
	 *            the GUI state map.
	 * @return a new view.
	 */
	public T show( final ProjectModel projectModel, final Map< String, Object > guiState );

	/**
	 * Restores the GUI state stored in the specified map for the specified
	 * view.
	 * 
	 * @param view
	 *            the view.
	 * @param guiState
	 *            the GUI state map.
	 */
	public void restoreGuiState( final T view, final Map< String, Object > guiState );

	/**
	 * Serializes the current GUI state of the specified view in a map.
	 * 
	 * @param view
	 *            the view.
	 * @return a new map.
	 */
	public Map< String, Object > getGuiState( final T view );

	/**
	 * Returns the name of the command that will use this factory to create a
	 * new view.
	 * 
	 * @return the command name.
	 */
	public String getCommandName();

	/**
	 * Returns the list of default keystrokes of the command.
	 * 
	 * @return the default keystrokes0
	 */
	public String[] getCommandKeys();

	/**
	 * Returns the description of the command.
	 * 
	 * @return the description.
	 */
	public String getCommandDescription();

	/**
	 * Returns the text of the command to appear in menus.
	 * 
	 * @return the menu text for the command.
	 */
	public String getCommandMenuText();

	/**
	 * Returns the class of the view created by this factory.
	 * <p>
	 * This class is used as key in several maps or to get the right factory
	 * when deserializing GUI state.
	 * 
	 * @return the view class.
	 */
	public Class< T > getViewClass();
}
