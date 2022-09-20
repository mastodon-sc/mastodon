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
package org.mastodon.app.ui.settings;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scijava.listeners.Listeners;

/**
 * Helper class to easily put a {@link JPanel} as a page into a
 * {@link SettingsPanel}.
 * <p>
 * Call {@link #notifyModified()} when edits were made in your panel. (This will
 * trigger correct "Cancel", "Apply", and "OK" behaviour by the
 * {@link SettingsPanel}.)
 * </p>
 * <p>
 * To specify what should happen on "Apply" (propagate panel edits to your data
 * model) add a {@code #runOnApply} callback. To specify what should happen on
 * "Cancel" (reset panel contents to your data model) add a {@code #runOnCancel}
 * callback.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class SimpleSettingsPage implements SettingsPage
{
	private final String treePath;

	private final JPanel panel;

	private final Listeners.List< ModificationListener > modificationListeners;

	public SimpleSettingsPage( final String treePath, final JPanel panel )
	{
		this.treePath = treePath;
		this.panel = panel;
		modificationListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public String getTreePath()
	{
		return treePath;
	}

	@Override
	public JPanel getJPanel()
	{
		return panel;
	}

	public void notifyModified()
	{
		modificationListeners.list.forEach( ModificationListener::modified );
	}

	@Override
	public Listeners< ModificationListener > modificationListeners()
	{
		return modificationListeners;
	}

	protected final ArrayList< Runnable > runOnApply = new ArrayList<>();

	public synchronized void onApply( final Runnable runnable )
	{
		runOnApply.add( runnable );
	}

	protected final ArrayList< Runnable > runOnCancel = new ArrayList<>();

	public synchronized void onCancel( final Runnable runnable )
	{
		runOnCancel.add( runnable );
	}

	@Override
	public void cancel()
	{
		runOnCancel.forEach( Runnable::run );
	}

	@Override
	public void apply()
	{
		runOnApply.forEach( Runnable::run );
	}
}
