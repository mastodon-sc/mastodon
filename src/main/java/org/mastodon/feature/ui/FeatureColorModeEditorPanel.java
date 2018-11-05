package org.mastodon.feature.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.revised.ui.coloring.feature.DefaultFeatureRangeCalculator;
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

	private final FeatureColorMode mode;

	private Set< FeatureSpec< ?, ? > > vertexFeatureSpecs = new HashSet<>();

	private Set< FeatureSpec< ?, ? > > edgeFeatureSpecs = new HashSet<>();

	private final FeatureSelectionPanel edgeFeatureSelectionPanel;

	private final FeatureSelectionPanel vertexFeatureSelectionPanel;

	/**
	 * Flag used to avoid altering the mode when updating the features displayed
	 * in the JComboBoxes.
	 */
	private boolean doForwardToMode = true;

	public FeatureColorModeEditorPanel(
			final FeatureColorMode mode,
			final FeatureRangeCalculator vertexFeatureRangeCalculator,
			final FeatureRangeCalculator edgeFeatureRangeCalculator
			)
	{
		this.mode = mode;

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

		this.vertexFeatureSelectionPanel = new FeatureSelectionPanel();
		addToLayout( new JLabel( "vertex feature", JLabel.TRAILING ), vertexFeatureSelectionPanel, c );


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
				final FeatureRangeCalculator rangeCalculator = ( mode.getVertexColorMode().equals( VertexColorMode.VERTEX ) )
						? vertexFeatureRangeCalculator
						: edgeFeatureRangeCalculator;
				final String featureKey = mode.getVertexFeatureProjection()[ 0 ];
				final String projectionKey = mode.getVertexFeatureProjection()[ 1 ];
				final FeatureSpec< ?, ? > featureSpec = getFeatureSpecFromKey( featureKey );
				if ( null == featureSpec )
					return;
				final double[] minMax = rangeCalculator.computeMinMax( featureSpec, projectionKey );
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

		this.edgeFeatureSelectionPanel = new FeatureSelectionPanel();
		addToLayout( new JLabel( "edge feature", JLabel.TRAILING ), edgeFeatureSelectionPanel, c );

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
				final FeatureRangeCalculator rangeCalculator = ( mode.getEdgeColorMode().equals( EdgeColorMode.EDGE ) )
						? edgeFeatureRangeCalculator
						: vertexFeatureRangeCalculator;
				final String featureKey = mode.getEdgeFeatureProjection()[ 0 ];
				final String projectionKey = mode.getEdgeFeatureProjection()[ 1 ];
				final FeatureSpec< ?, ? > featureSpec = getFeatureSpecFromKey( featureKey );
				if ( null == featureSpec )
					return;
				final double[] minMax = rangeCalculator.computeMinMax( featureSpec, projectionKey );
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

		/*
		 * Listeners.
		 */

		// Listen to changes in the vertex feature panel and forward it to the mode.
		vertexFeatureSelectionPanel.updateListeners().add( () -> {
			if ( doForwardToMode )
			{
				final String[] keys = vertexFeatureSelectionPanel.getSelection();
				mode.setVertexFeatureProjection( keys[ 0 ], keys[ 1 ] );
			}
		} );

		// Listen to changes in the vertex color mode and update feature panel.
		final Consumer< VertexColorMode > vertexColorModeListener = new Consumer< VertexColorMode >()
		{
			@Override
			public void accept( final VertexColorMode m )
			{
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
			}
		};
		vertexColorModeSelector.listeners().add( vertexColorModeListener );

		// Listen to changes in the vertex feature panel and forward it to the mode.

		// Listen to changes in the edge color mode and update feature panel.
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
		
		// Listen to changes in the vertex feature panel and forward it to the mode.
		edgeFeatureSelectionPanel.updateListeners().add( () -> {
			if ( doForwardToMode )
			{
				final String[] keys = edgeFeatureSelectionPanel.getSelection();
				mode.setEdgeFeatureProjection( keys[ 0 ], keys[ 1 ] );
			}
		} );
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

	/**
	 * Tries to retrieve the {@link FeatureSpec} corresponding to the specified
	 * key, by searching the {@link #vertexFeatureSpecs} and
	 * {@link #edgeFeatureSpecs} fields. If not found, return <code>null</code>.
	 * 
	 * @param key
	 *            the feature key.
	 * @return the corresponding feature spec, or <code>null</code> if it cannot
	 *         be found.
	 */
	private FeatureSpec< ?, ? > getFeatureSpecFromKey( final String key )
	{
		for ( final FeatureSpec< ?, ? > featureSpec : vertexFeatureSpecs )
		{
			if ( featureSpec.getKey().equals( key ) )
				return featureSpec;
		}
		for ( final FeatureSpec< ?, ? > featureSpec : edgeFeatureSpecs )
		{
			if ( featureSpec.getKey().equals( key ) )
				return featureSpec;
		}
		return null;
	}

	public void setFeatureSpecs( final Set< FeatureSpec< ?, ? > > vertexFeatureSpecs, final Set< FeatureSpec< ?, ? > > edgeFeatureSpecs )
	{
		// Sort.
		final List< FeatureSpec< ?, ? > > vfs = new ArrayList<>( vertexFeatureSpecs );
		vfs.sort( Comparator.comparing( FeatureSpec::getKey ) );
		this.vertexFeatureSpecs  = new LinkedHashSet<>( vfs );

		final List< FeatureSpec< ?, ? > > efs = new ArrayList<>( edgeFeatureSpecs );
		efs.sort( Comparator.comparing( FeatureSpec::getKey ) );
		this.edgeFeatureSpecs = new LinkedHashSet<>( efs );

		// Pass to lists.
		doForwardToMode = false;
		vertexFeatureSelectionPanel.setFeatureSpecs( mode.getVertexColorMode() == VertexColorMode.VERTEX ? vertexFeatureSpecs : edgeFeatureSpecs );
		vertexFeatureSelectionPanel.setSelection( mode.getVertexFeatureProjection() );
		edgeFeatureSelectionPanel.setFeatureSpecs( mode.getEdgeColorMode() == EdgeColorMode.EDGE ? edgeFeatureSpecs : vertexFeatureSpecs );
		edgeFeatureSelectionPanel.setSelection( mode.getEdgeFeatureProjection() );
		doForwardToMode = true;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );

		final FeatureColorMode mode = FeatureColorMode.defaultMode();
		mode.updateListeners().add( () -> System.out.println( "Mode is now: " + mode ) );
		
		final Model model = new Model();

		final DefaultFeatureRangeCalculator< Spot > vertexFeatureRangeCalculator =
				new DefaultFeatureRangeCalculator<>( model.getGraph().vertices(), model.getFeatureModel() );
		final DefaultFeatureRangeCalculator< Link > edgeFeatureRangeCalculator =
				new DefaultFeatureRangeCalculator<>( model.getGraph().edges(), model.getFeatureModel() );

		final FeatureColorModeEditorPanel editorPanel = new FeatureColorModeEditorPanel(
				mode,
				vertexFeatureRangeCalculator,
				edgeFeatureRangeCalculator );

		// Collect feature specs.
		final Context context = new Context( FeatureSpecsService.class );
		final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );

		// Vertex feature specs.
		final Set<FeatureSpec< ?, ? >> vfs = new HashSet<>(featureSpecsService.getSpecs( Spot.class ));
		final Set< FeatureSpec< ?, ? > > fmVfs = model.getFeatureModel().getFeatureSpecs().stream()
			.filter( (fs) -> fs.getTargetClass().isAssignableFrom(Spot.class) )
			.collect( Collectors.toSet() );
		vfs.addAll( fmVfs );

		// Edge feature specs.
		final Set<FeatureSpec< ?, ? >> efs = new HashSet<>(featureSpecsService.getSpecs( Link.class ));
		final Set< FeatureSpec< ?, ? > > fmEfs = model.getFeatureModel().getFeatureSpecs().stream()
			.filter( (fs) -> fs.getTargetClass().isAssignableFrom(Link.class) )
			.collect( Collectors.toSet() );
		efs.addAll( fmEfs );

		editorPanel.setFeatureSpecs( vfs, efs );

		final JFrame frame = new JFrame( "Feature color mode editor" );
		frame.getContentPane().add( editorPanel );
		frame.pack();
		frame.setVisible( true );
	}
}
