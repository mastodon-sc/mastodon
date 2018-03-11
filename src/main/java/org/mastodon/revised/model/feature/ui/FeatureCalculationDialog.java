package org.mastodon.revised.model.feature.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.Context;

public class FeatureCalculationDialog< AM extends AbstractModel< ?, ?, ? > > extends JDialog
{

	private static final long serialVersionUID = 1L;

	public FeatureCalculationDialog( final Frame owner, final FeatureComputerService< AM > computerService, final AM model, final FeatureModel featureModel )
	{
		super( owner, "Feature Calculation", false );
		final FeatureCalculationPanel< AM > panel = new FeatureCalculationPanel<>( computerService, model, featureModel );

		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		getContentPane().add( panel, BorderLayout.CENTER );
		pack();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InvocationTargetException, InterruptedException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.US );

		final Context context = new org.scijava.Context();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Model model = new Model();

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final FeatureCalculationDialog< Model > dialog = new FeatureCalculationDialog<>( null, featureComputerService, model, model.getFeatureModel() );
				dialog.setVisible( true );
			}
		} );
	}
}
