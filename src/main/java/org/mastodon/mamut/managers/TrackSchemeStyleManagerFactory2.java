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
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.ui.settings.SettingsPage;

@Plugin( type = StyleManagerFactory2.class, priority = Priority.NORMAL - 1 )
public class TrackSchemeStyleManagerFactory2 implements StyleManagerFactory2< TrackSchemeStyleManager >
{

	@Override
	public TrackSchemeStyleManager create( final UIModel< ? > uiModel )
	{
		return new TrackSchemeStyleManager();
	}

	@Override
	public boolean hasSettingsPage()
	{
		return true;
	}

	@Override
	public SettingsPage createSettingsPage( final TrackSchemeStyleManager manager )
	{
		return new TrackSchemeStyleSettingsPage( "Settings > TrackScheme Styles", manager );
	}

	@Override
	public Class< TrackSchemeStyleManager > getManagerClass()
	{
		return TrackSchemeStyleManager.class;
	}
}
