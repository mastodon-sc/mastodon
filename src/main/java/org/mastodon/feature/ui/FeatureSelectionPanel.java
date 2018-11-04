package org.mastodon.feature.ui;

import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mastodon.feature.FeatureSpec;

public class FeatureSelectionPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private Set< FeatureSpec< ?, ? > > featureSpecs;

	private final JComboBox< FeatureSpec< ?, ? > > cbFeatures;

	private final JComboBox< String > cbProjections;

	private final JLabel lblArrow;

	private final JComboBox< Integer > cbSource1;

	private final JLabel lblAnd;

	private final JComboBox< Integer > cbSource2;

	public FeatureSelectionPanel( final Set< FeatureSpec< ?, ? > > featureSpecs )
	{
		final BoxLayout layout = new BoxLayout( this, BoxLayout.LINE_AXIS );
		setLayout( layout );

		// Feature CB.
		this.cbFeatures = new JComboBox<>();
		add( cbFeatures );
		add( Box.createHorizontalStrut( 5 ) );

		// Arrow label
		this.lblArrow = new JLabel( "\u2192" );
		add( lblArrow );
		add( Box.createHorizontalStrut( 5 ) );

		// Projection CB.
		this.cbProjections = new JComboBox<>();
		add( cbProjections );
		add( Box.createHorizontalStrut( 5 ) );

		// Source index 1.
		this.cbSource1 = new JComboBox<>();
		add( cbSource1 );

		// & label.
		this.lblAnd = new JLabel( "&" );
		add( lblAnd );

		// Source index 2.
		this.cbSource2 = new JComboBox<>();
		add( cbSource2 );

		setFeatureSpects( featureSpecs );
	}

	private void setFeatureSpects( final Set< FeatureSpec< ?, ? > > featureSpecs )
	{
		if ( null == this.featureSpecs || !this.featureSpecs.equals( featureSpecs ) )
		{
			this.featureSpecs = featureSpecs;
			refresh();
		}
	}

	private void refresh()
	{
		// TODO Auto-generated method stub

	}

	private class FeatureSpectComboBoxModel extends AbstractListModel< FeatureSpec< ?, ? > >
	{

		private static final long serialVersionUID = 1L;

		private final List< FeatureSpec< ?, ? > > featureSpecs;

		public FeatureSpectComboBoxModel( final List< FeatureSpec< ?, ? > > featureSpecs )
		{
			this.featureSpecs = featureSpecs;
		}

		@Override
		public int getSize()
		{
			return featureSpecs.size();
		}

		@Override
		public FeatureSpec< ?, ? > getElementAt( final int index )
		{
			return featureSpecs.get( index );
		}

	}
}
