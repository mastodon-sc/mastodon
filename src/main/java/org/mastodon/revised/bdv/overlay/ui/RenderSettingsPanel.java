package org.mastodon.revised.bdv.overlay.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

	private final AbstractButton arrowHeadBox;

	public RenderSettingsPanel( final RenderSettings renderSettings )
	{
		super( new GridBagLayout() );
		this.renderSettings = renderSettings;

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.gridwidth = 1;


		c.gridy = 0;
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


		c.gridy++;
		add( Box.createVerticalStrut( 15 ), c );


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
		add( timeLimitSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
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


		c.gridy++;
		add( Box.createVerticalStrut( 15 ), c );


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
		add( focusLimitSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
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
		add( Box.createVerticalStrut( 15 ), c );


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
		add( ellipsoidFadeDepthSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
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
		add( pointFadeDepthSlider, c );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		add( new JLabel( "center point fade depth" ), c );


		renderSettings.addUpdateListener( this );
		update();
	}

	protected void update()
	{
		synchronized ( renderSettings )
		{
			antialiasingBox.setSelected( renderSettings.getUseAntialiasing() );

			linksBox.setSelected( renderSettings.getDrawLinks() );
			timeLimit.setCurrentValue( renderSettings.getTimeLimit() );
			gradientBox.setSelected( renderSettings.getUseGradient() );

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

	@Override
	public void renderSettingsChanged()
	{
		update();
	}
}
