package org.mastodon.feature.ui;

import java.util.Collection;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatusListener;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.feature.FeatureComputerService;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.revised.ui.util.EverythingDisablerAndReenabler;
import org.scijava.command.CommandService;

public class FeatureComputationController implements GraphChangeListener
{

	private final JDialog dialog;

	private final FeatureComputationPanel gui;

	private final FeatureComputerService computerService;

	private final FeatureComputationModel model;

	private final FeatureComputationStatusListener computationStatusListener;

	public FeatureComputationController( final FeatureComputerService computerService, final Collection< Class< ? > > targets )
	{
		this.computerService = computerService;
		model = createModel( targets );
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
				final Map< FeatureSpec< ?, ? >, Feature< ? > > computed =
						computerService.compute( model.getSelectedFeatureKeys() );
				SwingUtilities.invokeLater( () -> {
					gui.btnCancel.setVisible( false );
					gui.btnCompute.setVisible( true );
					reenabler.reenable();
					if ( !computerService.isCanceled() )
						model.setUptodate( computed.keySet() );
				} );
			};
		}.start();
	}

	private FeatureComputationModel createModel( final Collection< Class< ? > > targets )
	{
		final CommandService commandService = computerService.getContext().getService( CommandService.class );
		final FeatureComputationModel model = new FeatureComputationModel();
		for ( final FeatureSpec< ?, ? > spec : computerService.getFeatureSpecs() )
		{
			// Only add the features with specified targets.
			if ( targets.contains( spec.getTargetClass() ) )
			{
				model.put( spec.getTargetClass(), spec, computerService.getDependencies( spec ) );
				model.setSelected( spec, true );

				final FeatureComputer featureComputer = computerService.getFeatureComputerFor( spec );
				// Check visibility.
				final boolean visible = commandService.getCommand( featureComputer.getClass() ).isVisible();
				model.setVisible( spec, visible );
			}
		}
		return model;
	}

	@Override
	public void graphChanged()
	{
		model.setOutofdate();
		gui.repaint();
	}

	public JDialog getDialog()
	{
		return dialog;
	}
}
