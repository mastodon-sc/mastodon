/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.table;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * ColumnGroup
 *
 * @version 1.1 2010/10/23
 * @author Nobuo Tamemasa (modified by Q)
 */

public class ColumnGroup
{
	protected TableCellRenderer renderer;

	protected Vector< Object > v;

	protected String text;

	protected int margin = 0;

	public ColumnGroup( final String text )
	{
		this( null, text );
	}

	public ColumnGroup( final TableCellRenderer renderer, final String text )
	{
		if ( renderer == null )
		{
			this.renderer = new DefaultTableCellRenderer()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent( final JTable table, final Object value,
						final boolean isSelected, final boolean hasFocus, final int row, final int column )
				{
					final JTableHeader header = table.getTableHeader();
					if ( header != null )
					{
						setForeground( header.getForeground() );
						setBackground( header.getBackground() );
						setFont( header.getFont() );
					}
					setHorizontalAlignment( JLabel.CENTER );
					setText( ( value == null ) ? "" : value.toString() );
					setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
					return this;
				}
			};
		}
		else
		{
			this.renderer = renderer;
		}
		this.text = text;
		v = new Vector<>();
	}

	/**
	 * @param obj
	 *            TableColumn or ColumnGroup
	 */
	public void add( final Object obj )
	{
		if ( obj == null )
		{ return; }
		v.addElement( obj );
	}

	/**
	 * @param c
	 *            TableColumn
	 * @param g
	 *            ColumnGroups
	 * @return the column groups.
	 */
	public Vector< ColumnGroup > getColumnGroups( final TableColumn c, final Vector< ColumnGroup > g )
	{
		g.addElement( this );
		if ( v.contains( c ) )
			return g;
		final Enumeration< Object > en = v.elements();
		while ( en.hasMoreElements() )
		{
			final Object obj = en.nextElement();
			if ( obj instanceof ColumnGroup )
			{
				@SuppressWarnings( "unchecked" )
				final Vector< ColumnGroup > groups =
						( ( ColumnGroup ) obj ).getColumnGroups( c, ( Vector< ColumnGroup > ) g.clone() );
				if ( groups != null )
					return groups;
			}
		}
		return null;
	}

	public TableCellRenderer getHeaderRenderer()
	{
		return renderer;
	}

	public void setHeaderRenderer( final TableCellRenderer renderer )
	{
		if ( renderer != null )
		{
			this.renderer = renderer;
		}
	}

	public Object getHeaderValue()
	{
		return text;
	}

	public Dimension getSize( final JTable table )
	{
		final Component comp = renderer.getTableCellRendererComponent(
				table, getHeaderValue(), false, false, -1, -1 );
		final int height = comp.getPreferredSize().height;
		int width = 0;
		final Enumeration< Object > en = v.elements();
		while ( en.hasMoreElements() )
		{
			final Object obj = en.nextElement();
			if ( obj instanceof TableColumn )
			{
				final TableColumn aColumn = ( TableColumn ) obj;
				width += aColumn.getWidth();
			}
			else
			{
				width += ( ( ColumnGroup ) obj ).getSize( table ).width;
			}
		}
		return new Dimension( width, height );
	}

	public void setColumnMargin( final int margin )
	{}
}
