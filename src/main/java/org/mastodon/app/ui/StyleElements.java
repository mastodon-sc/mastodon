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
package org.mastodon.app.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.util.ColorIcon;

import bdv.tools.brightness.SliderPanel;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValue;
import bdv.util.BoundedValueDouble;

public class StyleElements
{
	private static final Font SMALL_FONT;
	static
	{
		final Font font = new JLabel().getFont();
		SMALL_FONT = font.deriveFont( font.getSize2D() - 2f );
	}

	public static Separator separator()
	{
		return new Separator();
	}

	public static LabelElement label( final String label )
	{
		return new LabelElement( label );
	}

	public static BooleanElement booleanElement( final String label, final BooleanSupplier get,
			final Consumer< Boolean > set )
	{
		return new BooleanElement( label )
		{
			@Override
			public boolean get()
			{
				return get.getAsBoolean();
			}

			@Override
			public void set( final boolean b )
			{
				set.accept( b );
			}
		};
	}

	public static ColorElement colorElement( final String label, final Supplier< Color > get,
			final Consumer< Color > set )
	{
		return new ColorElement( label )
		{
			@Override
			public Color getColor()
			{
				return get.get();
			}

			@Override
			public void setColor( final Color c )
			{
				set.accept( c );
			}
		};
	}

	public static DoubleElement doubleElement( final String label, final double rangeMin, final double rangeMax,
			final DoubleSupplier get, final Consumer< Double > set )
	{
		return new DoubleElement( label, rangeMin, rangeMax )
		{
			@Override
			public double get()
			{
				return get.getAsDouble();
			}

			@Override
			public void set( final double v )
			{
				set.accept( v );
			}
		};
	}

	public static IntElement intElement( final String label, final int rangeMin, final int rangeMax,
			final IntSupplier get, final Consumer< Integer > set )
	{
		return new IntElement( label, rangeMin, rangeMax )
		{
			@Override
			public int get()
			{
				return get.getAsInt();
			}

			@Override
			public void set( final int v )
			{
				set.accept( v );
			}
		};
	}

	public static < E > EnumElement< E > enumElement( final String label, final E[] values, final Supplier< E > get,
			final Consumer< E > set )
	{
		return new EnumElement< E >( label, values )
		{

			@Override
			public E getValue()
			{
				return get.get();
			}

			@Override
			public void setValue( final E e )
			{
				set.accept( e );
			}
		};
	}

	public interface StyleElementVisitor
	{
		public default void visit( final Separator element )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final LabelElement label )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final ColorElement colorElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final BooleanElement booleanElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final DoubleElement doubleElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final IntElement intElement )
		{
			throw new UnsupportedOperationException();
		}

		public default < E > void visit( final EnumElement< E > enumElement )
		{
			throw new UnsupportedOperationException();
		}
	}

	/*
	 *
	 * ===============================================================
	 *
	 */

	public interface StyleElement
	{
		public default void update()
		{}

		public void accept( StyleElementVisitor visitor );
	}

	public static class Separator implements StyleElement
	{
		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class LabelElement implements StyleElement
	{
		private final String label;

		public LabelElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static abstract class ColorElement implements StyleElement
	{
		private final ArrayList< Consumer< Color > > onSet = new ArrayList<>();

		private final String label;

		public ColorElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< Color > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( getColor() ) );
		}

		public abstract Color getColor();

		public abstract void setColor( Color c );
	}

	public static abstract class BooleanElement implements StyleElement
	{
		private final String label;

		private final ArrayList< Consumer< Boolean > > onSet = new ArrayList<>();

		public BooleanElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< Boolean > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( get() ) );
		}

		public abstract boolean get();

		public abstract void set( boolean b );
	}

	public static abstract class DoubleElement implements StyleElement
	{
		private final BoundedValueDouble value;

		private final String label;

		public DoubleElement( final String label, final double rangeMin, final double rangeMax )
		{
			final double currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
			value = new BoundedValueDouble( rangeMin, rangeMax, currentValue )
			{
				@Override
				public void setCurrentValue( final double value )
				{
					super.setCurrentValue( value );
					if ( get() != getCurrentValue() )
						set( getCurrentValue() );
				}
			};
			this.label = label;
		}

		public BoundedValueDouble getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract double get();

		public abstract void set( double v );

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	public static abstract class IntElement implements StyleElement
	{
		private final BoundedValue value;

		private final String label;

		public IntElement( final String label, final int rangeMin, final int rangeMax )
		{
			final int currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
			value = new BoundedValue( rangeMin, rangeMax, currentValue )
			{
				@Override
				public void setCurrentValue( final int value )
				{
					super.setCurrentValue( value );
					if ( get() != getCurrentValue() )
						set( getCurrentValue() );
				}
			};
			this.label = label;
		}

		public BoundedValue getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract int get();

		public abstract void set( int v );

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	public static abstract class EnumElement< E > implements StyleElement
	{
		private final ArrayList< Consumer< E > > onSet = new ArrayList<>();

		private final String label;

		private final E[] values;

		public EnumElement( final String label, final E[] values )
		{
			this.label = label;
			this.values = values;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< E > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( getValue() ) );
		}

		public abstract E getValue();

		public abstract void setValue( E e );

		public E[] getValues()
		{
			return values;
		}
	}

	/*
	 *
	 * ===============================================================
	 *
	 */

	public static JLabel linkedLabel( final LabelElement element )
	{
		return new JLabel( element.getLabel() );
	}

	public static JCheckBox linkedCheckBox( final BooleanElement element, final String label )
	{
		final JCheckBox checkbox = new JCheckBox( label, element.get() );
		checkbox.setFocusable( false );
		checkbox.addActionListener( ( e ) -> element.set( checkbox.isSelected() ) );
		element.onSet( b -> {
			if ( b != checkbox.isSelected() )
				checkbox.setSelected( b );
		} );
		return checkbox;
	}

	public static JButton linkedColorButton( final ColorElement element, final String label,
			final JColorChooser colorChooser )
	{
		final ColorIcon icon = new ColorIcon( element.getColor(), 16, 2 );
		final JButton button = new JButton( label, icon );
		button.setOpaque( false );
		button.setContentAreaFilled( false );
		button.setBorderPainted( false );
		button.setFont( new JButton().getFont() );
		button.setMargin( new Insets( 0, 0, 0, 0 ) );
		button.setBorder( new EmptyBorder( 2, 5, 2, 2 ) );
		button.setHorizontalAlignment( SwingConstants.LEFT );
		button.setFocusable( false );
		button.addActionListener( e -> {
			colorChooser.setColor( element.getColor() );
			final JDialog d =
					JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent arg0 )
						{
							final Color c = colorChooser.getColor();
							if ( c != null )
							{
								icon.setColor( c );
								button.repaint();
								element.setColor( c );
							}
						}
					}, null );
			d.setVisible( true );
		} );
		element.onSet( icon::setColor );
		return button;
	}

	public static SliderPanel linkedSliderPanel( final IntElement element, final int tfCols )
	{
		final SliderPanel slider = new SliderPanel( null, element.getValue(), 1 );
		slider.setNumColummns( tfCols );
		slider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );
		return slider;
	}

	public static JSpinner linkedSpinner( final IntElement element )
	{
		final BoundedValue value = element.getValue();
		final SpinnerNumberModel model = new SpinnerNumberModel( element.get(), value.getRangeMin(), value.getRangeMax(), 1 );
		final JSpinner spinner = new JSpinner( model );
		spinner.setMaximumSize( new Dimension( 80, spinner.getMaximumSize().height ) );
		model.addChangeListener( e -> element.set( ( ( Number ) model.getValue() ).intValue() ) );
		return spinner;
	}

	public static SliderPanelDouble linkedSliderPanel( final DoubleElement element, final int tfCols )
	{
		final SliderPanelDouble slider = new SliderPanelDouble( null, element.getValue(), 1 );
		slider.setDecimalFormat( "0.####" );
		slider.setNumColummns( tfCols );
		slider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );
		return slider;
	}

	@SuppressWarnings( "unchecked" )
	public static < E > JSpinner linkedSpinnerEnumSelector( final EnumElement< E > element )
	{
		final SpinnerListModel model = new SpinnerListModel( element.getValues() );
		final JSpinner spinner = new JSpinner( model );
		spinner.setFont( SMALL_FONT );
		( ( DefaultEditor ) spinner.getEditor() ).getTextField().setEditable( false );
		model.setValue( element.getValue() );
		model.addChangeListener( e -> element.setValue( ( E ) model.getValue() ) );
		element.onSet( e -> {
			if ( e != model.getValue() )
				model.setValue( e );
		} );
		return spinner;
	}

	@SuppressWarnings( "unchecked" )
	public static < E > JComboBox< E > linkedComboBoxEnumSelector( final EnumElement< E > element )
	{
		final DefaultComboBoxModel< E > model = new DefaultComboBoxModel<>( element.values );
		final JComboBox< E > cb = new JComboBox<>( model );
		cb.setFont( SMALL_FONT );
		cb.addActionListener( e -> element.setValue( ( E ) model.getSelectedItem() ) );
		element.onSet( e -> {
			if ( e != model.getSelectedItem() )
				model.setSelectedItem( e );
		} );
		return cb;
	}
}
