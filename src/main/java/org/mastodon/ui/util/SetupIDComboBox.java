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
package org.mastodon.ui.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

/**
 * A combo-box that lets the user select one of the setup ids present in a spim
 * data.
 *
 * @author Jean-Yves Tinevez
 */
public class SetupIDComboBox extends JComboBox< String >
{

	private static final long serialVersionUID = 1L;

	private final Map< String, Integer > idMap;

	private final Map< Integer, String > strMap;

	public SetupIDComboBox( final List< SourceAndConverter< ? > > sources )
	{
		if ( null == sources || sources.isEmpty() )
		{
			this.idMap = new HashMap<>( 1 );
			this.strMap = new HashMap<>( 1 );
			final String str = "No data";
			final Integer id = Integer.valueOf( -1 );
			idMap.put( str, id );
			strMap.put( id, str );
			final ComboBoxModel< String > aModel = new DefaultComboBoxModel<>( new String[] { str } );
			setModel( aModel );
		}
		else
		{
			final int nSetups = sources.size();
			this.idMap = new HashMap<>( nSetups );
			this.strMap = new HashMap<>( nSetups );

			final String[] items = new String[ nSetups ];
			int i = 0;
			for ( int setupID = 0; setupID < sources.size(); setupID++ )
			{
				final Source< ? > source = sources.get( setupID ).getSpimSource();
				final String name = source.getName();
				final long[] size = new long[ source.getSource( 0, 0 ).numDimensions() ];
				source.getSource( 0, 0 ).dimensions( size );
				final String str = ( null == name )
						? setupID + "  -  " + size[ 0 ] + " x " + size[ 1 ] + " x " + size[ 2 ]
						: setupID + "  -  " + name;
				items[ i++ ] = str;
				idMap.put( str, setupID );
				strMap.put( setupID, str );
			}

			final ComboBoxModel< String > aModel = new DefaultComboBoxModel<>( items );
			setModel( aModel );
		}
	}

	/**
	 * Returns the id of the setup id currently selected.
	 *
	 * @return the setup id.
	 */
	public int getSelectedSetupID()
	{
		final Integer id = idMap.get( getSelectedItem() );
		if (null == id)
			return idMap.values().iterator().next();

		return idMap.get( getSelectedItem() );
	}

	/**
	 * Sets the specified setup id as selection.
	 *
	 * @param setupID
	 *            the setup id to select.
	 */
	public void setSelectedSetupID( final int setupID )
	{
		setSelectedItem( strMap.get( Integer.valueOf( setupID ) ) );
	}
}
