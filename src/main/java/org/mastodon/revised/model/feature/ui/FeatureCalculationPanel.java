package org.mastodon.revised.model.feature.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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

	private static final ImageIcon HELP_ICON = new ImageIcon( FeatureCalculationPanel.class.getResource( "help.png" ) );

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

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder( null );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		add( scrollPane, BorderLayout.CENTER );

		final JPanel panelFeatures = new JPanel();
		panelFeatures.setBorder( null );
		scrollPane.setViewportView( panelFeatures );
		final GridBagLayout gbl = new GridBagLayout();
		panelFeatures.setLayout( gbl );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;

		// Feed the feature panel.
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
			c.gridx++;
			c.weightx = 0.;
			panel.add( config, c );

			final JButton help = new JButton( HELP_ICON );
			help.setBorder( null );
			help.setBorderPainted( false );
			help.setContentAreaFilled( false );
			c.gridx++;
			c.weightx = 0.;
			panel.add( help, c );

			c.gridy++;
		}
		c.gridy++;
		panel.add( Box.createVerticalStrut( 15 ), c );
		c.gridy++;
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
			final boolean ok = computerService.compute( model, featureModel, selectedComputers, progressBar );
			return Boolean.valueOf( ok );
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
