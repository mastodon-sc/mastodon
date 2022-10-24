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
package org.mastodon.util;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ToggleDialogAction extends AbstractNamedAction implements HasSelectedState
{
	private static final long serialVersionUID = 1L;

	protected final Dialog dialog;

	protected final Listeners.List< Listener > selectListeners;

	public ToggleDialogAction( final String name, final Dialog dialog )
	{
		super( name );
		this.dialog = dialog;
		selectListeners = new Listeners.SynchronizedList<>();
		dialog.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				selectListeners.list.forEach( l -> l.setSelected( true ) );
			}

			@Override
			public void componentHidden( final ComponentEvent e )
			{
				selectListeners.list.forEach( l -> l.setSelected( false ) );
			}
		} );
	}

	@Override
	public void actionPerformed( final ActionEvent arg0 )
	{
		dialog.setVisible( !dialog.isVisible() );
	}

	@Override
	public boolean isSelected()
	{
		return dialog.isVisible();
	}

	@Override
	public Listeners< Listener > selectListeners()
	{
		return selectListeners;
	}
}
