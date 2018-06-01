package org.mastodon.revised.model.feature.ui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;

public class FeatureCalculationDialog< AM extends AbstractModel< ?, ?, ? >, FC extends FeatureComputer< AM > > extends JDialog
{

	private static final long serialVersionUID = 1L;

	public FeatureCalculationDialog( final Frame owner, final FeatureComputerService< AM, FC > computerService, final AM model, final FeatureModel featureModel )
	{
		super( owner, "Feature Calculation", false );
		final FeatureCalculationPanel< AM, FC > panel = new FeatureCalculationPanel<>( computerService, model, featureModel );

		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		getContentPane().add( panel, BorderLayout.CENTER );
		pack();
	}
}
