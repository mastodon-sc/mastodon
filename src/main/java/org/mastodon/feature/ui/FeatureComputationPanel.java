package org.mastodon.feature.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;

public class FeatureComputationPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon GO_ICON = new ImageIcon( FeatureComputationPanel.class.getResource( "bullet_green.png" ) );

	private static final ImageIcon CANCEL_ICON = new ImageIcon( FeatureComputationPanel.class.getResource( "cancel.png" ) );

	final JButton btnCompute;

	final JButton btnCancel;

	final JProgressBar progressBar;

	final JPanel panelConfig;

	final JCheckBox chckbxForce;

	public FeatureComputationPanel(
			final FeatureComputationModel model,
			final Collection< Class< ? > > targets )
	{
		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelComputation = new JPanel();
		add( panelComputation, BorderLayout.SOUTH );

		final GridBagLayout gbl_panelComputation = new GridBagLayout();
		gbl_panelComputation.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelComputation.rowWeights = new double[] { 1.0 };
		panelComputation.setLayout( gbl_panelComputation );

		final JPanel panelButton = new JPanel();
		final GridBagConstraints gbc_panelButton = new GridBagConstraints();
		gbc_panelButton.gridwidth = 2;
		gbc_panelButton.insets = new Insets( 10, 10, 10, 10 );
		gbc_panelButton.fill = GridBagConstraints.BOTH;
		gbc_panelButton.gridx = 0;
		gbc_panelButton.gridy = 0;
		panelComputation.add(panelButton, gbc_panelButton);
		panelButton.setLayout( new BoxLayout( panelButton, BoxLayout.X_AXIS ) );

		final Component horizontalStrut0 = Box.createHorizontalStrut( 20 );
		panelButton.add( horizontalStrut0 );

		btnCompute = new JButton( "Compute", GO_ICON );
		panelButton.add( btnCompute );

		btnCancel = new JButton( "Cancel", CANCEL_ICON );
		btnCancel.setVisible( false );
		panelButton.add( btnCancel );

		final Component horizontalStrut1 = Box.createHorizontalStrut( 20 );
		panelButton.add( horizontalStrut1 );

		progressBar = new JProgressBar();
		progressBar.setStringPainted( true );
		panelButton.add( progressBar );

		final Component horizontalStrut2 = Box.createHorizontalStrut( 5 );
		panelButton.add( horizontalStrut2 );

		chckbxForce = new JCheckBox( "Force compute all" );
		chckbxForce.setFont( new Font( "Lucida Grande", Font.PLAIN, 11 ) );
		panelButton.add( chckbxForce );

		final JPanel panelTitle = new JPanel( new FlowLayout( FlowLayout.LEADING ) );
		add( panelTitle, BorderLayout.NORTH );

		final JLabel lblTitle = new JLabel( "Features available for computation:" );
		lblTitle.setFont( getFont().deriveFont( Font.BOLD ) );
		panelTitle.add( lblTitle );

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder( null );
		splitPane.setResizeWeight( 0.5 );
		add( splitPane, BorderLayout.CENTER );

		final JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent( panelLeft );
		panelLeft.setLayout( new BorderLayout( 0, 0 ) );

		final JScrollPane scrollPaneFeatures = new JScrollPane();
		panelLeft.add( scrollPaneFeatures );
		scrollPaneFeatures.setViewportBorder( null );
		scrollPaneFeatures.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPaneFeatures.getVerticalScrollBar().setUnitIncrement( 20 );

		final JPanel panelFeatures = new JPanel();
		panelFeatures.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		scrollPaneFeatures.setViewportView( panelFeatures );
		final BoxLayout boxLayout = new BoxLayout( panelFeatures, BoxLayout.PAGE_AXIS );
		panelFeatures.setLayout( boxLayout );

		final JPanel panelRight = new JPanel();
		panelRight.setPreferredSize( new Dimension( 300, 300 ) );
		splitPane.setRightComponent( panelRight );
		panelRight.setLayout( new BorderLayout( 0, 0 ) );

		this.panelConfig = new JPanel();
		panelConfig.setLayout( new BorderLayout() );
		panelRight.add( panelConfig, BorderLayout.CENTER );

		// Feed the feature panel.

		final FeatureTable.SelectionListener< FeatureSpec< ?, ? > > sl = fs -> displayConfigPanel( fs, model.getDependencies( fs ) );
		final FeatureTable.Tables aggregator = new FeatureTable.Tables();

		for ( final Class< ? > target : targets )
		{
			final JPanel headerPanel = new JPanel();
			final BoxLayout hpLayout = new BoxLayout( headerPanel, BoxLayout.LINE_AXIS );
			headerPanel.setLayout( hpLayout );
			final JToggleButton button = new JToggleButton( "ALL" );
			button.setPreferredSize( new Dimension( 40, 24 ) );
			button.setFont( button.getFont().deriveFont( 6f ) );

			final JLabel lbl = new JLabel( target.getSimpleName() );
			lbl.setFont( panelFeatures.getFont().deriveFont( Font.BOLD ) );
			lbl.setAlignmentX( Component.LEFT_ALIGNMENT );

			headerPanel.add( lbl );
			headerPanel.add( Box.createHorizontalGlue() );
			headerPanel.add( button );

			panelFeatures.add( headerPanel );
			headerPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
			panelFeatures.add( Box.createVerticalStrut( 5 ) );

			final List< FeatureSpec< ?, ? > > featureSpecs = model.getFeatureSpecs( target )
					.stream()
					.filter( model::isVisible )
					.collect( Collectors.toList() );

			final FeatureTable< List< FeatureSpec< ?, ? > >, FeatureSpec< ?, ? > > featureTable =
					new FeatureTable<>(
							featureSpecs,
							List::size,
							List::get,
							FeatureSpec::getKey,
							model::isSelected,
							model::setSelected,
							model::isUptodate );

			featureTable.getComponent().setAlignmentX( Component.LEFT_ALIGNMENT );
			featureTable.getComponent().setBackground( panelFeatures.getBackground() );
			panelFeatures.add( featureTable.getComponent() );
			panelFeatures.add( Box.createVerticalStrut( 10 ) );

			aggregator.add( featureTable );
			featureTable.selectionListeners().add( sl );

			button.addChangeListener( new ChangeListener()
			{

				@Override
				public void stateChanged( final ChangeEvent e )
				{
					final boolean selected = button.isSelected();
					model.getFeatureSpecs( target ).forEach( fs -> model.setSelected( fs, selected ) );
					featureTable.getComponent().repaint();
				}
			} );
			button.doClick();
		}
		scrollPaneFeatures.setPreferredSize( new Dimension( 300, 300 ) );
	}

	private void displayConfigPanel( final FeatureSpec< ?, ? > spec, final Collection< FeatureSpec< ?, ? > > dependencies )
	{
		panelConfig.removeAll();
		if ( null == spec )
			return;

		final JPanel infoPanel = new JPanel();
		infoPanel.setLayout( new GridBagLayout() );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 5, 5, 5, 5 );
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.;
		c.weighty = 1.;

		final JLabel title = new JLabel( spec.getKey() );
		title.setFont( getFont().deriveFont( Font.BOLD ) );
		c.gridy = 0;
		infoPanel.add( title, c );

		final JLabel infoLbl = new JLabel();
		infoLbl.setFont( getFont().deriveFont( Font.ITALIC ) );
		infoLbl.setText( "<html>" + spec.getInfo() + "</html>" );
		c.gridy++;
		infoPanel.add( infoLbl, c );

		final JLabel multiplicityLbl = new JLabel();
		switch ( spec.getMultiplicity() )
		{
		case ON_SOURCES:
			multiplicityLbl.setText( "One value per source." );
			break;
		case ON_SOURCE_PAIRS:
			multiplicityLbl.setText( "One value per source pair." );
			break;
		case SINGLE:
		default:
			break;
		}
		c.gridy++;
		infoPanel.add( multiplicityLbl, c );

		final Set< FeatureProjectionSpec > projections = spec.getProjectionSpecs();
		final StringBuilder projStr = new StringBuilder( ( projections.size() == 1 )
				? "<html>Projection: <ul>"
				: "<html>Projections: <ul>" );
		for ( final FeatureProjectionSpec projSpec : projections )
		{
			projStr.append( "<li>" + projSpec.projectionName );
			projStr.append( "<br>- Dimension: " + projSpec.projectionDimension.toString() );
			projStr.append( "</li>" );
		}
		projStr.append( "</ul></html>" );
		c.gridy++;
		final JLabel projLabel = new JLabel( projStr.toString() );
		infoPanel.add( projLabel, c );

		if ( !dependencies.isEmpty() )
		{
			final StringBuilder depStr = new StringBuilder();
			if ( dependencies.size() == 1 )
				depStr.append( "<html>Dependency: <ul>" );
			else
				depStr.append( "<html>Dependencies: <ul>" );
			for ( final FeatureSpec< ?, ? > dep : dependencies )
				depStr.append( "<li>" + dep.getKey() + "</li>" );
			depStr.append( "</ul></html>" );
			c.gridy++;
			final JLabel depsLabel = new JLabel( depStr.toString() );
			infoPanel.add( depsLabel, c );
		}
		panelConfig.add( infoPanel, BorderLayout.NORTH );
		panelConfig.revalidate();
		panelConfig.repaint();
	}
}
