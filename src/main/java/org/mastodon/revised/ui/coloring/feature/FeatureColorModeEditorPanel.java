package org.mastodon.revised.ui.coloring.feature;

import java.awt.Color;
import java.awt.Component;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;

/**
 * JPanel to edit a single {@link FeatureColorMode}.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureColorModeEditorPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final FeatureModel featureModel;

	private final Class< ? extends Vertex< ? > > vertexClass;

	private final Class< ? extends Edge< ? > > edgeClass;

	private final FeatureColorMode mode;

	private EdgeFeatureKeySelector edgeFeatureKeySelector;

	private final VertexFeatureKeySelector vertexFeatureKeySelector;

	private final Map< String, Collection< String > > vertexFeatureKeys;

	private final Map< String, Collection< String > > edgeFeatureKeys;

	private final JLabel warningLabel;

	public FeatureColorModeEditorPanel(
			final FeatureColorMode mode,
			final FeatureModel featureModel,
			final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator,
			final Class< ? extends Vertex< ? > > vertexClass,
			final Map< String, Collection<String> > vertexFeatureKeys,
			final Class< ? extends Edge< ? > > edgeClass,
			final Map< String, Collection<String> > edgeFeatureKeys )
	{
		this.mode = mode;
		this.featureModel = featureModel;
		this.vertexClass = vertexClass;
		this.vertexFeatureKeys = vertexFeatureKeys;
		this.edgeClass = edgeClass;
		this.edgeFeatureKeys = edgeFeatureKeys;

		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 45, 45, 45, 45, 45, 10, 45, 45, 45, 45, 45 };

		setLayout( layout );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		/*
		 * Message.
		 */

		c.gridwidth = 2;
		this.warningLabel = new JLabel();
		add( warningLabel, c );
		c.gridwidth = 1;
		c.gridy++;

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
		 * Vertex color mode.
		 */
		final ModeSelector< VertexColorMode > vertexColorModeSelector = new ModeSelector<>( VertexColorMode.values() );
		addToLayout( new JLabel( "vertex color mode", JLabel.TRAILING ), vertexColorModeSelector, c );

		/*
		 * Vertex feature.
		 */

		this.vertexFeatureKeySelector = new VertexFeatureKeySelector();
		vertexFeatureKeySelector.regen( true );
		addToLayout( new JLabel( "vertex feature", JLabel.TRAILING ), vertexFeatureKeySelector, c );

		vertexColorModeSelector.listeners().add( m -> mode.setVertexColorMode( m ) );
		vertexFeatureKeySelector.listeners().add( ( fk, pk ) -> mode.setVertexFeatureProjection( fk, pk ) );
		vertexColorModeSelector.listeners().add( m -> vertexFeatureKeySelector.regen( false ) );

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
		 * Separator.
		 */

		c.gridx = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add( new JSeparator(), c );
		c.gridy++;
		c.gridwidth = 1;

		/*
		 * Edge color mode.
		 */
		final ModeSelector< EdgeColorMode > edgeColorModeSelector = new ModeSelector<>( EdgeColorMode.values() );
		addToLayout( new JLabel( "edge color mode", JLabel.TRAILING ), edgeColorModeSelector, c );

		/*
		 * Edge feature.
		 */

		this.edgeFeatureKeySelector = new EdgeFeatureKeySelector();
		edgeFeatureKeySelector.regen( true );
		addToLayout( new JLabel( "edge feature", JLabel.TRAILING ), edgeFeatureKeySelector, c );

		edgeColorModeSelector.listeners().add( m -> mode.setEdgeColorMode( m ) );
		edgeFeatureKeySelector.listeners().add( ( fk, pk ) -> mode.setEdgeFeatureProjection( fk, pk ) );
		edgeColorModeSelector.listeners().add( m -> edgeFeatureKeySelector.regen( false ) );

		/*
		 * Edge color map.
		 */

		final ColorMapSelector edgeColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "edge colormap", JLabel.TRAILING ), edgeColorMapSelector, c );

		edgeColorMapSelector.listeners().add( cm -> mode.setEdgeColorMap( cm ) );

		/*
		 * Edge feature range.
		 */

		final FeatureRangeSelector edgeFeatureRangeSelector = new FeatureRangeSelector()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void autoscale()
			{
				final Class< ? > clazz = ( mode.getEdgeColorMode().equals( EdgeColorMode.EDGE ) ) ? edgeClass : vertexClass;
				final String featureKey = mode.getEdgeFeatureProjection()[ 0 ];
				final String projectionKey = mode.getEdgeFeatureProjection()[ 1 ];
				final double[] minMax = rangeCalculator.computeMinMax( clazz, featureKey, projectionKey );
				if ( null == minMax )
					return;
				setMinMax( minMax[ 0 ], minMax[ 1 ] );
			}
		};
		addToLayout( new JLabel( "edge range", JLabel.TRAILING ), edgeFeatureRangeSelector, c );

		edgeFeatureRangeSelector.listeners().add( mm -> mode.setEdgeRange( mm[ 0 ], mm[ 1 ] ) );

		/*
		 * Listen to changes in vertex color mode and hide panels or not.
		 */

		final Consumer< VertexColorMode > vv = vcm -> {
			final boolean visible = !vcm.equals( VertexColorMode.NONE );
			vertexFeatureKeySelector.setVisible( visible );
			vertexColorMapSelector.setVisible( visible );
			vertexFeatureRangeSelector.setVisible( visible );
		};
		vertexColorModeSelector.listeners.add( vv );

		/*
		 * Listen to changes in edge color mode and hide panels or not.
		 */

		final Consumer< EdgeColorMode > ve = vcm -> {
			final boolean visible = !vcm.equals( EdgeColorMode.NONE );
			edgeFeatureKeySelector.setVisible( visible );
			edgeColorMapSelector.setVisible( visible );
			edgeFeatureRangeSelector.setVisible( visible );
		};
		edgeColorModeSelector.listeners.add( ve );

		/*
		 * Listen to changes in the mode and forward them to the view.
		 */

		final FeatureColorMode.UpdateListener l = new FeatureColorMode.UpdateListener()
		{

			@Override
			public void featureColorModeChanged()
			{

				vertexColorModeSelector.setSelected( mode.getVertexColorMode() );
				vertexColorMapSelector.setColorMap( mode.getVertexColorMap() );
				vertexFeatureRangeSelector.setMinMax( mode.getVertexRangeMin(), mode.getVertexRangeMax() );
				edgeColorModeSelector.setSelected( mode.getEdgeColorMode() );
				edgeColorMapSelector.setColorMap( mode.getEdgeColorMap() );
				edgeFeatureRangeSelector.setMinMax( mode.getEdgeRangeMin(), mode.getEdgeRangeMax() );
				vv.accept( mode.getVertexColorMode() );
				ve.accept( mode.getEdgeColorMode() );
				echoModeValidity(  );
				edgeFeatureKeySelector.setFeatureKeys( mode.getEdgeFeatureProjection()[ 0 ], mode.getEdgeFeatureProjection()[ 1 ] );
				vertexFeatureKeySelector.setFeatureKeys( mode.getVertexFeatureProjection()[ 0 ], mode.getVertexFeatureProjection()[ 1 ] );
			}
		};
		l.featureColorModeChanged();
		mode.updateListeners().add( l );

		// Listen to changes in the feature model.
		featureModel.listeners().add( () -> {
			final String[] vertexFeatureProjection = mode.getVertexFeatureProjection();
			final String[] edgeFeatureProjection = mode.getEdgeFeatureProjection();
			vertexFeatureKeySelector.regen( true );
			edgeFeatureKeySelector.regen( true );
			echoModeValidity(  );
			vertexFeatureKeySelector.setFeatureKeys( vertexFeatureProjection[ 0 ], vertexFeatureProjection[ 1 ] );
			edgeFeatureKeySelector.setFeatureKeys( edgeFeatureProjection[ 0 ], edgeFeatureProjection[ 1 ] );
		} );
	}

	private void echoModeValidity()
	{
		if ( !mode.isValid( featureModel, vertexClass, edgeClass ) )
		{
			final Set< String > requiredVertexFeatures = new HashSet<>();
			final Set< String > requiredEdgeFeatures = new HashSet<>();
			switch ( mode.getVertexColorMode() )
			{
			case INCOMING_EDGE:
			case OUTGOING_EDGE:
				if ( !isIn( featureModel, mode.getVertexFeatureProjection(), edgeClass ) )
					requiredEdgeFeatures.add( mode.getVertexFeatureProjection()[ 0 ] + " \u2192 " + mode.getVertexFeatureProjection()[ 1 ] );
				break;
			case VERTEX:
				if ( !isIn( featureModel, mode.getVertexFeatureProjection(), vertexClass ) )
					requiredVertexFeatures.add( mode.getVertexFeatureProjection()[ 0 ] + " \u2192 " + mode.getVertexFeatureProjection()[ 1 ] );
				break;
			case NONE:
			default:
			}
			switch ( mode.getEdgeColorMode() )
			{
			case EDGE:
				if ( !isIn( featureModel, mode.getEdgeFeatureProjection(), edgeClass ) )
					requiredEdgeFeatures.add( mode.getEdgeFeatureProjection()[ 0 ] + " \u2192 " + mode.getEdgeFeatureProjection()[ 1 ] );
				break;
			case SOURCE_VERTEX:
			case TARGET_VERTEX:
				if ( !isIn( featureModel, mode.getEdgeFeatureProjection(), vertexClass ) )
					requiredVertexFeatures.add( mode.getEdgeFeatureProjection()[ 0 ] + " \u2192 " + mode.getEdgeFeatureProjection()[ 1 ] );
				break;
			case NONE:
			default:
				break;
			}

			final StringBuilder warning = new StringBuilder();
			warning.append( "<html>"
					+ "Some features are missing for this mode."
					+ "<p>"
					+ "Please compute features over the current model. This feature color mode "
					+ "requires:"
					+ "<ul>" );
			if ( !requiredVertexFeatures.isEmpty() )
			{
				warning.append( "For spots: <ul>" );
				for ( final String f : requiredVertexFeatures )
					warning.append( "<li>" + f + "</li>" );
				warning.append( "</ul>" );
			}
			if ( !requiredEdgeFeatures.isEmpty() )
			{
				warning.append( "For links: <ul>" );
				for ( final String f : requiredEdgeFeatures )
					warning.append( "<li>" + f + "</li>" );
				warning.append( "</ul>" );
			}
			warning.append( "</ul></html>" );
			warningLabel.setText( warning.toString() );
		}
		else
		{
			warningLabel.setText( "" );
		}
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
			final int lw = ( int ) ( 0.85 * w );
			for ( int i = 0; i < lw; i++ )
			{
				g.setColor( new Color( cmap.get( ( double ) i / lw ), true ) );
				g.drawLine( i, 0, i, h );
			}

			// NaN.
			g.setColor( new Color( cmap.get( Double.NaN ) ) );
			g.fillRect( ( int ) ( 0.9 * w ), 0, ( int ) ( 0.1 * w ), h );
		}

		@Override
		public Dimension getPreferredSize()
		{
			final Dimension dimension = super.getPreferredSize();
			dimension.height = 20;
			dimension.width = 150;
			return dimension;
		}

		@Override
		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}
	}

	private final class ModeSelector< E > extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final List< Consumer< E > > listeners = new ArrayList<>();

		private final Map< E, JToggleButton > buttons;

		public ModeSelector( final E[] choices )
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

	private class VertexFeatureKeySelector extends FeatureKeySelector
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void regen( final boolean force )
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
			if ( !force && currentClass == clazz )
				return;
			currentClass = clazz;
			if ( null == clazz  )
				return;

			super.regen( false );
		}
	}

	private class EdgeFeatureKeySelector extends FeatureKeySelector
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void regen( final boolean force )
		{
			final Class< ? > clazz;
			switch ( mode.getEdgeColorMode() )
			{
			case EDGE:
				clazz = edgeClass;
				setVisible( true );
				break;
			case NONE:
			default:
				clazz = null;
				setVisible( false );
				break;
			case SOURCE_VERTEX:
			case TARGET_VERTEX:
				clazz = vertexClass;
				setVisible( true );
				break;
			}
			if ( !force && currentClass == clazz )
				return;
			currentClass = clazz;
			if ( null == clazz  )
				return;

			super.regen( false );
		}
	}

	private abstract class FeatureKeySelector extends JPanel
	{

		private static final long serialVersionUID = 1L;

		protected final JComboBox< String > cb1;

		private final JComboBox< String > cb2;

		protected Class< ? > currentClass;

		private final JLabel arrow;

		private final List< BiConsumer< String, String > > listeners = new ArrayList<>();

		public FeatureKeySelector()
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );
			cb1 = new JComboBox<>();
			cb1.setRenderer( new MyComboBoxRenderer() );
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
			echoModeValidity();
			final String fk = ( String ) cb1.getSelectedItem();
			final String pk = ( String ) cb2.getSelectedItem();
			listeners.forEach( ( bc ) -> bc.accept( fk, pk ) );
		}

		public List< BiConsumer< String, String > > listeners()
		{
			return listeners;
		}

		public void setFeatureKeys( final String c1, final String c2 )
		{
			regen( false );
			cb1.setSelectedItem( c1 );
			cb2.setSelectedItem( c2 );
		}

		protected void regen( final boolean force )
		{
			final HashSet< String > keysCB1 = new HashSet<>();
			if ( currentClass.equals( vertexClass ) )
			{
				keysCB1.addAll( vertexFeatureKeys.keySet() );
				keysCB1.addAll( collectKeys( featureModel.getFeatureSet( vertexClass )  ) );
			}
			else if ( currentClass.equals( edgeClass ) )
			{
				keysCB1.addAll( edgeFeatureKeys.keySet() );
				keysCB1.addAll( collectKeys( featureModel.getFeatureSet( edgeClass )  ) );
			}

			final List<String> lKeysCB1 = new ArrayList<>(keysCB1);
			lKeysCB1.sort( null );
			cb1.setModel( new DefaultComboBoxModel<>( lKeysCB1.toArray( new String[] {} ) ) );

			regenCB2();
			notifyListers();
		}

		private void regenCB2()
		{
			final String featureKey = ( String ) cb1.getSelectedItem();
			final ArrayList< String > keysCB2 = new ArrayList<>();

			// Feature is vertex, computer generated.
			if ( vertexClass.equals( currentClass ) && null != vertexFeatureKeys.get( featureKey ) )
				keysCB2.addAll( vertexFeatureKeys.get( featureKey ) );
			// Feature is edge, computer generated.
			else if ( edgeClass.equals( currentClass ) && null != edgeFeatureKeys.get( featureKey ) )
				keysCB2.addAll( edgeFeatureKeys.get( featureKey ) );
			// Feature is stored in the feature model, not computer generated.
			else
				keysCB2.addAll( featureModel.getFeature( featureKey ).getProjections().keySet() );
			// Else feature is invalid!

			keysCB2.sort( null );
			cb2.setModel( new DefaultComboBoxModel<>( keysCB2.toArray( new String[] {} ) ) );

			final boolean visible = keysCB2.size() > 1;
			cb2.setVisible( visible );
			arrow.setVisible( visible );
		}

		private class MyComboBoxRenderer  implements ListCellRenderer<String>
		{

			private final Color disabledBgColor;

			private final Color disabledFgColor;

			private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			public MyComboBoxRenderer()
			{
				setOpaque( true );
				this.disabledBgColor = ( Color ) UIManager.get( "ComboBox.disabledBackground" );
				this.disabledFgColor = ( Color ) UIManager.get( "ComboBox.disabledForeground" );
			}

			@Override
			public Component getListCellRendererComponent( final JList< ? extends String > list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus )
			{
				final JLabel c = ( JLabel ) defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				if ( isSelected )
				{
					c.setBackground( list.getSelectionBackground() );
					c.setForeground( list.getSelectionForeground() );
				}
				else
				{
					if ( !isFeatureKeyIn( featureModel, value, currentClass ) )
					{
						c.setBackground( disabledBgColor);
						c.setForeground( disabledFgColor );
					}
					else
					{
						c.setBackground( list.getBackground() );
						c.setForeground( list.getForeground() );
					}
				}
				c.setText( value );
				return c;
			}
		}
	}

	/**
	 * Returns <code>true</code> iff the specified feature key and feature
	 * projection key are registered into the feature model under the specified
	 * class.
	 *
	 * @param featureModel
	 *            the feature model.
	 * @param keys
	 *            the pair of feature and projection keys.
	 * @param clazz
	 *            the class of objects for the feature to check.
	 * @return <code>true</code> if the key pair is in the feature model.
	 */
	private static boolean isIn(final FeatureModel featureModel, final String[] keys, final Class<?> clazz)
	{
		if ( !isFeatureKeyIn( featureModel, keys[0], clazz ) )
			return false;

		final Map< String, ? > projs = featureModel.getFeature( keys[ 0 ] ).getProjections();
		return ( projs.keySet().contains( keys[ 1 ] ) );
	}

	private static boolean isFeatureKeyIn(final FeatureModel featureModel, final String featureKey, final Class<?> clazz)
	{
		return collectKeys( featureModel.getFeatureSet( clazz ) ).contains( featureKey );
	}

	/**
	 * Collects they keys of a feature set. If the feature set is
	 * <code>null</code> then an empty collection is returned.
	 *
	 * @param featureSet
	 *            the feature set.
	 * @return the keys of the feature set.
	 */
	private static Collection< String > collectKeys( final Set< Feature< ?, ? > > featureSet )
	{
		if ( featureSet == null )
			return Collections.emptyList();
		return featureSet.stream().map( Feature::getKey ).collect( Collectors.toList() );
	}
}
