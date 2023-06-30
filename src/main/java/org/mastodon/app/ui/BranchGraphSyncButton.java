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
package org.mastodon.app.ui;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer.UpdateListener;

public class BranchGraphSyncButton extends JPanel
{

	private static final long serialVersionUID = 1L;

	public BranchGraphSyncButton( final BranchGraphSynchronizer model )
	{
		final JButton btn = new JButton();
		final UpdateListener l = () -> btn.setIcon( model.isUptodate()
				? MastodonIcons.UP_TO_DATE_ICON
				: MastodonIcons.NOT_UP_TO_DATE_ICON );
		model.updateListeners().add( l );
		l.branchGraphSyncChanged();
		btn.addActionListener( e -> new Thread( () -> model.sync(), "Branch-graph synchronizer" ).start() );
		add( btn );
		btn.setText( "Regenerate" );
		btn.setToolTipText( "Updates the view by regenerating the branch graph from the current model graph." );
		btn.setFont( new Font( "arial", Font.PLAIN, 10 ) );
	}
}
