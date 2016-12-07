package org.mastodon.revised.bdv.overlay.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.RenderSettings.UpdateListener;

import bdv.tools.brightness.SliderPanel;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValue;
import bdv.util.BoundedValueDouble;

public class RenderSettingsPanel extends JPanel implements UpdateListener
{
	private static final long serialVersionUID = 1L;

	private static final int tfCols = 4;

	private final RenderSettings renderSettings;

	private final JCheckBox antialiasingBox;

	private final JCheckBox gradientBox;

	private final JCheckBox intersectionBox;

	private final JCheckBox projectionBox;

	private final BoundedValue timeLimit;

	private final SliderPanel timeLimitSlider;

	private final JCheckBox linksBox;

	private final JCheckBox arrowHeadBox;

	private final JCheckBox spotsBox;

	private final AbstractButton centersBox;

	private final JCheckBox centersForEllipsesBox;

	private final JCheckBox drawSpotLabelsBox;

	private final BoundedValueDouble focusLimit;

	private final SliderPanelDouble focusLimitSlider;

	private final JCheckBox focusLimitRelativeBox;

	private final BoundedValueDouble ellipsoidFadeDepth;

	private final SliderPanelDouble ellipsoidFadeDepthSlider;

	private final BoundedValueDouble pointFadeDepth;

	private final SliderPanelDouble pointFadeDepthSlider;

	private final ArrayList< JButton > buttonList;

	public RenderSettingsPanel( final RenderSettings renderSettings )
	{
		super( new GridBagLayout() );

		this.renderSettings = renderSettings;

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );

		c.gridwidth = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;

		c.fill = GridBagConstraints.HORIZONTAL;
		final JLabel title1 = new JLabel( "Colors and look" );
		title1.setFont( getFont().deriveFont( Font.BOLD ) );
		title1.setHorizontalAlignment( SwingConstants.CENTER );
		title1.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ) );
		add( title1, c );


		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridy++;
		antialiasingBox = new JCheckBox();
		antialiasingBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setUseAntialiasing( antialiasingBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( antialiasingBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel( "anti-aliasing" ), c );

		/*
		 * Colors
		 */

		/*
		 * Link colors - taken from Tobias TrackScheme style.
		 */

		final List< ColorSetter > styleColors = styleColors( renderSettings );
		buttonList = new ArrayList<>( styleColors.size() );

		final JColorChooser colorChooser = new JColorChooser();

		c.gridy++;
		for ( final ColorSetter colorSetter : styleColors )
		{
			c.gridx = 0;
			final JButton button = new JButton( new ColorIcon( colorSetter.getColor() ) );
			buttonList.add( button );
			button.setMargin( new Insets( 0, 0, 0, 0 ) );
			button.setBorder( new EmptyBorder( 2, 0, 2, 6 ) );
			button.setHorizontalAlignment( SwingConstants.LEFT );
			button.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					colorChooser.setColor( colorSetter.getColor() );
					final JDialog d = JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent evt )
						{
							final Color c = colorChooser.getColor();
							if ( c != null )
							{
								final ColorIcon ci = new ColorIcon( c );
								button.setIcon( ci );
								colorSetter.setColor( c );
							}
						}
					}, null );
					d.setVisible( true );
				}
			} );
			c.anchor = GridBagConstraints.LINE_END;
			add( button, c );

			c.gridx = 1;
			c.anchor = GridBagConstraints.LINE_START;
			add( new JLabel( colorSetter.getLabel() ), c );

			c.gridy++;

			if ( colorSetter.skip > 0 )
			{
				add( Box.createVerticalStrut( colorSetter.skip ), c );
				c.gridy++;
			}
		}

		/*
		 * Links settings.
		 */

		c.gridy++;
		add( Box.createVerticalStrut( 15 ), c );

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		final JLabel title2 = new JLabel( "Links" );
		title2.setFont( getFont().deriveFont( Font.BOLD ) );
		title2.setHorizontalAlignment( SwingConstants.CENTER );
		title2.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ) );
		add( title2, c );

		c.gridwidth = 1;
		c.gridy++;
		linksBox = new JCheckBox();
		linksBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawLinks( linksBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		add( linksBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("draw links"), c );


		c.gridy++;
		timeLimit = new BoundedValue( 0, 100, 10 )
		{
			@Override
			public void setCurrentValue( final int value )
			{
				super.setCurrentValue( value );
				renderSettings.setTimeLimit( getCurrentValue() );
			}
		};
		timeLimitSlider = new SliderPanel( null, timeLimit, 1 );
		timeLimitSlider.setNumColummns( tfCols );
		timeLimitSlider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );

		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.weightx = 1.;
		c.fill = GridBagConstraints.HORIZONTAL;
		add( timeLimitSlider, c );

		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		c.weightx = 0.;
		c.fill = GridBagConstraints.NONE;
		add( new JLabel( "time range for links" ), c );


		c.gridy++;
		gradientBox = new JCheckBox();
		gradientBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setUseGradient( gradientBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( gradientBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel( "gradients for links" ), c );

		c.gridy++;
		arrowHeadBox = new JCheckBox();
		arrowHeadBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawLinkArrows( arrowHeadBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( arrowHeadBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel( "draw link arrow heads" ), c );

		/*
		 * Spot settings.
		 */

		c.gridy++;
		add( Box.createVerticalStrut( 15 ), c );

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		final JLabel title3 = new JLabel( "Spots" );
		title3.setFont( getFont().deriveFont( Font.BOLD ) );
		title3.setHorizontalAlignment( SwingConstants.CENTER );
		title3.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ) );
		add( title3, c );

		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridy++;
		spotsBox = new JCheckBox();
		spotsBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawSpots( spotsBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( spotsBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("draw spots"), c );


		c.gridy++;
		intersectionBox = new JCheckBox();
		intersectionBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawEllipsoidSliceIntersection( intersectionBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( intersectionBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("ellipsoid intersection"), c );


		c.gridy++;
		projectionBox = new JCheckBox();
		projectionBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawEllipsoidSliceProjection( projectionBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( projectionBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("ellipsoid projection"), c );


		c.gridy++;
		centersBox = new JCheckBox();
		centersBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawSpotCenters( centersBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( centersBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("draw spot centers"), c );


		c.gridy++;
		centersForEllipsesBox = new JCheckBox();
		centersForEllipsesBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawSpotCentersForEllipses( centersForEllipsesBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( centersForEllipsesBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("draw spot centers for ellipses"), c );


		/*
		 * Spot labels
		 */

		c.gridy++;
		drawSpotLabelsBox = new JCheckBox();
		drawSpotLabelsBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setDrawSpotLabels( drawSpotLabelsBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( drawSpotLabelsBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel( "draw spot labels" ), c );

		/*
		 * Vertical space.
		 */

		c.gridy++;
		add( Box.createVerticalStrut( 15 ), c );

		/*
		 * Focus limit.
		 */

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		final JLabel title4 = new JLabel( "Focus limits" );
		title4.setFont( getFont().deriveFont( Font.BOLD ) );
		title4.setHorizontalAlignment( SwingConstants.CENTER );
		title4.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ) );
		add( title4, c );

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.gridy++;
		focusLimit = new BoundedValueDouble( 0, 2000, 100 )
		{
			@Override
			public void setCurrentValue( final double value )
			{
				super.setCurrentValue( value );
				renderSettings.setFocusLimit( getCurrentValue() );
			}
		};
		focusLimitSlider = new SliderPanelDouble( null, focusLimit, 1 );
		focusLimitSlider.setDecimalFormat( "0.####" );
		focusLimitSlider.setNumColummns( tfCols );
		focusLimitSlider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );

		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		add( focusLimitSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;

		c.fill = GridBagConstraints.NONE;
		add( new JLabel( "focus limit (max dist to view plane)" ), c );


		c.gridy++;
		focusLimitRelativeBox = new JCheckBox();
		focusLimitRelativeBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				renderSettings.setFocusLimitViewRelative( focusLimitRelativeBox.isSelected() );
			}
		} );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		add( focusLimitRelativeBox, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel("view-relative focus limit"), c );

		c.gridy++;
		ellipsoidFadeDepth = new BoundedValueDouble( 0, 1, 0.2 )
		{
			@Override
			public void setCurrentValue( final double value )
			{
				super.setCurrentValue( value );
				renderSettings.setEllipsoidFadeDepth( getCurrentValue() );
			}
		};
		ellipsoidFadeDepthSlider = new SliderPanelDouble( null, ellipsoidFadeDepth, 0.05 );
		ellipsoidFadeDepthSlider.setDecimalFormat( "0.####" );
		ellipsoidFadeDepthSlider.setNumColummns( tfCols );
		ellipsoidFadeDepthSlider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		add( ellipsoidFadeDepthSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;

		c.fill = GridBagConstraints.NONE;
		add( new JLabel( "ellipsoid fade depth" ), c );


		c.gridy++;
		pointFadeDepth = new BoundedValueDouble( 0, 1, 0.2 )
		{
			@Override
			public void setCurrentValue( final double value )
			{
				super.setCurrentValue( value );
				renderSettings.setPointFadeDepth( getCurrentValue() );
			}
		};
		pointFadeDepthSlider = new SliderPanelDouble( null, pointFadeDepth, 0.05 );
		pointFadeDepthSlider.setDecimalFormat( "0.####" );
		pointFadeDepthSlider.setNumColummns( tfCols );
		pointFadeDepthSlider.setBorder( new EmptyBorder( 0, 0, 0, 6 ) );

		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		add( pointFadeDepthSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;

		c.fill = GridBagConstraints.NONE;
		add( new JLabel( "center point fade depth" ), c );


		renderSettings.addUpdateListener( this );
		update();
	}

	protected void update()
	{
		synchronized ( renderSettings )
		{
			antialiasingBox.setSelected( renderSettings.getUseAntialiasing() );

			buttonList.get( 0 ).setIcon( new ColorIcon( renderSettings.getLinkColor1() ) );
			buttonList.get( 1 ).setIcon( new ColorIcon( renderSettings.getLinkColor2() ) );
			
			linksBox.setSelected( renderSettings.getDrawLinks() );
			timeLimit.setCurrentValue( renderSettings.getTimeLimit() );
			gradientBox.setSelected( renderSettings.getUseGradient() );
			arrowHeadBox.setSelected( renderSettings.getDrawLinkArrows() );

			spotsBox.setSelected( renderSettings.getDrawSpots() );
			intersectionBox.setSelected( renderSettings.getDrawEllipsoidSliceIntersection() );
			projectionBox.setSelected( renderSettings.getDrawEllipsoidSliceProjection() );
			centersBox.setSelected( renderSettings.getDrawSpotCenters() );
			centersForEllipsesBox.setSelected( renderSettings.getDrawSpotCentersForEllipses() );
			drawSpotLabelsBox.setSelected( renderSettings.getDrawSpotLabels() );

			focusLimit.setCurrentValue( renderSettings.getFocusLimit() );
			focusLimitRelativeBox.setSelected( renderSettings.getFocusLimitViewRelative() );

			ellipsoidFadeDepth.setCurrentValue( renderSettings.getEllipsoidFadeDepth() );
			pointFadeDepth.setCurrentValue( renderSettings.getPointFadeDepth() );
		}
	}

	/**
	 * Updates the UI when the settings are changed.
	 */
	@Override
	public void renderSettingsChanged()
	{
		update();
	}

	/*
	 * COLOR STUFF
	 */

	private static List< ColorSetter > styleColors(final RenderSettings settings)
	{
		return Arrays.asList( new ColorSetter[] {
				new ColorSetter( "spot and link color 1" )
				{
					@Override public Color getColor() { return settings.getLinkColor1(); }
					@Override public void setColor( final Color c ) { settings.setLinkColor1( c ); }
				},
				new ColorSetter( "link color 2" )
				{
					@Override public Color getColor() { return settings.getLinkColor2(); }
					@Override public void setColor( final Color c ) { settings.setLinkColor2( c ); }
				}
		} );
	}
	
	/*
	 * INNER CLASSES
	 */
	
	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
	 */
	private static class ColorIcon implements Icon
	{
		private final int size = 16;

		private final Color color;

		public ColorIcon( final Color color )
		{
			this.color = color;
		}

		@Override
		public void paintIcon( final Component c, final Graphics g, final int x, final int y )
		{
			final Graphics2D g2d = ( Graphics2D ) g;
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setColor( color );
			final RoundRectangle2D shape = new RoundRectangle2D.Float( x, y, size, size, 5, 5 );
			g2d.fill( shape );
			g2d.setColor( Color.BLACK );
			g2d.draw( shape );
		}

		@Override
		public int getIconWidth()
		{
			return size;
		}

		@Override
		public int getIconHeight()
		{
			return size;
		}
	}

	/**
	 * Taken from Tobias TrackScheme
	 */
	private static abstract class ColorSetter
	{
		private final String label;

		private final int skip;

		public ColorSetter( final String label )
		{
			this( label, 0 );
		}

		public ColorSetter( final String label, final int skip )
		{
			this.label = label;
			this.skip = skip;
		}

		public String getLabel()
		{
			return label;
		}

		public abstract Color getColor();

		public abstract void setColor( Color c );
	}
}
