package org.mastodon.revised.ui.coloring.feature;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;

public class FeatureColorModePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final FeatureModel featureModel;

	private final Class< ? extends Vertex< ? > > vertexClass;

	private final Class< ? extends Edge< ? > > edgeClass;

	private final FeatureColorMode mode;

	public FeatureColorModePanel( final FeatureColorMode mode, final FeatureModel featureModel, final Class< ? extends Vertex< ? > > vertexClass, final Class< ? extends Edge< ? > > edgeClass )
	{
		super( new GridBagLayout() );
		this.mode = mode;
		this.featureModel = featureModel;
		this.vertexClass = vertexClass;
		this.edgeClass = edgeClass;

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		/*
		 * Vertex color mode.
		 */
		final RadioButtonChoices< VertexColorMode > vModePanel = new RadioButtonChoices<>( VertexColorMode.values() );
		addToLayout( new JLabel( "vertex color mode" ), vModePanel, c );

		/*
		 * Vertex feature.
		 */

		final KeyChainPanel kcp = new KeyChainPanel();
		addToLayout( new JLabel( "vertex feature" ), kcp, c );

		vModePanel.listeners().add( m -> mode.setVertexColorMode( m ) );
		vModePanel.listeners().add( m -> kcp.regenCB1() );
	}

	private void addToLayout( final JComponent comp1, final JComponent comp2, final GridBagConstraints c )
	{
		c.gridx = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.0;
		add( comp1, c );
		c.gridx++;

		c.anchor = GridBagConstraints.LINE_END;
		c.weightx = 1.0;
		add( comp2, c );
		c.gridy++;
	}

	private final class RadioButtonChoices< E > extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final List< Consumer< E > > listeners = new ArrayList<>();

		public RadioButtonChoices( final E[] choices )
		{
			final ButtonGroup group = new ButtonGroup();
			for ( final E c : choices )
			{
				final JRadioButton button = new JRadioButton( c.toString() );
				button.addActionListener( ( e ) -> listeners.forEach( l -> l.accept( c ) ) );
				group.add( button );
				add( button );
			}
		}

		public List< Consumer< E > > listeners()
		{
			return listeners;
		}
	}

	private final class KeyChainPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JComboBox< String > cb1;

		private final JComboBox< String > cb2;

		private Class< ? > previousClass;

		private final JLabel arrow;

		public KeyChainPanel()
		{
			cb1 = new JComboBox<>();
			cb1.addItemListener( (e) ->
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
					regenCB2();
			});
			add( cb1 );
			arrow = new JLabel( "\u2192" );
			add( arrow );
			cb2 = new JComboBox<>();
			add( cb2 );
		}

		public void regenCB1()
		{
			final Class< ? > clazz;
			switch ( mode.getVertexColorMode() )
			{
			case INCOMING_EDGE:
			case OUTGOING_EDGE:
				clazz = edgeClass;
				setVisible( true );
				break;
			case NONE:
			default:
				clazz = null;
				setVisible( false );
				break;
			case VERTEX:
				clazz = vertexClass;
				setVisible( true );
				break;
			}
			if ( previousClass == clazz )
				return;
			previousClass = clazz;
			if ( null == clazz )
				return;

			final List< String > featureKeys = featureModel.getFeatureSet( clazz ).stream().map( Feature::getKey ).collect( Collectors.toList() );
			featureKeys.sort( null );
			cb1.setModel( new DefaultComboBoxModel<>( featureKeys.toArray( new String[] {} ) ) );
			regenCB2();
		}

		private void regenCB2()
		{
			final Set< String > keySet = featureModel.getFeature( ( String ) cb1.getSelectedItem() ).getProjections().keySet();
			final List< String > list = new ArrayList<>( keySet );
			list.sort( null );
			cb2.setModel( new DefaultComboBoxModel<>( list.toArray( new String[] {} ) ) );
		}
	}
}
