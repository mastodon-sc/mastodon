package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.revised.mamut.feature.DefaultMamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.ProgressListener;
import org.scijava.Context;

public class FeatureComputersPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon COG_ICON = new ImageIcon( FeatureComputersPanel.class.getResource( "cog.png" ) );

	private static final ImageIcon HELP_ICON = new ImageIcon( FeatureComputersPanel.class.getResource( "help.png" ) );

	private static final ImageIcon GO_ICON = new ImageIcon( FeatureComputersPanel.class.getResource( "bullet_green.png" ) );

	private static final ImageIcon CANCEL_ICON = new ImageIcon( FeatureComputersPanel.class.getResource( "cancel.png" ) );

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	private final FeatureComputerService< Model > computerService;

	private final Model model;

	private final MyProgressBar progressBar;

	private final Set< FeatureComputer< Model > > selectedComputers;

	private final JButton btnCompute;

	private final JLabel lblComputationDate;

	private FeatureComputerWorker worker;

	public FeatureComputersPanel( final FeatureComputerService< Model > computerService, final Model model )
	{
		this.computerService = computerService;
		this.model = model;
		this.selectedComputers = new HashSet<>();

		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelComputation = new JPanel();
		add( panelComputation, BorderLayout.SOUTH );

		btnCompute = new JButton( "Compute", GO_ICON );

		progressBar = new MyProgressBar();
		progressBar.setStringPainted( true );

		lblComputationDate = new JLabel( "Last feature computation: Never." );
		final JLabel lblModelModificationDate = new JLabel( "Model last modified: Unknown." );
		final GroupLayout gl_panelComputation = new GroupLayout( panelComputation );
		gl_panelComputation.setHorizontalGroup(
				gl_panelComputation.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelComputation.createSequentialGroup()
								.addContainerGap()
								.addGroup( gl_panelComputation.createParallelGroup( Alignment.LEADING )
										.addGroup( gl_panelComputation.createSequentialGroup()
												.addComponent( btnCompute )
												.addPreferredGap( ComponentPlacement.RELATED )
												.addComponent( progressBar, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE ) )
										.addComponent( lblComputationDate, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE )
										.addComponent( lblModelModificationDate, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE ) )
								.addContainerGap() ) );
		gl_panelComputation.setVerticalGroup(
				gl_panelComputation.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelComputation.createSequentialGroup()
								.addContainerGap()
								.addGroup( gl_panelComputation.createParallelGroup( Alignment.TRAILING, false )
										.addComponent( progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
										.addComponent( btnCompute, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) )
								.addPreferredGap( ComponentPlacement.UNRELATED )
								.addComponent( lblComputationDate )
								.addComponent( lblModelModificationDate )
								.addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) ) );
		panelComputation.setLayout( gl_panelComputation );

		final JPanel panelTitle = new JPanel();
		add( panelTitle, BorderLayout.NORTH );

		final JLabel lblTitle = new JLabel( "Features available for computation:" );
		lblTitle.setFont( getFont().deriveFont( Font.BOLD ) );
		final GroupLayout gl_panelTitle = new GroupLayout( panelTitle );
		gl_panelTitle.setHorizontalGroup(
				gl_panelTitle.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelTitle.createSequentialGroup()
								.addContainerGap()
								.addComponent( lblTitle, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE )
								.addContainerGap() ) );
		gl_panelTitle.setVerticalGroup(
				gl_panelTitle.createParallelGroup( Alignment.LEADING )
						.addGroup( gl_panelTitle.createSequentialGroup()
								.addGap( 5 )
								.addComponent( lblTitle, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE )
								.addGap( 0, 0, Short.MAX_VALUE ) ) );
		panelTitle.setLayout( gl_panelTitle );

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

		// Wire listener to compute button.
		btnCompute.addActionListener( ( e ) -> compute() );

		// Wire listener to graph.
		model.getGraph().addGraphChangeListener( new GraphChangeListener()
		{

			@Override
			public void graphChanged()
			{
				lblModelModificationDate.setText( "Model last modified: " + now() );
			}
		} );
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
						enableComponents( FeatureComputersPanel.this, true );
						lblComputationDate.setText( "Last feature computation: " + now() );
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
			enableComponents( FeatureComputersPanel.this, true );
			worker = null;
			btnCompute.setText( "Compute" );
			btnCompute.setIcon( GO_ICON );
		}
	}

	private static final String now()
	{
		return DATE_FORMAT.format( Calendar.getInstance().getTime() );
	}

	private void layoutComputers( final JPanel panel, final GridBagConstraints c, final Collection< FeatureComputer< Model > > set )
	{
		if ( set.isEmpty() )
			return;

		c.gridx = 0;
		for ( final FeatureComputer< Model > computer : set )
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
			c.gridy++;

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
		}
		c.gridy++;
		panel.add( Box.createVerticalStrut( 15 ), c );
		c.gridy++;
	}

	private class FeatureComputerWorker extends SwingWorker< Boolean, String >
	{

		@Override
		protected Boolean doInBackground() throws Exception
		{
			final boolean ok = computerService.compute( model, selectedComputers, progressBar );
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

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InvocationTargetException, InterruptedException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.US );

		final Context context = new org.scijava.Context();
		final DefaultMamutFeatureComputerService featureComputerService = new DefaultMamutFeatureComputerService();
		context.inject( featureComputerService );
		featureComputerService.initialize();

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{

				final JFrame frame = new JFrame( "Test" );
				final FeatureComputersPanel panel = new FeatureComputersPanel( featureComputerService, new Model() );
				frame.getContentPane().add( panel );
				frame.setSize( 400, 400 );
				frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
				frame.setVisible( true );
			}
		} );
	}
}
