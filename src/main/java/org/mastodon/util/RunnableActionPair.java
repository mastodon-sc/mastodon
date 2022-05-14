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
