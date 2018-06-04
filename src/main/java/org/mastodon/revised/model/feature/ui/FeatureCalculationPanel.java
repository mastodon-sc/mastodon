package org.mastodon.revised.model.feature.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.ui.ProgressListener;

public class FeatureCalculationPanel< AM extends AbstractModel< ?, ?, ? >, FC extends FeatureComputer< AM > > extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon COG_ICON = new ImageIcon( FeatureCalculationPanel.class.getResource( "cog.png" ) );

	private static final ImageIcon GO_ICON = new ImageIcon( FeatureCalculationPanel.class.getResource( "bullet_green.png" ) );

	private static final ImageIcon CANCEL_ICON = new ImageIcon( FeatureCalculationPanel.class.getResource( "cancel.png" ) );

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd --- HH:mm:ss" );

	private final FeatureComputerService< AM, FC > computerService;

	private final AM model;

	private final FeatureModel featureModel;

	private final MyProgressBar progressBar;

	private final Set< FC > selectedComputers;

	private final JButton btnCompute;

	private final JLabel lblComputationDate;

	private FeatureComputerWorker worker;

	private final JLabel lblModelModificationDate;

	private final JPanel panelConfig;

	public FeatureCalculationPanel( final FeatureComputerService< AM, FC > computerService, final AM model, final FeatureModel featureModel )
	{
		this.computerService = computerService;
		this.model = model;
		this.featureModel = featureModel;
		this.selectedComputers = new HashSet<>();

		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelComputation = new JPanel();
		add( panelComputation, BorderLayout.SOUTH );

		final GridBagLayout gbl_panelComputation = new GridBagLayout();
		gbl_panelComputation.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelComputation.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		panelComputation.setLayout( gbl_panelComputation );

		btnCompute = new JButton( "Compute", GO_ICON );
		final GridBagConstraints gbc_btnCompute = new GridBagConstraints();
		gbc_btnCompute.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnCompute.anchor = GridBagConstraints.CENTER;
		gbc_btnCompute.gridx = 0;
		gbc_btnCompute.gridy = 0;
		panelComputation.add( btnCompute, gbc_btnCompute );

		progressBar = new MyProgressBar();
		progressBar.setStringPainted( true );
		final GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.BOTH;
		gbc_progressBar.insets = new Insets( 5, 5, 5, 5 );
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 0;
		panelComputation.add( progressBar, gbc_progressBar );

		// Wire listener to compute button.
		btnCompute.addActionListener( ( e ) -> compute() );

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
		gbc_lblComputationDate.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblComputationDate.gridx = 1;
		gbc_lblComputationDate.gridy = 1;
		panelComputation.add( lblComputationDate, gbc_lblComputationDate );

		final JLabel lblLastModelModification = new JLabel( "Model last modified:" );
		final GridBagConstraints gbc_lblLastFeatureComputation_1 = new GridBagConstraints();
		gbc_lblLastFeatureComputation_1.anchor = GridBagConstraints.WEST;
		gbc_lblLastFeatureComputation_1.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblLastFeatureComputation_1.gridx = 0;
		gbc_lblLastFeatureComputation_1.gridy = 2;
		panelComputation.add( lblLastModelModification, gbc_lblLastFeatureComputation_1 );

		lblModelModificationDate = new JLabel( "Unknown." );
		final GridBagConstraints gbc_lblModelModificationDate = new GridBagConstraints();
		gbc_lblModelModificationDate.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblModelModificationDate.insets = new Insets( 5, 5, 5, 5 );
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
		panelFeatures.setBorder( null );
		scrollPaneFeatures.setViewportView( panelFeatures );
		final GridBagLayout gbl = new GridBagLayout();
		panelFeatures.setLayout( gbl );

		final JPanel panelRight = new JPanel();
		panelRight.setPreferredSize( new Dimension( 300, 300 ) );
		splitPane.setRightComponent( panelRight );
		panelRight.setLayout( new BorderLayout( 0, 0 ) );

		this.panelConfig = new JPanel();
		panelConfig.setLayout( new BorderLayout() );
		panelRight.add( panelConfig, BorderLayout.CENTER );

		// Feed the feature panel.
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		layoutComputers( panelFeatures, c, computerService.getFeatureComputers() );

		// Listen to graph changes.
		model.getGraph().addGraphChangeListener( () -> lblModelModificationDate.setText( now() ) );
	}

	private synchronized void compute()
	{
		enableComponents( this, false );

		if ( worker == null )
		{
			progressBar.setEnabled( true );
			btnCompute.setText( "Cancel" );
			btnCompute.setIcon( CANCEL_ICON );
			btnCompute.setEnabled( true );

			worker = new FeatureComputerWorker();
			worker.addPropertyChangeListener( new PropertyChangeListener()
			{

				@Override
				public void propertyChange( final PropertyChangeEvent evt )
				{
					if ( null == worker )
						return;

					if ( worker.isDone() && !worker.isCancelled() )
					{
						enableComponents( FeatureCalculationPanel.this, true );
						lblComputationDate.setText( now() );
						worker = null;
						btnCompute.setText( "Compute" );
						btnCompute.setIcon( GO_ICON );
					}
				}
			} );
			worker.execute();
		}
		else
		{
			worker.cancel( true );
			progressBar.clearStatus();
			progressBar.setString( "Canceled." );
			enableComponents( FeatureCalculationPanel.this, true );
			worker = null;
			btnCompute.setText( "Compute" );
			btnCompute.setIcon( GO_ICON );
		}
	}

	private static final String now()
	{
		return DATE_FORMAT.format( Calendar.getInstance().getTime() );
	}

	private void layoutComputers( final JPanel panel, final GridBagConstraints c, final Collection< FC > collection )
	{
		if ( collection.isEmpty() )
			return;

		c.gridx = 0;
		for ( final FC computer : collection )
		{
			final boolean selected = true;
			final JCheckBox checkBox = new JCheckBox( computer.getKey(), selected );
			checkBox.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					if ( checkBox.isSelected() )
						selectedComputers.add( computer );
					else
						selectedComputers.remove( computer );
				}
			} );
			if ( selected )
				selectedComputers.add( computer );

			c.gridx = 0;
			c.weightx = 1.;
			c.gridwidth = 1;
			panel.add( checkBox, c );

			final JButton config = new JButton( COG_ICON );
			config.setBorder( null );
			config.setBorderPainted( false );
			config.setContentAreaFilled( false );
			config.setMargin( new Insets( 0, 0, 0, 0 ) );
			config.addActionListener( ( e ) -> displayConfigPanel( computer ) );
			c.gridx++;
			c.weightx = 0.;
			panel.add( config, c );

			c.gridy++;
		}
		c.gridy++;
		panel.add( Box.createVerticalStrut( 15 ), c );
		c.gridy++;
	}

	private void displayConfigPanel( final FC computer )
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

		final JLabel title = new JLabel( computer.getKey() );
		title.setFont( getFont().deriveFont( Font.BOLD ) );
		c.gridy = 0;
		infoPanel.add( title, c );

		final JLabel infoLbl = new JLabel( "<html>" + computer.getHelpString() + "</html>" );
		infoLbl.setFont( getFont().deriveFont( Font.ITALIC ) );
		c.gridy++;
		infoPanel.add( infoLbl, c );

		final Set< String > dependencies = computer.getDependencies();
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

		final JComponent configPanel = computer.getConfigPanel();
		if ( null != configPanel )
			panelConfig.add( configPanel, BorderLayout.CENTER );

		panelConfig.revalidate();
		panelConfig.repaint();
	}

	public interface UpdateListener
	{
		public void featureValuesCalculated();
	}

	private class FeatureComputerWorker extends SwingWorker< Boolean, String >
	{

		@Override
		protected Boolean doInBackground() throws Exception
		{
			try
			{
				final boolean ok = computerService.compute( model, featureModel, selectedComputers, progressBar );
				return Boolean.valueOf( ok );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return Boolean.valueOf( false );
		}
	}

	private class MyProgressBar extends JProgressBar implements ProgressListener
	{

		private static final long serialVersionUID = 1L;

		public MyProgressBar()
		{
			super();
			setStringPainted( true );
		}

		@Override
		public void showStatus( final String string )
		{
			setString( string );
		}

		@Override
		public void showProgress( final int current, final int total )
		{
			setValue( ( int ) ( 100. * current / total ) );
		}

		@Override
		public void clearStatus()
		{
			setString( "" );
			setValue( 0 );
		}
	}

	private static final void enableComponents( final Container container, final boolean enable )
	{
		final Component[] components = container.getComponents();
		for ( final Component component : components )
		{
			component.setEnabled( enable );
			if ( component instanceof Container )
			{
				enableComponents( ( Container ) component, enable );
			}
		}
	}
}
