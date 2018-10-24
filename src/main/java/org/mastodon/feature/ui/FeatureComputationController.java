package org.mastodon.feature.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatusListener;
import org.mastodon.feature.FeatureComputerService;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.revised.ui.util.EverythingDisablerAndReenabler;

public class FeatureComputationController implements GraphChangeListener
{

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd --- HH:mm:ss" );

	private final JDialog dialog;

	private final FeatureComputationPanel gui;

	private final FeatureComputerService computerService;

	private final FeatureComputationModel model;

	private final FeatureComputationStatusListener computationStatusListener;

	public FeatureComputationController( final FeatureSpecsService specsService, final FeatureComputerService computerService, final Collection< Class< ? > > targets )
	{
		this.computerService = computerService;
		model = createModel( specsService, targets );
		dialog = new JDialog( ( JFrame ) null, "Feature calculation" );
		gui = new FeatureComputationPanel( model, targets );
		dialog.getContentPane().add( gui );
		dialog.pack();

		gui.btnCompute.addActionListener( ( e ) -> compute() );
		gui.btnCancel.addActionListener( ( e ) -> cancel() );

		gui.progressBar.setString( "" );
		computationStatusListener = new FeatureComputationStatusListener()
		{
			@Override
			public void status( final String status )
			{
				SwingUtilities.invokeLater( () -> gui.progressBar.setString( status ) );
			}

			@Override
			public void progress( final double progress )
			{
				SwingUtilities.invokeLater( () -> gui.progressBar.setValue( ( int ) ( 100 * progress ) ) );
			}

			@Override
			public void clear()
			{
				SwingUtilities.invokeLater( () -> {
					gui.progressBar.setValue( 0 );
					gui.progressBar.setString( "" );
				} );
			}
		};

	}

	public FeatureComputationStatusListener getComputationStatusListener()
	{
		return computationStatusListener;
	}

	private void cancel()
	{
		computerService.cancel( "User pressed cancel button." );
	}

	private synchronized void compute()
	{
		final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler( gui,
				new Class[] { JLabel.class, JProgressBar.class } );
		reenabler.disable();

		gui.btnCancel.setEnabled( true );
		gui.btnCancel.setVisible( true );
		gui.btnCompute.setVisible( false );
		new Thread( "Feature computation thread" )
		{
			@Override
			public void run()
			{
				computerService.compute( model.getSelectedFeatureKeys() );
				SwingUtilities.invokeLater( () -> {
					gui.btnCancel.setVisible( false );
					gui.btnCompute.setVisible( true );
					reenabler.reenable();
					if ( !computerService.isCanceled() )
						gui.lblComputationDate.setText( now() );
				} );
			};
		}.start();
	}

	private FeatureComputationModel createModel( final FeatureSpecsService featureSpecsService, final Collection< Class< ? > > targets )
	{
		final FeatureComputationModel model = new FeatureComputationModel();
		for ( final Class< ? > target : targets )
		{
			final List< FeatureSpec< ?, ? > > fs = new ArrayList<>( featureSpecsService.getSpecs( target ) );
			// Only add the feature that can be computed.
			for ( final FeatureSpec< ?, ? > f : fs )
			{
				if ( null != computerService.getFeatureComputerFor( f ) )
				{
					model.put( target, f );
					model.setSelected( f.getKey(), true );
				}
			}
		}
		return model;
	}

	@Override
	public void graphChanged()
	{
		gui.lblModelModificationDate.setText( now() );
	}

	private static final String now()
	{
		return DATE_FORMAT.format( Calendar.getInstance().getTime() );
	}

	public JDialog getDialog()
	{
		return dialog;
	}
}
