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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.mastodon.ui.coloring.ColorMap;

public class ColorMapSelector extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final List< Consumer< String > > listeners = new ArrayList<>();

	private final JComboBox< String > cb;

	public ColorMapSelector( final Collection< String > names )
	{
		super( new FlowLayout( FlowLayout.LEADING, 10, 10 ) );
		cb = new JComboBox<>( names.toArray( new String[] {} ) );
		add( cb );
		final ColorMapPainter painter = new ColorMapPainter( cb );
		add( painter );
		cb.addItemListener( ( e ) -> {
			if ( e.getStateChange() == ItemEvent.SELECTED )
			{
				listeners.forEach( l -> l.accept( ( String ) cb.getSelectedItem() ) );
				painter.repaint();
			}
		} );
	}

	public void setColorMap( final String name )
	{
		cb.setSelectedItem( name );
	}

	public List< Consumer< String > > listeners()
	{
		return listeners;
	}

	private static final class ColorMapPainter extends JComponent
	{

		private static final long serialVersionUID = 1L;

		private final JComboBox< String > choices;

		public ColorMapPainter( final JComboBox< String > choices )
		{
			this.choices = choices;
		}

		@Override
		protected void paintComponent( final Graphics g )
		{
			super.paintComponent( g );
			if ( !isEnabled() )
				return;

			final String cname = ( String ) choices.getSelectedItem();
			final ColorMap cmap = ColorMap.getColorMap( cname );
			final int w = getWidth();
			final int h = getHeight();
			final int lw = ( int ) ( 0.85 * w );
			for ( int i = 0; i < lw; i++ )
			{
				g.setColor( new Color( cmap.get( ( double ) i / lw ), true ) );
				g.drawLine( i, 0, i, h );
			}

			// NaN.
			g.setColor( new Color( cmap.get( Double.NaN ) ) );
			g.fillRect( ( int ) ( 0.9 * w ), 0, ( int ) ( 0.1 * w ), h );
		}

		@Override
		public Dimension getPreferredSize()
		{
			final Dimension dimension = super.getPreferredSize();
			dimension.height = 20;
			dimension.width = 150;
			return dimension;
		}

		@Override
		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}
	}
}
