package org.mastodon.revised.ui.coloring.feature;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;

public class FeatureColorModePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final FeatureModel featureModel;

	private final Class< ? extends Vertex< ? > > vertexClass;

	private final Class< ? extends Edge< ? > > edgeClass;

	private final FeatureColorMode mode;

	public FeatureColorModePanel( final FeatureColorMode mode, final FeatureModel featureModel, final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator, final Class< ? extends Vertex< ? > > vertexClass, final Class< ? extends Edge< ? > > edgeClass )
	{
		this.mode = mode;
		this.featureModel = featureModel;
		this.vertexClass = vertexClass;
		this.edgeClass = edgeClass;

		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 45, 45, 45, 45, 10, 45, 45, 45, 45 };

		setLayout( layout );
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
		final RadioButtonChoices< VertexColorMode > vertexColorModeSelector = new RadioButtonChoices<>( VertexColorMode.values() );
		addToLayout( new JLabel( "vertex color mode", JLabel.TRAILING ), vertexColorModeSelector, c );

		/*
		 * Vertex feature.
		 */

		final KeyChainPanel vertexFeatureKeySelector = new KeyChainPanel();
		addToLayout( new JLabel( "vertex feature", JLabel.TRAILING ), vertexFeatureKeySelector, c );

		vertexColorModeSelector.listeners().add( m -> mode.setVertexColorMode( m ) );
		vertexFeatureKeySelector.listeners().add( ( fk, pk ) -> mode.setVertexFeatureProjection( fk, pk ) );
		vertexColorModeSelector.listeners().add( m -> vertexFeatureKeySelector.regenCB1() );

		/*
		 * Vertex color map.
		 */

		final ColorMapSelector vertexColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "vertex colormap", JLabel.TRAILING ), vertexColorMapSelector, c );

		vertexColorMapSelector.listeners().add( cm -> mode.setVertexColorMap( cm ) );

		/*
		 * Vertex feature range.
		 */

		final FeatureRangeSelector vertexFeatureRangeSelector = new FeatureRangeSelector()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void autoscale()
			{
				final Class< ? > clazz = ( mode.getVertexColorMode().equals( VertexColorMode.VERTEX ) ) ? vertexClass : edgeClass;
				final String featureKey = mode.getVertexFeatureProjection()[ 0 ];
				final String projectionKey = mode.getVertexFeatureProjection()[ 1 ];
				final double[] minMax = rangeCalculator.computeMinMax( clazz, featureKey, projectionKey );
				if ( null == minMax )
					return;
				setMinMax( minMax[ 0 ], minMax[ 1 ] );
			}

		};
		addToLayout( new JLabel( "vertex range", JLabel.TRAILING ), vertexFeatureRangeSelector, c );

		vertexFeatureRangeSelector.listeners().add( mm -> mode.setVertexRange( mm[ 0 ], mm[ 1 ] ) );

		/*
		 * Listen to changes in vertex color mode and hide panels or not.
		 */

		vertexColorModeSelector.listeners.add( vcm -> {
			final boolean visible = !vcm.equals( VertexColorMode.NONE );
			vertexFeatureKeySelector.setVisible( visible );
			vertexColorMapSelector.setVisible( visible );
			vertexFeatureRangeSelector.setVisible( visible );
		} );

		/*
		 * Separator.
		 */

		c.gridx = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add( new JSeparator(), c );
		c.gridy++;
		c.gridwidth = 1;

		/*
		 * Listen to changes in the mode and forward them to the view.
		 */

		mode.updateListeners().add( () -> System.out.println( "Mode updated: " + mode.toString() ) ); // DEBUG
		final FeatureColorMode.UpdateListener l = new FeatureColorMode.UpdateListener()
		{

			@Override
			public void featureColorModeChanged()
			{
				vertexColorModeSelector.setSelected( mode.getVertexColorMode() );
				vertexFeatureKeySelector.setChain( mode.getVertexFeatureProjection()[ 0 ], mode.getVertexFeatureProjection()[ 1 ] );
				vertexColorMapSelector.setColorMap( mode.getVertexColorMap() );
				vertexFeatureRangeSelector.setMinMax( mode.getVertexRangeMin(), mode.getVertexRangeMax() );
			}
		};
		l.featureColorModeChanged();
		mode.updateListeners().add( l );
	}

	private void addToLayout( final JComponent comp1, final JComponent comp2, final GridBagConstraints c )
	{
		c.gridx = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.weightx = 0.0;
		add( comp1, c );
		c.gridx++;

		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1.0;
		add( comp2, c );
		c.gridy++;
	}

	private abstract class FeatureRangeSelector extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JFormattedTextField min;

		private final JFormattedTextField max;

		private final List< Consumer< double[] > > listeners = new ArrayList<>();

		private final JButton autoscale;

		public FeatureRangeSelector()
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );

			final NumberFormat format = DecimalFormat.getNumberInstance();
			add( new JLabel( "min", JLabel.TRAILING ) );
			min = new JFormattedTextField( format );
			min.setColumns( 6 );
			min.setHorizontalAlignment( JLabel.TRAILING );
			min.setValue( 0. );
			add( min );
			add( new JLabel( "max", JLabel.TRAILING ) );
			max = new JFormattedTextField( format );
			max.setColumns( 6 );
			max.setHorizontalAlignment( JLabel.TRAILING );
			max.setValue( 1. );
			add( max );
			autoscale = new JButton( "autoscale" );
			add( autoscale );

			final FocusListener fl = new FocusAdapter()
			{
				@Override
				public void focusGained( final FocusEvent e )
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							( ( JFormattedTextField ) e.getSource() ).selectAll();
						}
					} );
				}
			};
			min.addFocusListener( fl );
			max.addFocusListener( fl );

			autoscale.addActionListener( e -> new Thread( () -> preAutoscale(), "Autoscale calculation thread." ).start() );

			final PropertyChangeListener l = ( e ) -> notifyListeners();
			min.addPropertyChangeListener( "value", l );
			max.addPropertyChangeListener( "value", l );
		}

		private void preAutoscale()
		{
			min.setEnabled( false );
			max.setEnabled( false );
			autoscale.setEnabled( false );
			autoscale.setText( "calculating..." );
			try
			{
				autoscale();
			}
			finally
			{
				min.setEnabled( true );
				max.setEnabled( true );
				autoscale.setText( "autoscale" );
				autoscale.setEnabled( true );
			}
		}

		private void notifyListeners()
		{
			final double l1 = ( ( Number ) min.getValue() ).doubleValue();
			final double l2 = ( ( Number ) max.getValue() ).doubleValue();
			final double[] val = new double[] {
					Math.min( l1, l2 ),
					Math.max( l1, l2 )
			};
			listeners.forEach( c -> c.accept( val ) );
		}

		public abstract void autoscale();

		public void setMinMax( final double min, final double max )
		{
			final double l1 = Math.min( min, max );
			final double l2 = Math.max( min, max );
			this.min.setValue( Double.valueOf( l1 ) );
			this.max.setValue( Double.valueOf( l2 ) );
		}

		public List< Consumer< double[] > > listeners()
		{
			return listeners;
		}
	}

	private final class ColorMapSelector extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final List< Consumer< String > > listeners = new ArrayList<>();

		private final JComboBox< String > cb;

		public ColorMapSelector( final Collection< String > names )
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 10 ) );
			cb = new JComboBox<>( names.toArray( new String[] {} ) );
			add( cb );
			final ColorMapPainter painter = new ColorMapPainter( cb );
			add( painter );
			cb.addItemListener( ( e ) -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					listeners.forEach( l -> l.accept( ( String ) cb.getSelectedItem() ) );
					painter.repaint();
				}
			} );
		}

		public void setColorMap( final String name )
		{
			cb.setSelectedItem( name );
		}

		public List< Consumer< String > > listeners()
		{
			return listeners;
		}
	}

	private static final class ColorMapPainter extends JComponent
	{

		private static final long serialVersionUID = 1L;

		private final JComboBox< String > choices;

		public ColorMapPainter( final JComboBox< String > choices )
		{
			this.choices = choices;
		}

		@Override
		protected void paintComponent( final Graphics g )
		{
			super.paintComponent( g );
			if ( !isEnabled() )
				return;

			final String cname = ( String ) choices.getSelectedItem();
			final ColorMap cmap = ColorMap.getColorMap( cname );
			final int w = getWidth();
			final int h = getHeight();
			final int lw = ( int ) ( 0.9 * w );
			for ( int i = 0; i < lw; i++ )
			{
				g.setColor( new Color( cmap.get( ( double ) i / lw ), true ) );
				g.drawLine( i, 0, i, h );
			}

			// NaN.
			g.setColor( new Color( cmap.get( Double.NaN ) ) );
			g.fillRect( ( int ) ( 0.92 * w ), 0, ( int ) ( 0.08 * w ), h );
		}

		@Override
		public Dimension getPreferredSize()
		{
			final Dimension dimension = super.getPreferredSize();
			dimension.height = 20;
			dimension.width = 100;
			return dimension;
		}

		@Override
		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}
	}

	private final class RadioButtonChoices< E > extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final List< Consumer< E > > listeners = new ArrayList<>();

		private final Map< E, JToggleButton > buttons;

		public RadioButtonChoices( final E[] choices )
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );
			buttons = new HashMap<>();
			final ButtonGroup group = new ButtonGroup();
			for ( final E c : choices )
			{
				final JRadioButton button = new JRadioButton( c.toString() );
				button.addActionListener( ( e ) -> listeners.forEach( l -> l.accept( c ) ) );
				group.add( button );
				add( button );
				buttons.put( c, button );
			}
		}

		public void setSelected( final E c )
		{
			buttons.get( c ).setSelected( true );
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

		private final List< BiConsumer< String, String > > listeners = new ArrayList<>();

		public KeyChainPanel()
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );
			cb1 = new JComboBox<>();
			cb1.addItemListener( ( e ) -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					regenCB2();
					notifyListers();
				}
			} );
			add( cb1 );
			arrow = new JLabel( "\u2192" );
			add( arrow );
			cb2 = new JComboBox<>();
			cb2.addItemListener( ( e ) -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
					notifyListers();
			} );
			add( cb2 );
		}

		private void notifyListers()
		{
			final String fk = ( String ) cb1.getSelectedItem();
			final String pk = ( String ) cb2.getSelectedItem();
			listeners.forEach( ( bc ) -> bc.accept( fk, pk ) );
		}

		public List< BiConsumer< String, String > > listeners()
		{
			return listeners;
		}

		public void setChain( final String c1, final String c2 )
		{
			regenCB1();
			cb1.setSelectedItem( c1 );
			cb2.setSelectedItem( c2 );
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
			notifyListers();
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
