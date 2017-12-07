package org.mastodon.revised.bdv.overlay.ui;

import bdv.tools.brightness.SliderPanel;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValue;
import bdv.util.BoundedValueDouble;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.mastodon.revised.bdv.overlay.RenderSettings;

public class RenderSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final int tfCols = 4;

	private final RenderSettings style;

	private final List< StyleElement > styleElements;

	public RenderSettingsPanel( final RenderSettings style )
	{
		super( new GridBagLayout() );
		this.style = style;

		styleElements = styleElements( style );

		style.addUpdateListener( () -> {
			styleElements.forEach( StyleElement::update );
			repaint();
		} );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		styleElements.forEach( element -> element.addToLayout( this, c ) );
	}

	private interface StyleElement
	{
		public default void addToLayout( final JComponent parent, final GridBagConstraints c )
		{
				c.anchor = GridBagConstraints.LINE_END;
				parent.add( comp1(), c );
				c.gridx++;
				c.weightx = 0.0;
				c.anchor = GridBagConstraints.LINE_START;
				parent.add( comp2(), c );
				c.gridx = 0;
				c.weightx = 1.0;
				c.gridy++;
		}

		public default Component comp1()
		{
			throw new UnsupportedOperationException();
		}
		public default Component comp2()
		{
			throw new UnsupportedOperationException();
		}

		public default void update()
		{}
	}

	private static class Separator implements StyleElement
	{
		@Override
		public void addToLayout( final JComponent parent, final GridBagConstraints c )
		{
			parent.add( Box.createVerticalStrut( 10 ), c );
			++c.gridy;
		}
	}

	private static Separator separator()
	{
		return new Separator();
	}

	private static abstract class BooleanSetter implements StyleElement
	{
		private final JCheckBox checkbox;

		private final JLabel label;

		public BooleanSetter( final String label )
		{
			checkbox = new JCheckBox( "", get() );
			checkbox.setFocusable( false );
			checkbox.addActionListener( ( e ) -> set( checkbox.isSelected() ) );
			checkbox.setHorizontalAlignment( SwingConstants.TRAILING );
			this.label = new JLabel( label );
		}

		public abstract boolean get();

		public abstract void set( boolean b );

		@Override
		public Component comp1()
		{
			return checkbox;
		}

		@Override
		public Component comp2()
		{
			return label;
		}

		@Override
		public void update()
		{
			if ( get() != checkbox.isSelected() )
				checkbox.setSelected( get() );
		}
	}

	private static BooleanSetter booleanSetter( final String label, final BooleanSupplier get, final Consumer< Boolean > set )
	{
		return new BooleanSetter( label )
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

	private static abstract class SliderDoubleSetter implements StyleElement
	{
		private final BoundedValueDouble value;

		private final SliderPanelDouble slider;

		private final JLabel label;

		public SliderDoubleSetter( final String label, double rangeMin, double rangeMax )
		{
			double currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
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
			slider = new SliderPanelDouble( null, value, 1 );
			slider.setDecimalFormat( "0.####" );
			slider.setNumColummns( tfCols );
			slider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );

			this.label = new JLabel( label );
		}

		public abstract double get();

		public abstract void set( double v );

		@Override
		public Component comp1()
		{
			return slider;
		}

		@Override
		public Component comp2()
		{
			return label;
		}

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	private static SliderDoubleSetter sliderSetter( final String label, double rangeMin, double rangeMax, final DoubleSupplier get, final Consumer< Double > set )
	{
		return new SliderDoubleSetter( label, rangeMin, rangeMax )
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

	private static abstract class SliderIntSetter implements StyleElement
	{
		private final BoundedValue value;

		private final SliderPanel slider;

		private final JLabel label;

		public SliderIntSetter( final String label, int rangeMin, int rangeMax )
		{
			int currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
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
			slider = new SliderPanel( null, value, 1 );
			slider.setNumColummns( tfCols );
			slider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );

			this.label = new JLabel( label );
		}

		public abstract int get();

		public abstract void set( int v );

		@Override
		public Component comp1()
		{
			return slider;
		}

		@Override
		public Component comp2()
		{
			return label;
		}

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	private static SliderIntSetter sliderSetter( final String label, int rangeMin, int rangeMax, final IntSupplier get, final Consumer< Integer > set )
	{
		return new SliderIntSetter( label, rangeMin, rangeMax )
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

	private List< StyleElement > styleElements( final RenderSettings style )
	{
		return Arrays.asList(
				booleanSetter( "anti-aliasing", style::getUseAntialiasing, style::setUseAntialiasing ),

				separator(),

				booleanSetter( "draw links", style::getDrawLinks, style::setDrawLinks ),
				sliderSetter( "time range for links", 0, 100, style::getTimeLimit, style::setTimeLimit ),
				booleanSetter( "gradients for links", style::getUseGradient, style::setUseGradient ),

				separator(),

				booleanSetter( "draw spots", style::getDrawSpots, style::setDrawSpots ),
				booleanSetter( "ellipsoid intersection", style::getDrawEllipsoidSliceIntersection, style::setDrawEllipsoidSliceIntersection ),
				booleanSetter( "ellipsoid projection", style::getDrawEllipsoidSliceProjection, style::setDrawEllipsoidSliceProjection ),
				booleanSetter( "draw spot centers", style::getDrawSpotCenters, style::setDrawSpotCenters ),
				booleanSetter( "draw spot centers for ellipses", style::getDrawSpotCentersForEllipses, style::setDrawSpotCentersForEllipses ),
				booleanSetter( "draw spot labels", style::getDrawSpotLabels, style::setDrawSpotLabels ),

				separator(),

				sliderSetter( "focus limit (max dist to view plane)", 0, 2000, style::getFocusLimit, style::setFocusLimit ),
				booleanSetter( "view relative focus limit", style::getFocusLimitViewRelative, style::setFocusLimitViewRelative ),

				separator(),
				sliderSetter( "ellipsoid fade depth", 0, 1, style::getEllipsoidFadeDepth, style::setEllipsoidFadeDepth ),
				sliderSetter( "center point fade depth", 0, 1, style::getPointFadeDepth, style::setPointFadeDepth )
		);
	}
}
