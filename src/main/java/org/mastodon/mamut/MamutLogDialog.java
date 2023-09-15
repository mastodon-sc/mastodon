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
package org.mastodon.mamut;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.mastodon.app.logging.DefaultMastodonLogger;
import org.mastodon.app.logging.MastodonLogPanel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogSource;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.Keymap;

public class MamutLogDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	/** Used to deregister a keymap listener on close. */
	private final ArrayList< Runnable > runOnDispose;

	/**
	 * Creates a new logging dialog with a new {@link DefaultMastodonLogger},
	 * and pass it the the specified {@link ProjectModel} so that future log
	 * events will be shown in the created dialog.
	 *
	 * @param owner
	 *            the dialog owner.
	 * @param projectModel
	 *            the {@link ProjectModel} to set.
	 * @param toggle
	 *            the named action that toggles the dialog visibility.
	 */
	public MamutLogDialog(
			final Frame owner,
			final ProjectModel projectModel,
			final AbstractNamedAction toggle )
	{
		super( owner, "Log", false );
		final Context context = projectModel.getContext();
		MastodonLogPanel loggingPanel = new MastodonLogPanel( context );
		final DefaultMastodonLogger logger = new DefaultMastodonLogger( loggingPanel, LogSource.newRoot(), LogLevel.INFO );
		projectModel.setLogger( logger );
		getContentPane().add( loggingPanel );
		setLocationRelativeTo( null );
		pack();

		// Add the toggle visibility to the dialog.
		this.runOnDispose = new ArrayList<>();
		if ( toggle != null )
		{
			final ActionMap am = getRootPane().getActionMap();
			final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			final Keymap keymap = projectModel.getKeymap();
			final Actions actions = new Actions( im, am, keymap.getConfig(), KeyConfigContexts.MASTODON );
			actions.namedAction( toggle, WindowManager.TOGGLE_LOG_DIALOG_KEYS );

			final Keymap.UpdateListener listener = () -> actions.updateKeyConfig( keymap.getConfig() );
			keymap.updateListeners().add( listener );
			runOnDispose.add( () -> keymap.updateListeners().remove( listener ) );
		}
	}

	@Override
	public void dispose()
	{
		runOnDispose.forEach( Runnable::run );
		runOnDispose.clear();
		super.dispose();
	}
}
