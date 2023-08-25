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
package org.mastodon.ui.coloring;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.mastodon.ui.coloring.ColorBarOverlay.ColorBarOverlayListener;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;

public class ColorBarOverlayMenu implements ColorBarOverlayListener
{
	private final JMenu menu;

	private final ColorBarOverlay colorBarOverlay;

	private final Runnable refresh;

	public ColorBarOverlayMenu(
			final JMenu menu,
			final ColorBarOverlay colorBarOverlay,
			final Runnable refresh )
	{
		this.menu = menu;
		this.colorBarOverlay = colorBarOverlay;
		this.refresh = refresh;
		rebuild();
	}

	public void rebuild()
	{
		menu.removeAll();
		menu.add( new JSeparator() );
		final JCheckBoxMenuItem toggleOverlay =
				new JCheckBoxMenuItem( "Show colorbar", colorBarOverlay.isVisible() );
		toggleOverlay.addActionListener( ( l ) -> {
			colorBarOverlay.setVisible( toggleOverlay.isSelected() );
			refresh.run();
		} );
		menu.add( toggleOverlay );

		menu.add( new JSeparator() );
		menu.add( "Position:" ).setEnabled( colorBarOverlay.isVisible() );

		final ButtonGroup buttonGroup = new ButtonGroup();
		for ( final Position position : Position.values() )
		{
			final JRadioButtonMenuItem positionItem = new JRadioButtonMenuItem( position.toString() );
			positionItem.addActionListener( ( l ) -> {
				if ( positionItem.isSelected() )
				{
					colorBarOverlay.setPosition( position );
					refresh.run();
				}
			} );
			buttonGroup.add( positionItem );
			menu.add( positionItem );

			if ( position.equals( colorBarOverlay.getPosition() ) )
				positionItem.setSelected( true );
		}
	}

	@Override
	public void colorBarOverlayChanged()
	{
		rebuild();
	}
}
