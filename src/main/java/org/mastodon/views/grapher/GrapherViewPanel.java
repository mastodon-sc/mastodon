package org.mastodon.views.grapher;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.util.FeatureUtils;

public class GrapherViewPanel< V, E > extends JPanel
{

	private static final long serialVersionUID = 1L;

	public GrapherViewPanel(
			final FeatureModel featureModel,
			final Class< V > vertexClass,
			final Class< E > edgeClass )
	{
		setLayout( new BorderLayout( 5, 5 ) );

		final JTabbedPane sidePane = new JTabbedPane( JTabbedPane.TOP );

		final GrapherSidePanel vertexSidePanel = new GrapherSidePanel();
		final GrapherSidePanel edgeSidePanel = new GrapherSidePanel();
		sidePane.add( vertexClass.getName(), vertexSidePanel );
		sidePane.add( edgeClass.getName(), edgeSidePanel );

		final FeatureModelListener featureModelListener = () -> {
			vertexSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, vertexClass ) );
			edgeSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, edgeClass ) );
		};
		featureModel.listeners().add( featureModelListener );
		featureModelListener.featureModelChanged();

		sidePane.setSelectedIndex( 0 );
		add( sidePane, BorderLayout.WEST );
	}
}
