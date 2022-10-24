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
package org.mastodon.views.table;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * GroupableTableHeader
 *
 * @version 1.1 2010/10/23
 * @author Nobuo Tamemasa (modified by Q)
 */

public class GroupableTableHeader extends JTableHeader
{

	private static final long serialVersionUID = 1L;

	protected Vector< ColumnGroup > columnGroups = null;

	public GroupableTableHeader( final TableColumnModel model )
	{
		super( model );
		setReorderingAllowed( false );
	}

	@Override
	public void setReorderingAllowed( final boolean b )
	{
		reorderingAllowed = false;
	}

	public void addColumnGroup( final ColumnGroup g )
	{
		if ( columnGroups == null )
			columnGroups = new Vector< ColumnGroup >();

		columnGroups.addElement( g );
	}

	public void clear()
	{
		if ( columnGroups != null )
			columnGroups.clear();
	}

	public Enumeration< ? > getColumnGroups( final TableColumn col )
	{
		if ( columnGroups == null )
			return null;
		final Enumeration< ColumnGroup > en = columnGroups.elements();
		while ( en.hasMoreElements() )
		{
			final ColumnGroup cGroup = en.nextElement();
			final Vector< ? > v_ret = cGroup.getColumnGroups( col, new Vector< ColumnGroup >() );
			if ( v_ret != null ) { return v_ret.elements(); }
		}
		return null;
	}

	@Override
	public void updateUI()
	{
		setUI( new GroupableTableHeaderUI() );

		final TableCellRenderer tablecellrenderer = getDefaultRenderer();
		if ( tablecellrenderer instanceof Component )
			SwingUtilities.updateComponentTreeUI( ( Component ) tablecellrenderer );
	}

	public void setColumnMargin()
	{
		if ( columnGroups == null )
			return;
//    final int columnMargin = getColumnModel().getColumnMargin();
		final Enumeration< ColumnGroup > en = columnGroups.elements();
		while ( en.hasMoreElements() )
		{
			final ColumnGroup cGroup = en.nextElement();
			cGroup.setColumnMargin( 0/* columnMargin */ );
		}
	}

}
