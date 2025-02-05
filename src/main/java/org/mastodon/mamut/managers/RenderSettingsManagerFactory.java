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

import org.mastodon.mamut.ProjectModel;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsConfigPage;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsManager;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.ui.settings.SettingsPage;

@Plugin( type = StyleManagerFactory.class, priority = Priority.NORMAL )
public class RenderSettingsManagerFactory implements StyleManagerFactory< RenderSettingsManager >
{

	@Override
	public RenderSettingsManager create( final ProjectModel projectModel )
	{
		return new RenderSettingsManager();
	}

	@Override
	public boolean hasSettingsPage()
	{
		return true;
	}

	@Override
	public SettingsPage createSettingsPage( final RenderSettingsManager manager )
	{
		return new RenderSettingsConfigPage( "Settings > BDV Render Settings", manager );
	}

	@Override
	public Class< RenderSettingsManager > getManagerClass()
	{
		return RenderSettingsManager.class;
	}
}
