package org.mastodon.feature.ui;

import static org.mastodon.app.ui.settings.StyleElements.booleanElement;
import static org.mastodon.app.ui.settings.StyleElements.label;
import static org.mastodon.app.ui.settings.StyleElements.separator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import org.mastodon.app.ui.settings.StyleElements.StyleElement;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;

public class FeatureComputationPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon COG_ICON = new ImageIcon( FeatureComputationPanel.class.getResource( "cog.png" ) );

	private static final ImageIcon GO_ICON = new ImageIcon( FeatureComputationPanel.class.getResource( "bullet_green.png" ) );

	private static final ImageIcon CANCEL_ICON = new ImageIcon( FeatureComputationPanel.class.getResource( "cancel.png" ) );

	final JButton btnCompute;

	final JButton btnCancel;

	final JProgressBar progressBar;

	final JLabel lblComputationDate;

	final JLabel lblModelModificationDate;

	final JPanel panelConfig;

	private final FeatureComputationModel model;

	public FeatureComputationPanel(
			final FeatureComputationModel model,
			final Collection< Class< ? > > targets )
	{
		this.model = model;

		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelComputation = new JPanel();
		add( panelComputation, BorderLayout.SOUTH );

		final GridBagLayout gbl_panelComputation = new GridBagLayout();
		gbl_panelComputation.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelComputation.rowWeights = new double[] { 1.0, 0.0, 0.0 };
		panelComputation.setLayout( gbl_panelComputation );

		final JPanel panelButton = new JPanel();
		final GridBagConstraints gbc_panelButton = new GridBagConstraints();
		gbc_panelButton.gridwidth = 2;
		gbc_panelButton.insets = new Insets(0, 0, 5, 0);
		gbc_panelButton.fill = GridBagConstraints.BOTH;
		gbc_panelButton.gridx = 0;
		gbc_panelButton.gridy = 0;
		panelComputation.add(panelButton, gbc_panelButton);
		panelButton.setLayout( new BoxLayout( panelButton, BoxLayout.X_AXIS ) );

		final Component horizontalStrut = Box.createHorizontalStrut( 20 );
		panelButton.add( horizontalStrut );

		btnCompute = new JButton( "Compute", GO_ICON );
		panelButton.add( btnCompute );

		btnCancel = new JButton( "Cancel", CANCEL_ICON );
		btnCancel.setVisible( false );
		panelButton.add( btnCancel );

		final Component horizontalStrut_1 = Box.createHorizontalStrut( 20 );
		panelButton.add( horizontalStrut_1 );

		progressBar = new JProgressBar();
		progressBar.setStringPainted( true );
		panelButton.add( progressBar );

		final Component horizontalStrut_2 = Box.createHorizontalStrut( 5 );
		panelButton.add( horizontalStrut_2 );

		final JLabel lblLastFeatureComputation = new JLabel( "Last feature computation:" );
		final GridBagConstraints gbc_lblLastFeatureComputation = new GridBagConstraints();
		gbc_lblLastFeatureComputation.anchor = GridBagConstraints.WEST;
		gbc_lblLastFeatureComputation.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblLastFeatureComputation.gridx = 0;
		gbc_lblLastFeatureComputation.gridy = 1;
		panelComputation.add( lblLastFeatureComputation, gbc_lblLastFeatureComputation );

		lblComputationDate = new JLabel( "Never." );
		final GridBagConstraints gbc_lblComputationDate = new GridBagConstraints();
		gbc_lblComputationDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblComputationDate.anchor = GridBagConstraints.WEST;
		gbc_lblComputationDate.insets = new Insets(5, 5, 5, 0);
		gbc_lblComputationDate.gridx = 1;
		gbc_lblComputationDate.gridy = 1;
		panelComputation.add( lblComputationDate, gbc_lblComputationDate );

		final JLabel lblLastModelModification = new JLabel( "Model last modified:" );
		final GridBagConstraints gbc_lblLastFeatureComputation_1 = new GridBagConstraints();
		gbc_lblLastFeatureComputation_1.anchor = GridBagConstraints.WEST;
		gbc_lblLastFeatureComputation_1.insets = new Insets(5, 5, 0, 5);
		gbc_lblLastFeatureComputation_1.gridx = 0;
		gbc_lblLastFeatureComputation_1.gridy = 2;
		panelComputation.add( lblLastModelModification, gbc_lblLastFeatureComputation_1 );

		lblModelModificationDate = new JLabel( "Unknown." );
		final GridBagConstraints gbc_lblModelModificationDate = new GridBagConstraints();
		gbc_lblModelModificationDate.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblModelModificationDate.insets = new Insets(5, 5, 0, 0);
		gbc_lblModelModificationDate.gridx = 1;
		gbc_lblModelModificationDate.gridy = 2;
		panelComputation.add( lblModelModificationDate, gbc_lblModelModificationDate );

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
		scrollPaneFeatures.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		final JPanel panelFeatures = new JPanel();
		panelFeatures.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		panelFeatures.setPreferredSize( new Dimension( 300, 300 ) );
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

		final FeatureTable.SelectionListener< ElementWrapper > sl = t -> displayConfigPanel( t.fs );
		final FeatureTable.Tables aggregator = new FeatureTable.Tables();

		for ( final Class< ? > target : targets )
		{
			final JLabel lbl = new JLabel( target.getSimpleName() );
			lbl.setFont( panelFeatures.getFont().deriveFont( Font.BOLD ).deriveFont( panelFeatures.getFont().getSize2D() + 2f ) );
			lbl.setAlignmentX( Component.LEFT_ALIGNMENT );
			panelFeatures.add( lbl );
			panelFeatures.add( Box.createVerticalStrut( 5 ) );

			final Collection< FeatureSpec< ?, ? > > featureSpecs = model.getFeatureSpecs( target );
			final List< ElementWrapper > elements = featureSpecs.stream()
					.map( ElementWrapper::new )
					.collect( Collectors.toList() );
			final FeatureTable< List< ElementWrapper >, ElementWrapper > featureTable =
					new FeatureTable<>(
							elements,
							List::size,
							List::get,
							ElementWrapper::getName,
							ElementWrapper::isFocused,
							ElementWrapper::setFocused,
							ElementWrapper::isUptodate );
			featureTable.getComponent().setAlignmentX( Component.LEFT_ALIGNMENT );
			featureTable.getComponent().setBackground( panelFeatures.getBackground() );
			panelFeatures.add( featureTable.getComponent() );
			panelFeatures.add( Box.createVerticalStrut( 10 ) );

			aggregator.add( featureTable );
			featureTable.selectionListeners().add( sl );
		}
	}

	private void displayConfigPanel( final FeatureSpec< ?, ? > spec )
	{
		panelConfig.removeAll();

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
		c.gridy++;
		infoPanel.add( infoLbl, c );

		infoLbl.setText(  "<html>" + spec.getInfo() +  "</html>"  );
		final FeatureProjectionSpec[] projections = spec.getProjectionSpecs();
		final StringBuilder projStr = new StringBuilder( ( projections.length == 1 )
				? "<html>Projection: <ul>"
				: "<html>Projections: <ul>" );
		for ( final FeatureProjectionSpec projSpec : projections )
		{
			projStr.append( "<li>" + projSpec.projectionName );
			projStr.append( "<br>- Dimension: " + projSpec.projectionDimension.toString() );
			switch(projSpec.multiplicity)
			{
			case ON_SOURCES:
				projStr.append( "<br>- <i>One value per source.</i>" );
				break;
			case ON_SOURCE_PAIRS:
				projStr.append( "<br>- <i>One value per source pair.</i>" );
				break;
			case SINGLE:
			default:
				break;
			}
			projStr.append( "</li>" );

		}
		projStr.append( "</ul></html>" );
		c.gridy++;
		final JLabel projLabel = new JLabel( projStr.toString() );
		infoPanel.add( projLabel, c );

		final Set< String > dependencies = new HashSet<>(); // TODO
		if ( !dependencies.isEmpty() )
		{
			final StringBuilder depStr = new StringBuilder();
			if ( dependencies.size() == 1 )
				depStr.append( "<html>Dependency: <ul>" );
			else
				depStr.append( "<html>Dependencies: <ul>" );
			for ( final String dep : dependencies )
				depStr.append( "<li>" + dep + "</li>" );
			depStr.append( "</ul></html>" );
			c.gridy++;
			final JLabel depsLabel = new JLabel( depStr.toString() );
			infoPanel.add( depsLabel, c );
		}
		panelConfig.add( infoPanel, BorderLayout.NORTH );

		final JComponent configPanel = null; // TODO
		if ( null != configPanel )
			panelConfig.add( configPanel, BorderLayout.CENTER );

		panelConfig.revalidate();
		panelConfig.repaint();
	}

	private List< StyleElement > styleElements( final Collection< Class< ? > > targets )
	{
		final List< StyleElement > elements = new ArrayList<>();
		elements.add( separator() );
		for ( final Class< ? > target : targets )
		{
			elements.add( label( target.getSimpleName() ) );
			final List< FeatureSpec< ?, ? > > featureSpecs =
					new ArrayList<>( model.getFeatureSpecs( target ) );
			featureSpecs.sort( ( fs1, fs2 ) -> fs1.getKey().compareTo( fs2.getKey() ) );
			for ( final FeatureSpec< ?, ? > spec : featureSpecs )
			{
				elements.add( booleanElement( spec.getKey(), () -> model.isSelected( spec ), ( s ) -> model.setSelected( spec, s ) ) );
			}
			elements.add( separator() );
		}
		return elements;
	}
	
	private static class ElementWrapper
	{
		private final FeatureSpec< ?, ? > fs;

		private boolean focused;

		private boolean uptodate;

		public ElementWrapper( final FeatureSpec< ?, ? > fs )
		{
			this.fs = fs;
		}

		public String getName()
		{
			return fs.getKey();
		}

		public boolean isFocused()
		{
			return focused;
		}

		public void setFocused( final boolean focused )
		{
			this.focused = focused;
		}

		public boolean isUptodate()
		{
			return uptodate;
		}
	}
}
