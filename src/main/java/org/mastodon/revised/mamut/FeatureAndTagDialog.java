package org.mastodon.revised.mamut;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;

public class FeatureAndTagDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	public FeatureAndTagDialog( final JFrame owner, final Model model, final MamutFeatureComputerService computerService )
	{
		super( owner, "Features and tags" );

		// Feature computing panel.
		final FeatureComputersPanel featureComputersPanel = new FeatureComputersPanel( computerService, model );

		// Tag panel.
		// TODO

		// Tabbed pane.
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		tabbedPane.add( "features", featureComputersPanel );
//		tabbedPane.add( "tags", tagsPanel );
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
		pack();
	}

}