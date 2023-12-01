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

import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class FeatureRangeSelector extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JFormattedTextField min;

	private final JFormattedTextField max;

	private final List< Consumer< double[] > > listeners = new ArrayList<>();

	private final JButton autoscale;

	public FeatureRangeSelector()
	{
		super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );

		final NumberFormat format = DecimalFormat.getNumberInstance();
		add( new JLabel( "min", JLabel.TRAILING ) );
		min = new JFormattedTextField( format );
		min.setColumns( 6 );
		min.setHorizontalAlignment( JLabel.TRAILING );
		min.setValue( 0. );
		add( min );
		add( new JLabel( "max", JLabel.TRAILING ) );
		max = new JFormattedTextField( format );
		max.setColumns( 6 );
		max.setHorizontalAlignment( JLabel.TRAILING );
		max.setValue( 1. );
		add( max );
		autoscale = new JButton( "autoscale" );
		add( autoscale );

		final FocusListener fl = new FocusAdapter()
		{
			@Override
			public void focusGained( final FocusEvent e )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						( ( JFormattedTextField ) e.getSource() ).selectAll();
					}
				} );
			}
		};
		min.addFocusListener( fl );
		max.addFocusListener( fl );

		autoscale.addActionListener( e -> new Thread( () -> preAutoscale(), "Autoscale calculation thread." ).start() );

		final PropertyChangeListener l = ( e ) -> notifyListeners();
		min.addPropertyChangeListener( "value", l );
		max.addPropertyChangeListener( "value", l );
	}

	private void preAutoscale()
	{
		min.setEnabled( false );
		max.setEnabled( false );
		autoscale.setEnabled( false );
		autoscale.setText( "calculating..." );
		try
		{
			autoscale();
		}
		finally
		{
			min.setEnabled( true );
			max.setEnabled( true );
			autoscale.setText( "autoscale" );
			autoscale.setEnabled( true );
		}
	}

	private void notifyListeners()
	{
		final double l1 = ( ( Number ) min.getValue() ).doubleValue();
		final double l2 = ( ( Number ) max.getValue() ).doubleValue();
		final double[] val = new double[] {
				Math.min( l1, l2 ),
				Math.max( l1, l2 )
		};
		listeners.forEach( c -> c.accept( val ) );
	}

	public abstract void autoscale();

	public void setMinMax( final double min, final double max )
	{
		final double l1 = Math.min( min, max );
		final double l2 = Math.max( min, max );
		this.min.setValue( Double.valueOf( l1 ) );
		this.max.setValue( Double.valueOf( l2 ) );
	}

	public List< Consumer< double[] > > listeners()
	{
		return listeners;
	}
}
