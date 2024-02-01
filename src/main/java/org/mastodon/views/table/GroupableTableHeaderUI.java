/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @version 1.1 2010/10/23
 * @author Nobuo Tamemasa (modified by Q)
 */
public class GroupableTableHeaderUI extends BasicTableHeaderUI
{

	@Override
	public void paint( final Graphics g, final JComponent c )
	{
		final Rectangle clipBounds = g.getClipBounds();
		if ( header.getColumnModel() == null )
			return;
		( ( GroupableTableHeader ) header ).setColumnMargin();
		int column = 0;
		final Dimension size = header.getSize();
		final Rectangle cellRect = new Rectangle( 0, 0, size.width, size.height );
		final Hashtable< ColumnGroup, Rectangle > h = new Hashtable<>();

		final Enumeration< ? > enumeration = header.getColumnModel().getColumns();
		while ( enumeration.hasMoreElements() )
		{
			cellRect.height = size.height;
			cellRect.y = 0;
			final TableColumn aColumn = ( TableColumn ) enumeration.nextElement();
			final Enumeration< ? > cGroups = ( ( GroupableTableHeader ) header ).getColumnGroups( aColumn );
			if ( cGroups != null )
			{
				int groupHeight = 0;
				while ( cGroups.hasMoreElements() )
				{
					final ColumnGroup cGroup = ( ColumnGroup ) cGroups.nextElement();
					Rectangle groupRect = h.get( cGroup );
					if ( groupRect == null )
					{
						groupRect = new Rectangle( cellRect );
						final Dimension d = cGroup.getSize( header.getTable() );
						groupRect.width = d.width;
						groupRect.height = d.height;
						h.put( cGroup, groupRect );
					}
					paintCell( g, groupRect, cGroup );
					groupHeight += groupRect.height;
					cellRect.height = size.height - groupHeight;
					cellRect.y = groupHeight;
				}
			}
			cellRect.width = aColumn.getWidth();
			if ( cellRect.intersects( clipBounds ) )
			{
				paintCell( g, cellRect, column );
			}
			cellRect.x += cellRect.width;
			column++;
		}
	}

	private void paintCell( final Graphics g, final Rectangle cellRect, final int columnIndex )
	{
		final TableColumn aColumn = header.getColumnModel().getColumn( columnIndex );
		final TableCellRenderer renderer = getRenderer( columnIndex );
		final Component component = renderer.getTableCellRendererComponent(
				header.getTable(), aColumn.getHeaderValue(), false, false, -1, columnIndex );
		rendererPane.add( component );
		rendererPane.paintComponent( g, component, header, cellRect.x, cellRect.y,
				cellRect.width, cellRect.height, true );
	}

	private void paintCell( final Graphics g, final Rectangle cellRect, final ColumnGroup cGroup )
	{
		final TableCellRenderer renderer = cGroup.getHeaderRenderer();
		final Component component = renderer.getTableCellRendererComponent(
				header.getTable(), cGroup.getHeaderValue(), false, false, -1, -1 );
		rendererPane.add( component );
		rendererPane.paintComponent( g, component, header, cellRect.x, cellRect.y,
				cellRect.width, cellRect.height, true );
	}

	private int getHeaderHeight()
	{
		int height = 0;
		final TableColumnModel columnModel = header.getColumnModel();
		for ( int column = 0; column < columnModel.getColumnCount(); column++ )
		{
			final TableColumn aColumn = columnModel.getColumn( column );
			final TableCellRenderer renderer = getRenderer( column );
			final Component comp = renderer.getTableCellRendererComponent(
					header.getTable(), aColumn.getHeaderValue(), false, false, -1, column );
			int cHeight = comp.getPreferredSize().height;
			final Enumeration< ? > en = ( ( GroupableTableHeader ) header ).getColumnGroups( aColumn );
			if ( en != null )
			{
				while ( en.hasMoreElements() )
				{
					final ColumnGroup cGroup = ( ColumnGroup ) en.nextElement();
					cHeight += cGroup.getSize( header.getTable() ).height;
				}
			}
			height = Math.max( height, cHeight );
		}
		return height;
	}

	private TableCellRenderer getRenderer( final int column )
	{
		final TableColumnModel columnModel = header.getColumnModel();
		TableCellRenderer renderer = null;
		if ( column < 0 && column < columnModel.getColumnCount() )
		{
			renderer = columnModel.getColumn( column ).getHeaderRenderer();
		}
		if ( renderer == null )
		{
			renderer = header.getDefaultRenderer();
		}
		return renderer;
	}

	private Dimension createHeaderSize( long width )
	{
		final TableColumnModel columnModel = header.getColumnModel();
		width += columnModel.getColumnMargin() * columnModel.getColumnCount();
		if ( width > Integer.MAX_VALUE )
		{
			width = Integer.MAX_VALUE;
		}
		return new Dimension( ( int ) width, getHeaderHeight() );
	}

	@Override
	public Dimension getPreferredSize( final JComponent c )
	{
		long width = 0;
		final Enumeration< ? > enumeration = header.getColumnModel().getColumns();
		while ( enumeration.hasMoreElements() )
		{
			final TableColumn aColumn = ( TableColumn ) enumeration.nextElement();
			width = width + aColumn.getPreferredWidth();
		}
		return createHeaderSize( width );
	}
}
