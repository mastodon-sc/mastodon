package net.trackmate.model.tgmm.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.HashSet;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.laf.TrackSchemeStyle;

public class DisplaySettingsPanel extends JPanel
{
	/*
	 * ENUMS.
	 */

	public static enum SpotOverlayStyle
	{
		ELLIPSE( "Ellipses" ),
		SQUARE( "Squares" );

		private final String name;

		private SpotOverlayStyle( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		};
	}

	/*
	 * USER ACTIONS.
	 */

	final ActionEvent limitFocusRangeOn = new ActionEvent( this, 0, "LimitFocusRangeOn" );

	final ActionEvent limitFocusRangeOff = new ActionEvent( this, 1, "LimitFocusRangeOff" );

	final ActionEvent focusRangeChanged = new ActionEvent( this, 2, "FocusRangeChanged" );

	final ActionEvent limitTimeRangeOn = new ActionEvent( this, 3, "LimitTimeRangeOn" );

	final ActionEvent limitTimeRangeOff = new ActionEvent( this, 4, "LimitTimeRangeOff" );

	final ActionEvent timeRangeChanged = new ActionEvent( this, 5, "TimeRangeChanged" );

	final ActionEvent antialiasingOn = new ActionEvent( this, 6, "AntialiasingOn" );

	final ActionEvent antialiasingOff = new ActionEvent( this, 7, "AntialiasingOff" );

	final ActionEvent gradientOn = new ActionEvent( this, 8, "GrandientOn" );

	final ActionEvent gradientOff = new ActionEvent( this, 9, "GrandientOff" );

	final ActionEvent spotStyleChanged = new ActionEvent( this, 10, "SpotStyleChanged" );
	
	final ActionEvent drawSpotsOn = new ActionEvent( this, 11, "DrawSpotsOn" );

	final ActionEvent drawSpotsOff = new ActionEvent( this, 12, "DrawSpotsOff" );

	final ActionEvent drawLinksOn = new ActionEvent( this, 13, "DrawLinksOn" );

	final ActionEvent drawLinksOff = new ActionEvent( this, 14, "DrawLinksOff" );

	final ActionEvent trackschemeContextOn = new ActionEvent( this, 15, "TrackschemeContextOn" );

	final ActionEvent trackschemeContextOff = new ActionEvent( this, 16, "TrackschemeContextOff" );

	final ActionEvent contextWindowChanged = new ActionEvent( this, 17, "ContextWindowChanged" );

	final ActionEvent trackschemeStyleChanged = new ActionEvent( this, 18, "TrackSchemeStyleChanged" );

	/*
	 * OTHER CONSTANTS
	 */

	private static final long serialVersionUID = 1L;

	private static final Font BIG_FONT = new Font( "Arial", Font.BOLD, 14 );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 11 );

	/*
	 * FIELDS.
	 */

	private final JFormattedTextField textFieldLimitFocusRange;

	private final JFormattedTextField textFieldLimitTimeRange;

	private final JFormattedTextField textFieldContextTrackScheme;

	private final JCheckBox chckbxLimitTimeRange;

	private final JCheckBox chckbxLimitFocusRange;

	private final JComboBox comboBoxStyle;

	private final HashSet< ActionListener > listeners = new HashSet< ActionListener >();

	private final JComboBox comboBoxTrackSchemeStyles;

	/**
	 * Create the panel.
	 */
	public DisplaySettingsPanel()
	{

		final JLabel lblBDVOverlay = new JLabel( "BDV overlay." );
		lblBDVOverlay.setFont( BIG_FONT );

		final JPanel panelBDVOverlay = new JPanel();
		panelBDVOverlay.setBorder( new LineBorder( new Color( 0, 0, 0 ) ) );

		final JLabel lblTrackscheme = new JLabel( "TrackScheme." );
		lblTrackscheme.setFont( BIG_FONT );

		final JPanel panelTrackScheme = new JPanel();
		panelTrackScheme.setBorder( new LineBorder( new Color( 0, 0, 0 ) ) );
		final GroupLayout groupLayout = new GroupLayout( this );
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup( Alignment.LEADING )
						.addGroup( groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
										.addComponent( lblBDVOverlay, GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE )
										.addComponent( panelBDVOverlay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.TRAILING )
										.addComponent( lblTrackscheme, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE )
										.addComponent( panelTrackScheme, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE ) )
								.addContainerGap() )
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup( Alignment.LEADING )
						.addGroup( groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblBDVOverlay )
										.addComponent( lblTrackscheme ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
										.addComponent( panelBDVOverlay, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
										.addComponent( panelTrackScheme, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE ) )
								.addContainerGap() )
				);

		final JCheckBox chckbxUseLiveContext = new JCheckBox( "Use live context" );
		chckbxUseLiveContext.setFont( FONT );
		chckbxUseLiveContext.setSelected( Launcher.DEFAULT_USE_TRACKSCHEME_CONTEXT );

		textFieldContextTrackScheme = new JFormattedTextField( new Double( ContextTrackScheme.DEFAULT_CONTEXT_WINDOW ) );
		textFieldContextTrackScheme.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldContextTrackScheme.setColumns( 5 );
		textFieldContextTrackScheme.setFont( FONT );
		textFieldContextTrackScheme.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( textFieldContextTrackScheme.isEditValid() )
					fireAction( contextWindowChanged );
			}
		} );
		textFieldContextTrackScheme.addFocusListener( new MyFocusListener( textFieldContextTrackScheme, contextWindowChanged ) );

		final JLabel lblFrames = new JLabel( "frames." );
		lblFrames.setFont( FONT );

		chckbxUseLiveContext.addItemListener( new CheckBoxEnablingListener( new JComponent[] { textFieldContextTrackScheme, lblFrames } ) );
		chckbxUseLiveContext.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxUseLiveContext.isSelected() ? trackschemeContextOn : trackschemeContextOff );
			}
		} );

		final JLabel lblLiveContext = new JLabel( "Live context." );
		lblLiveContext.setFont( FONT.deriveFont( Font.BOLD ) );

		final JLabel lblTrackschemeColorScheme = new JLabel( "TrackScheme color scheme." );
		lblTrackschemeColorScheme.setFont( FONT.deriveFont( Font.BOLD ) );


		comboBoxTrackSchemeStyles = new JComboBox( TrackSchemeStyle.AvailableStyles.values() );
		comboBoxTrackSchemeStyles.setSelectedItem( ShowTrackScheme.DEFAULT_TRAKSCHEME_STYLE );
		comboBoxTrackSchemeStyles.setFont( FONT );
		comboBoxTrackSchemeStyles.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( trackschemeStyleChanged );
			}
		} );

		final GroupLayout gl_panelTrackScheme = new GroupLayout( panelTrackScheme );
		gl_panelTrackScheme.setHorizontalGroup(
				gl_panelTrackScheme.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelTrackScheme.createSequentialGroup()
								.addContainerGap()
								.addGroup( gl_panelTrackScheme.createParallelGroup( Alignment.LEADING )
										.addComponent( lblTrackschemeColorScheme, GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE )
										.addGroup( gl_panelTrackScheme.createSequentialGroup()
												.addComponent( chckbxUseLiveContext, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE )
												.addPreferredGap( ComponentPlacement.RELATED )
												.addComponent( textFieldContextTrackScheme, 65, 65, 65 )
												.addPreferredGap( ComponentPlacement.RELATED )
												.addComponent( lblFrames ) )
										.addComponent( lblLiveContext )
										.addComponent( comboBoxTrackSchemeStyles, 0, 248, Short.MAX_VALUE ) )
								.addContainerGap() )
				);
		gl_panelTrackScheme.setVerticalGroup(
				gl_panelTrackScheme.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelTrackScheme.createSequentialGroup()
								.addContainerGap()
								.addComponent( lblLiveContext )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( gl_panelTrackScheme.createParallelGroup( Alignment.BASELINE )
										.addComponent( chckbxUseLiveContext )
										.addComponent( textFieldContextTrackScheme, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblFrames ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( lblTrackschemeColorScheme )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( comboBoxTrackSchemeStyles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
								.addContainerGap( 285, Short.MAX_VALUE ) )
				);
		panelTrackScheme.setLayout( gl_panelTrackScheme );

		final JLabel lblDepthOdDrawing = new JLabel( "Depth of drawing." );
		lblDepthOdDrawing.setFont( FONT.deriveFont( Font.BOLD ) );

		final JPanel panelDepthOfDrawing = new JPanel();

		final JLabel lblRendering = new JLabel( "Rendering." );
		lblRendering.setFont( FONT.deriveFont( Font.BOLD ) );

		final JPanel panelRendering = new JPanel();

		final JLabel lblStyle = new JLabel( "Spot style." );
		lblStyle.setFont( FONT.deriveFont( Font.BOLD ) );

		comboBoxStyle = new JComboBox( SpotOverlayStyle.values() );
		comboBoxStyle.setFont( FONT );
		comboBoxStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( spotStyleChanged );
			}
		} );

		final JLabel lblOverlayContent = new JLabel( "Overlay content." );
		lblOverlayContent.setFont( FONT.deriveFont( Font.BOLD ) );

		final JPanel panelOverlayContent = new JPanel();

		final GroupLayout gl_panelBDVOverlay = new GroupLayout( panelBDVOverlay );
		gl_panelBDVOverlay.setHorizontalGroup(
				gl_panelBDVOverlay.createParallelGroup( Alignment.TRAILING )
						.addGroup( gl_panelBDVOverlay.createSequentialGroup()
								.addContainerGap()
								.addGroup( gl_panelBDVOverlay.createParallelGroup( Alignment.LEADING )
										.addComponent( panelOverlayContent, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
										.addComponent( lblOverlayContent )
										.addComponent( panelRendering, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
										.addComponent( panelDepthOfDrawing, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
										.addComponent( lblDepthOdDrawing, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
										.addComponent( comboBoxStyle, 0, 222, Short.MAX_VALUE )
										.addComponent( lblStyle, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
										.addComponent( lblRendering, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE ) )
								.addContainerGap() )
				);
		gl_panelBDVOverlay.setVerticalGroup(
				gl_panelBDVOverlay.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelBDVOverlay.createSequentialGroup()
								.addContainerGap()
								.addComponent( lblOverlayContent )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( panelOverlayContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
								.addPreferredGap( ComponentPlacement.UNRELATED )
								.addComponent( lblDepthOdDrawing )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( panelDepthOfDrawing, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( lblRendering )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( panelRendering, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( lblStyle )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( comboBoxStyle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
								.addContainerGap( 87, Short.MAX_VALUE ) )
				);

		final JCheckBox chckbxDrawSpots = new JCheckBox( "Draw spots" );
		chckbxDrawSpots.setFont( FONT );
		chckbxDrawSpots.setSelected( TracksOverlaySpotCovariance.DEFAULT_DRAW_SPOTS );
		chckbxDrawSpots.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxDrawSpots.isSelected() ? drawSpotsOn : drawSpotsOff );
			}
		} );

		final JCheckBox chckbxDrawLinks = new JCheckBox( "Draw links" );
		chckbxDrawLinks.setFont( FONT );
		chckbxDrawLinks.setSelected( TracksOverlaySpotCovariance.DEFAULT_DRAW_ELLIPSE );
		chckbxDrawLinks.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxDrawLinks.isSelected() ? drawLinksOn : drawLinksOff );
			}
		} );

		final GroupLayout gl_panelOverlayContent = new GroupLayout( panelOverlayContent );
		gl_panelOverlayContent.setHorizontalGroup(
				gl_panelOverlayContent.createParallelGroup( Alignment.LEADING )
						.addComponent( chckbxDrawSpots, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
						.addComponent( chckbxDrawLinks, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
				);
		gl_panelOverlayContent.setVerticalGroup(
				gl_panelOverlayContent.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelOverlayContent.createSequentialGroup()
								.addComponent( chckbxDrawSpots )
								.addPreferredGap( ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
								.addComponent( chckbxDrawLinks ) )
				);
		panelOverlayContent.setLayout( gl_panelOverlayContent );

		final JCheckBox chckbxAntialiasing = new JCheckBox( "Antialiasing" );
		chckbxAntialiasing.setSelected( TracksOverlaySpotCovariance.DEFAULT_ALIASING_MODE == RenderingHints.VALUE_ANTIALIAS_ON );
		chckbxAntialiasing.setFont( FONT );
		chckbxAntialiasing.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxAntialiasing.isSelected() ? antialiasingOn : antialiasingOff );
			}
		} );

		final JCheckBox chckbxGradient = new JCheckBox( "Gradient" );
		chckbxGradient.setSelected( TracksOverlaySpotCovariance.DEFAULT_USE_GRADIENT );
		chckbxGradient.setFont( FONT );
		chckbxGradient.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxGradient.isSelected() ? gradientOn : gradientOff );
			}
		} );

		final GroupLayout gl_panelRendering = new GroupLayout( panelRendering );
		gl_panelRendering.setHorizontalGroup(
				gl_panelRendering.createParallelGroup( Alignment.LEADING )
						.addComponent( chckbxAntialiasing, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
						.addGroup( gl_panelRendering.createSequentialGroup()
								.addComponent( chckbxGradient, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE )
								.addContainerGap() )
				);
		gl_panelRendering.setVerticalGroup(
				gl_panelRendering.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelRendering.createSequentialGroup()
								.addComponent( chckbxAntialiasing )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( chckbxGradient )
								.addContainerGap( 11, Short.MAX_VALUE ) )
				);
		panelRendering.setLayout( gl_panelRendering );

		textFieldLimitFocusRange = new JFormattedTextField( new Double( TracksOverlaySpotCovariance.DEFAULT_LIMIT_FOCUS_RANGE ) );
		textFieldLimitFocusRange.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldLimitFocusRange.setColumns( 5 );
		textFieldLimitFocusRange.setFont( FONT );
		textFieldLimitFocusRange.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( textFieldLimitFocusRange.isEditValid() )
					fireAction( focusRangeChanged );
			}
		} );
		textFieldLimitFocusRange.addFocusListener( new MyFocusListener( textFieldLimitFocusRange, focusRangeChanged ) );

		final JLabel lblSpatialUnits = new JLabel( "pixels." );
		lblSpatialUnits.setFont( FONT );

		chckbxLimitFocusRange = new JCheckBox( "Limit focus range" );
		chckbxLimitFocusRange.setFont( FONT );
		chckbxLimitFocusRange.setSelected( !Double.isInfinite( TracksOverlaySpotCovariance.DEFAULT_LIMIT_FOCUS_RANGE ) );
		chckbxLimitFocusRange.addItemListener( new CheckBoxEnablingListener( new JComponent[] { textFieldLimitFocusRange, lblSpatialUnits } ) );
		chckbxLimitFocusRange.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxLimitFocusRange.isSelected() ? limitFocusRangeOn : limitFocusRangeOff );
			}
		} );

		textFieldLimitTimeRange = new JFormattedTextField( new Double( TracksOverlaySpotCovariance.DEFAULT_LIMIT_TIME_RANGE ) );
		textFieldLimitTimeRange.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldLimitTimeRange.setColumns( 10 );
		textFieldLimitTimeRange.setFont( FONT );
		textFieldLimitTimeRange.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( textFieldLimitTimeRange.isValid() )
					fireAction( timeRangeChanged );
			}
		} );
		textFieldLimitTimeRange.addFocusListener( new MyFocusListener( textFieldLimitTimeRange, timeRangeChanged ) );

		final JLabel lblTimeUnits = new JLabel( "frames." );
		lblTimeUnits.setFont( FONT );

		chckbxLimitTimeRange = new JCheckBox( "Limit time range" );
		chckbxLimitTimeRange.setFont( FONT );
		chckbxLimitTimeRange.setSelected( !Double.isInfinite( TracksOverlaySpotCovariance.DEFAULT_LIMIT_TIME_RANGE ) );
		chckbxLimitTimeRange.addItemListener( new CheckBoxEnablingListener( new JComponent[] { textFieldLimitTimeRange, lblTimeUnits } ) );
		chckbxLimitTimeRange.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				fireAction( chckbxLimitTimeRange.isSelected() ? limitTimeRangeOn : limitTimeRangeOff );
			}
		} );

		final GroupLayout gl_panelDepthOfDrawing = new GroupLayout( panelDepthOfDrawing );
		gl_panelDepthOfDrawing.setHorizontalGroup(
				gl_panelDepthOfDrawing.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelDepthOfDrawing.createSequentialGroup()
								.addGroup( gl_panelDepthOfDrawing.createParallelGroup( Alignment.LEADING )
										.addGroup( gl_panelDepthOfDrawing.createSequentialGroup()
												.addComponent( chckbxLimitTimeRange )
												.addGap( 12 )
												.addComponent( textFieldLimitTimeRange, 0, 0, Short.MAX_VALUE ) )
										.addGroup( gl_panelDepthOfDrawing.createSequentialGroup()
												.addComponent( chckbxLimitFocusRange )
												.addPreferredGap( ComponentPlacement.RELATED )
												.addComponent( textFieldLimitFocusRange, GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE ) ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( gl_panelDepthOfDrawing.createParallelGroup( Alignment.LEADING )
										.addGroup( gl_panelDepthOfDrawing.createSequentialGroup()
												.addComponent( lblTimeUnits, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE )
												.addContainerGap() )
										.addComponent( lblSpatialUnits, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE ) ) )
				);
		gl_panelDepthOfDrawing.setVerticalGroup(
				gl_panelDepthOfDrawing.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelDepthOfDrawing.createSequentialGroup()
								.addGroup( gl_panelDepthOfDrawing.createParallelGroup( Alignment.BASELINE )
										.addComponent( chckbxLimitFocusRange )
										.addComponent( textFieldLimitFocusRange, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblSpatialUnits ) )
								.addGap( 8 )
								.addGroup( gl_panelDepthOfDrawing.createParallelGroup( Alignment.BASELINE )
										.addComponent( chckbxLimitTimeRange )
										.addComponent( textFieldLimitTimeRange, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblTimeUnits ) )
								.addContainerGap( 14, Short.MAX_VALUE ) )
				);
		panelDepthOfDrawing.setLayout( gl_panelDepthOfDrawing );
		panelBDVOverlay.setLayout( gl_panelBDVOverlay );
		setLayout( groupLayout );
	}

	public SpotOverlayStyle getSelectedSpotOverlayStyle() 
	{
		return SpotOverlayStyle.values()[ comboBoxStyle.getSelectedIndex() ];
	}

	public TrackSchemeStyle getSelectedTrackSchemeStyle()
	{
		return TrackSchemeStyle.AvailableStyles.values()[ comboBoxTrackSchemeStyles.getSelectedIndex() ].getStyle();
	}

	public double getFocusRange()
	{
		try
		{
			textFieldLimitFocusRange.commitEdit();
		}
		catch ( final ParseException e )
		{}
		return Math.abs( ( Double ) textFieldLimitFocusRange.getValue() );
	}

	public double getTimeRange()
	{
		try
		{
			textFieldLimitTimeRange.commitEdit();
		}
		catch ( final ParseException e )
		{}
		return Math.abs( ( Double ) textFieldLimitTimeRange.getValue() );
	}

	public int getContextWindow()
	{
		try
		{
			textFieldContextTrackScheme.commitEdit();
		}
		catch ( final ParseException e )
		{}
		return ( int ) Math.abs( ( Double ) textFieldContextTrackScheme.getValue() );
	}


	public boolean addActionListener( final ActionListener listener )
	{
		return listeners.add( listener );
	}

	/*
	 * PRIVATE METHODS AND CLASSES.
	 */
	
	private void fireAction( final ActionEvent actionEvent )
	{
		for ( final ActionListener listener : listeners )
		{
			listener.actionPerformed( actionEvent );
		}
	}

	private static final class CheckBoxEnablingListener implements ItemListener
	{
		private final JComponent[] components;

		public CheckBoxEnablingListener( final JComponent[] components )
		{
			this.components = components;
		}

		@Override
		public void itemStateChanged( final ItemEvent event )
		{
			final boolean selected = event.getStateChange() == ItemEvent.SELECTED;
			for ( final JComponent component : components )
			{
				component.setEnabled( selected );
			}
		}
	}

	private final class MyFocusListener implements FocusListener
	{
		private double lastValidValue;

		private final ActionEvent toFire;

		private final JFormattedTextField textField;

		public MyFocusListener( final JFormattedTextField textField, final ActionEvent toFire )
		{
			this.textField = textField;
			this.toFire = toFire;
			this.lastValidValue = ( Double ) textField.getValue();
		}

		@Override
		public void focusGained( final FocusEvent e )
		{}

		@Override
		public void focusLost( final FocusEvent e )
		{
			try
			{
				textField.commitEdit();
			}
			catch ( final ParseException e1 )
			{}
			final double val = ( Double ) textField.getValue();
			if ( textField.isEditValid() && ( lastValidValue != val ) )
			{
				lastValidValue = val;
				fireAction( toFire );
			}
		}
	}


	/*
	 * MAIN
	 */

	public static void main( final String[] args )
	{
		final JFrame frame = new JFrame( "Display settings" );
		final DisplaySettingsPanel panel = new DisplaySettingsPanel();
		panel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				System.out.println(e);
			}
		} );
		frame.getContentPane().add( panel );
		frame.setVisible( true );

	}
}
