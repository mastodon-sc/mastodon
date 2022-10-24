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
package org.mastodon.model.tag.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.mastodon.util.ColorIcon;

public class ColorTagTable< C, T > extends AbstractTagTable< C, T, ColorTagTable< C, T >.Element >
{
	public class Element extends AbstractTagTable< C, T, ColorTagTable< C, T >.Element >.Element
	{
		public Element( final T wrapped )
		{
			super( wrapped );
		}

		Color getColor()
		{
			return wrapped == null ? Color.BLACK : getColor.apply( wrapped );
		}

		void setColor( final Color color )
		{
			if ( wrapped != null )
				setColor.accept( wrapped, color );
		}
	}

	private final JColorChooser colorChooser;

	private final int colorColumn = 1;

	private final BiConsumer< T, Color > setColor;

	private final Function< T, Color > getColor;

	public ColorTagTable(
			final C elements,
			final Function< C, T > addElement,
			final ToIntFunction< C > size,
			final BiConsumer< C, T > remove,
			final BiFunction< C, Integer, T > get,
			final BiConsumer< T, String > setName,
			final Function< T, String > getName,
			final BiConsumer< T, Color > setColor,
			final Function< T, Color > getColor )
	{
		super( elements, addElement, size, remove, get, setName, getName, 1 );
		this.setColor = setColor;
		this.getColor = getColor;

		colorChooser = new JColorChooser();

		table.getColumnModel().getColumn( colorColumn ).setCellRenderer( new MyColorButtonRenderer() );
		table.getColumnModel().getColumn( colorColumn ).setMaxWidth( 32 );
		table.addMouseListener( new MyTableColorButtonMouseListener() );
	}

	@Override
	protected Elements wrap( final C wrapped )
	{
		return new Elements( wrapped ) {
			@Override
			protected Element wrap( final T wrapped )
			{
				return new Element( wrapped );
			}
		};
	}

	private void editSelectedRowColor()
	{
		final int row = table.getSelectedRow();
		if ( row >= 0 )
		{
			final Element element = elements.get( row );
			colorChooser.setColor( element.getColor() );
			JColorChooser.createDialog( table, "Choose a color", true, colorChooser, ok -> {
				final Color c = colorChooser.getColor();
				if ( c != null )
				{
					element.setColor( c );
					notifyListeners();
					tableModel.fireTableRowsUpdated( row, row );
				}
			}, null ).setVisible( true );
		}
	}

	private class MyColorButtonRenderer extends JButton implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public MyColorButtonRenderer()
		{
			setOpaque( true );
			setBorderPainted( false );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			if ( row == elements.size() )
				setIcon( null );
			else
				setIcon( new ColorIcon( elements.get( row ).getColor() ) );
			final boolean paintSelected = isSelected && !table.isEditing();
			setForeground( paintSelected ? table.getSelectionForeground() : table.getForeground() );
			setBackground( paintSelected ? table.getSelectionBackground() : table.getBackground() );
			return this;
		}
	}

	private class MyTableColorButtonMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked( final MouseEvent e )
		{
			final int column = table.getColumnModel().getColumnIndexAtX( e.getX() );
			if ( column == colorColumn )
			{
				if ( table.getSelectedRow() < elements.size() )
					editSelectedRowColor();
			}
		}
	}
}
