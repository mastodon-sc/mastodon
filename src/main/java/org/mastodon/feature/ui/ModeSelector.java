package org.mastodon.feature.ui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

public class ModeSelector< E > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final List< Consumer< E > > listeners = new ArrayList<>();

	private final Map< E, JToggleButton > buttons;

	public ModeSelector( final E[] choices )
	{
		super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );
		buttons = new HashMap<>();
		final ButtonGroup group = new ButtonGroup();
		for ( final E c : choices )
		{
			final JRadioButton button = new JRadioButton( c.toString() );
			button.addActionListener( ( e ) -> listeners.forEach( l -> l.accept( c ) ) );
			group.add( button );
			add( button );
			buttons.put( c, button );
		}
	}

	public void setSelected( final E c )
	{
		buttons.get( c ).setSelected( true );
	}

	public List< Consumer< E > > listeners()
	{
		return listeners;
	}
}
