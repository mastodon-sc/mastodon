/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 5 )
public class MamutBranchViewTrackSchemeFactory extends AbstractMamutViewFactory< MamutBranchViewTrackScheme >
{

	public static final String NEW_BRANCH_TRACKSCHEME_VIEW = "new branch trackscheme view";

	@Override
	public MamutBranchViewTrackScheme create( final ProjectModel projectModel )
	{
		return new MamutBranchViewTrackScheme( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutBranchViewTrackScheme view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		MamutViewTrackSchemeFactory.storeTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutBranchViewTrackScheme view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		MamutViewTrackSchemeFactory.restoreTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
	}

	@Override
	public String getCommandName()
	{
		return NEW_BRANCH_TRACKSCHEME_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new branch TrackScheme view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New TrackScheme Branch";
	}

	@Override
	public Class< MamutBranchViewTrackScheme > getViewClass()
	{
		return MamutBranchViewTrackScheme.class;
	}
}
