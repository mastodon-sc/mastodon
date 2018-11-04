package org.mastodon.feature.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.scijava.Context;

/**
 * JPanel to edit a single {@link FeatureColorMode}.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureColorModeEditorPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final Class< ? extends Vertex< ? > > vertexClass;

	private final Class< ? extends Edge< ? > > edgeClass;

	private final FeatureColorMode mode;

	private Collection< FeatureSpec< ?, ? > > featureSpecs = new ArrayList<>();

	private final Set< FeatureSpec< ?, ? > > vertexFeatureSpecs;

	private final Set< FeatureSpec< ?, ? > > edgeFeatureSpecs;

	public FeatureColorModeEditorPanel(
			final FeatureColorMode mode,
			final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator,
			final FeatureSpecsService featureSpecsService,
			final Class< ? extends Vertex< ? > > vertexClass,
			final Class< ? extends Edge< ? > > edgeClass )
	{
		this.mode = mode;
		this.vertexClass = vertexClass;
		this.edgeClass = edgeClass;

		final List< FeatureSpec< ?, ? > > vfs = new ArrayList<>();
		vfs.addAll( featureSpecsService.getSpecs( vertexClass ) );
		vfs.sort( Comparator.comparing( FeatureSpec::getKey ) );
		this.vertexFeatureSpecs = new LinkedHashSet<>( vfs );

		final List< FeatureSpec< ?, ? > > efs = new ArrayList<>();
		efs.addAll( featureSpecsService.getSpecs( edgeClass ) );
		efs.sort( Comparator.comparing( FeatureSpec::getKey ) );
		this.edgeFeatureSpecs = new LinkedHashSet<>( efs );

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
		 * Vertex color mode.
		 */
		final ModeSelector< VertexColorMode > vertexColorModeSelector = new ModeSelector<>( VertexColorMode.values() );
		addToLayout( new JLabel( "vertex color mode", JLabel.TRAILING ), vertexColorModeSelector, c );

		/*
		 * Vertex feature.
		 */

		final FeatureSelectionPanel vertexFeatureSelectionPanel = new FeatureSelectionPanel();
		vertexFeatureSelectionPanel.updateListeners().add( () -> {
			final String[] keys = vertexFeatureSelectionPanel.getSelection();
			mode.setVertexFeatureProjection( keys[ 0 ], keys[ 1 ] );
		} );
		addToLayout( new JLabel( "vertex feature", JLabel.TRAILING ), vertexFeatureSelectionPanel, c );

		vertexColorModeSelector.listeners().add( m -> {
			if ( m == mode.getVertexColorMode() )
				return;

			mode.setVertexColorMode( m );
			switch ( m )
			{
			case VERTEX:
				vertexFeatureSelectionPanel.setFeatureSpecs( vertexFeatureSpecs );
				break;
			case INCOMING_EDGE:
			case OUTGOING_EDGE:
				vertexFeatureSelectionPanel.setFeatureSpecs( edgeFeatureSpecs );
				break;
			case NONE:
			default:
				break;
			}
		} );
		
		
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
				final FeatureSpec< ?, ? > featureSpec = getFeatureSpecFromKey( featureKey );
				final double[] minMax = rangeCalculator.computeMinMax( clazz, featureSpec, projectionKey );
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

		final FeatureSelectionPanel edgeFeatureSelectionPanel = new FeatureSelectionPanel();
		edgeFeatureSelectionPanel.updateListeners().add( () -> {
			final String[] keys = edgeFeatureSelectionPanel.getSelection();
			mode.setEdgeFeatureProjection( keys[ 0 ], keys[ 1 ] );
		} );
		addToLayout( new JLabel( "edge feature", JLabel.TRAILING ), edgeFeatureSelectionPanel, c );

		final Consumer< EdgeColorMode > edgeColorModeListener = new Consumer< EdgeColorMode >()
		{
			@Override
			public void accept( final EdgeColorMode m )
			{
				if ( m == mode.getEdgeColorMode() )
					return;

				mode.setEdgeColorMode( m );
				switch ( m )
				{
				case NONE:
				case EDGE:
					edgeFeatureSelectionPanel.setFeatureSpecs( edgeFeatureSpecs );
					break;
				case SOURCE_VERTEX:
				case TARGET_VERTEX:
					edgeFeatureSelectionPanel.setFeatureSpecs( vertexFeatureSpecs );
					break;
				default:
					break;
				}
			}
		};

		edgeColorModeSelector.listeners().add( edgeColorModeListener );
		edgeColorModeListener.accept( mode.getEdgeColorMode() );
		edgeFeatureSelectionPanel.setSelection( mode.getEdgeFeatureProjection() );

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
				final FeatureSpec< ?, ? > featureSpec = getFeatureSpecFromKey( featureKey );
				final double[] minMax = rangeCalculator.computeMinMax( clazz, featureSpec, projectionKey );
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
			vertexColorMapSelector.setVisible( visible );
			vertexFeatureRangeSelector.setVisible( visible );
		};
		vertexColorModeSelector.listeners().add( vv );

		/*
		 * Listen to changes in edge color mode and hide panels or not.
		 */

		final Consumer< EdgeColorMode > ve = vcm -> {
			final boolean visible = !vcm.equals( EdgeColorMode.NONE );
			edgeColorMapSelector.setVisible( visible );
			edgeFeatureRangeSelector.setVisible( visible );
		};
		edgeColorModeSelector.listeners().add( ve );

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

	private FeatureSpec< ?, ? > getFeatureSpecFromKey( final String key )
	{
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
		{
			if ( featureSpec.getKey().equals( key ) )
				return featureSpec;
		}
		throw new IllegalArgumentException( "Unknown key for feature specification: " + key );
	}

	public void setFeatureSpecs( final Collection< FeatureSpec< ?, ? > > featureSpecs )
	{
		this.featureSpecs = featureSpecs;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );

		final FeatureColorMode mode = FeatureColorMode.defaultMode();
		final FeatureModel featureModel = new FeatureModel();
		final Model model = new Model();
		final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator = new FeatureRangeCalculator<>(
				model.getGraph(),
				featureModel );

		final Context context = new Context( FeatureSpecsService.class );
		final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
		final FeatureColorModeEditorPanel editorPanel = new FeatureColorModeEditorPanel(
				mode,
				rangeCalculator,
				featureSpecsService,
				Spot.class,
				Link.class );
		final JFrame frame = new JFrame( "Feature color mode editor" );
		frame.getContentPane().add( editorPanel );
		frame.pack();
		frame.setVisible( true );
	}
}
