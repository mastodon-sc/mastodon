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
package org.mastodon.ui.context;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextProvider;

public class ContextChooserPanel< V > extends JPanel implements ContextChooser.UpdateListener
{
	private static final long serialVersionUID = 1L;

	private final ContextChooser< V > contextChooser;

	private class Entry
	{
		private final ContextProvider< V > provider;

		public Entry( final ContextProvider< V > provider )
		{
			this.provider = provider;
		}

		@Override
		public String toString()
		{
			return provider.getName();
		}

		public ContextProvider< V > getProvider()
		{
			return provider;
		}
	}

	private final JComboBox< Entry > comboBox;

	public ContextChooserPanel( final ContextChooser< V > contextChooser )
	{
		super( new FlowLayout( FlowLayout.LEADING ) );
		this.contextChooser = contextChooser;
		final Font font = getFont().deriveFont( getFont().getSize2D() - 2f );
		comboBox = new JComboBox<>();
		comboBox.setFont( font );
		comboBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				@SuppressWarnings( "unchecked" )
				final Entry entry = ( Entry ) comboBox.getSelectedItem();
				contextChooser.choose( entry.getProvider() );
			}
		} );
		final JLabel lbl = new JLabel( "context:" );
		lbl.setFont( font );
		add( lbl );
		add( comboBox );
		contextChooser.updateListeners().add( this );
	}

	@Override
	public synchronized void contextChooserUpdated()
	{
		final DefaultComboBoxModel< Entry > model = new DefaultComboBoxModel<>();
		final ContextProvider< V > chosenProvider = contextChooser.getChosenProvider();
		for ( final ContextProvider< V > provider : contextChooser.getProviders() )
		{
			final Entry entry = new Entry( provider );
			model.addElement( entry );
			if ( provider.equals( chosenProvider ) )
				model.setSelectedItem( entry );
		}
		comboBox.setModel( model );
	}
}
