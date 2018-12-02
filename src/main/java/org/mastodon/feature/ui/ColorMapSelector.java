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

import org.mastodon.revised.ui.coloring.ColorMap;

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
