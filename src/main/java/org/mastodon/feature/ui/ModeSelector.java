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
package org.mastodon.feature.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

public class ModeSelector< E > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final List< Consumer< E > > listeners = new ArrayList<>();

	private final Map< E, JToggleButton > buttons;

	private final JScrollPane scrollPane;

	public ModeSelector( final E[] choices )
	{
		this( choices, null );
	}

	public ModeSelector( final E[] choices, final String[] toolips )
	{
		super( new BorderLayout() );
		buttons = new HashMap<>();

		final JPanel main = new JPanel();
		main.setLayout( new BoxLayout( main, BoxLayout.LINE_AXIS ) );
		final ButtonGroup group = new ButtonGroup();
		final FocusListener fl = new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{}

			@Override
			public void focusGained( final FocusEvent e )
			{
				scrollPane.getHorizontalScrollBar().setValue( e.getComponent().getLocation().x );
			}
		};

		for ( int i = 0; i < choices.length; i++ )
		{
			final E c = choices[ i ];
			final JRadioButton button = new JRadioButton( c.toString() );
			button.addFocusListener( fl );
			button.setAlignmentY( 1f );
			button.addActionListener( ( e ) -> listeners.forEach( l -> l.accept( c ) ) );
			if ( toolips != null && toolips.length - 1 >= i )
				button.setToolTipText( "<html><p width=200px>" + toolips[ i ] + "</html>" );

			group.add( button );
			main.add( button );
			buttons.put( c, button );
		}
		scrollPane = new JScrollPane( main );
		scrollPane.getHorizontalScrollBar().setUnitIncrement( 16 );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
		scrollPane.setBorder( null );
		add( scrollPane );

		setBorder( BorderFactory.createMatteBorder( 0, 1, 0, 0, Color.LIGHT_GRAY ) );
	}

	public void setSelected( final E c )
	{
		final JToggleButton btn = buttons.get( c );
		scrollPane.getHorizontalScrollBar().setValue( btn.getLocation().x );
		btn.setSelected( true );
		buttons.values().forEach( b -> b.setForeground( Color.LIGHT_GRAY ) );
		btn.setForeground( getForeground() );
	}

	public List< Consumer< E > > listeners()
	{
		return listeners;
	}
}
