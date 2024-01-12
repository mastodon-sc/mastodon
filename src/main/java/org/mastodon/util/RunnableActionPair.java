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
package org.mastodon.util;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

/**
 * An extension of <code>RunnableAction</code> that allows for executing two
 * different runnables. The first will be run on a click. The second one will be
 * run with shift-click. This is useful using JButtons, to trigger a 'special'
 * version of the first action when the user shift-click on it.
 * 
 * @author Jean-Yves Tinevez
 */
public class RunnableActionPair extends AbstractNamedAction implements Runnable
{

	private final Runnable action;

	private final Runnable actionWithShift;

	public RunnableActionPair( final String name, final Runnable action, final Runnable actionWithShift )
	{
		super( name );
		this.action = action;
		this.actionWithShift = actionWithShift;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		if ( ( e.getModifiers() & InputEvent.SHIFT_MASK ) != 0 )
			actionWithShift.run();
		else
			action.run();
	}

	@Override
	public void run()
	{
		action.run();
	}

	private static final long serialVersionUID = 1L;

}
