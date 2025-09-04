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
package org.mastodon.mamut.managers;

import org.mastodon.app.ui.UIModel;
import org.scijava.plugin.SciJavaPlugin;

import bdv.ui.settings.SettingsPage;

/**
 * Interface for discoverable style manager factories.
 * <p>
 * Such factories are meant to be automatically discovered by the window
 * manager, and used to create style managers.
 *
 * @param <T>
 *            the type of style manager created by this factory.
 *
 */
public interface StyleManagerFactory2< T > extends SciJavaPlugin
{

	/**
	 * Creates a new manager instance for the specified UI model.
	 *
	 * @param uiModel
	 *            the UI model.
	 *
	 * @return a new manager instance.
	 */
	public T create( final UIModel< ? > uiModel );

	/**
	 * Returns <code>true</code> if the manager handled by this factory has a
	 * {@link SettingsPage} that can configure it.
	 *
	 * @return whether there is a settings page for the manager.
	 */
	public boolean hasSettingsPage();

	/**
	 * Creates a new settings page for the specified manager.
	 *
	 * @param manager
	 *            the manager.
	 * @return a new settings page.
	 */
	public SettingsPage createSettingsPage( T manager );

	/**
	 * Returns the class of the manager created by this factory.
	 *
	 * @return the manager class.
	 */
	public Class< T > getManagerClass();

}
