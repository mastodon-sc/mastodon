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
