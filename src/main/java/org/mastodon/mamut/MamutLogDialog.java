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

import org.mastodon.logging.DefaultMastodonLogger;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

public class MamutLogDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private final ArrayList< Runnable > runOnDispose;

	/**
	 * Creates a new logging dialog with a new {@link DefaultMastodonLogger},
	 * and pass it the the specified {@link MamutAppModel} so that future log
	 * events will be shown in the created dialog.
	 *
	 * @param owner
	 *            the dialog owner.
	 * @param appModel
	 *            the {@link MamutAppModel} to set.
	 * @param context
	 *            the context, used to create the logging panel.
	 * @param toggle
	 *            the named action that toggles the dialog visibility.
	 */
	public MamutLogDialog(
			final Frame owner,
			final MamutAppModel appModel,
			final Context context,
			final AbstractNamedAction toggle )
	{
		super( owner, "Log", false );
		final DefaultMastodonLogger logger = new DefaultMastodonLogger( context );
		appModel.setLog( logger );
		getContentPane().add( logger.getMastodonLogPanel() );
		pack();

		// Add the toggle visibility to the dialog.
		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Keymap keymap = appModel.getKeymap();
		final Actions actions = new Actions( im, am, keymap.getConfig(), KeyConfigContexts.MASTODON );
		actions.namedAction( toggle, WindowManager.TOGGLE_LOG_DIALOG_KEYS );

		final Keymap.UpdateListener listener = () -> actions.updateKeyConfig( keymap.getConfig() );
		keymap.updateListeners().add( listener );
		this.runOnDispose = new ArrayList<>();
		runOnDispose.add( () -> keymap.updateListeners().remove( listener ) );
	}

	@Override
	public void dispose()
	{
		runOnDispose.forEach( Runnable::run );
		runOnDispose.clear();
		super.dispose();
	}
}
