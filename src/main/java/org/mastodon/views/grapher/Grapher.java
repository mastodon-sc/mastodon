package org.mastodon.views.grapher;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.mastodon.collection.RefList;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.ui.util.ExportableChartPanel;

public class Grapher
{

	public static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	public static final Font SMALL_FONT = FONT.deriveFont( 8 );

	private final Color bgColor = new JLabel().getBackground();
	// new Color( 220, 220, 220 );

	private final FeatureModel featureModel;

	private final String spaceUnits;

	private final String timeUnits;

	public Grapher( final FeatureModel featureModel, final String spaceUnits, final String timeUnits )
	{
		this.featureModel = featureModel;
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
	}

	public < O > JScrollPane graph( final GraphConfig graphConfig, final RefList< O > items )
	{
		// X label
		final SpecPair xFeature = graphConfig.getXFeature();
		final String units = xFeature.projectionSpec.projectionDimension.getUnits( spaceUnits, timeUnits );
		final String xAxisLabel = ( units.isEmpty() )
				? xFeature.toString()
				: xFeature + " (" + units + ")";

		final Map< Dimension, GraphConfig > splitByDimension = graphConfig.splitByDimension();

		final List< ExportableChartPanel > chartPanels = new ArrayList<>( splitByDimension.size() );
		for ( final Dimension dimension : splitByDimension.keySet() )
		{
			final GraphConfig gc = splitByDimension.get( dimension );

			// Get X projection.
			final FeatureProjection< O > xProjection = gc.getXFeature().getProjection( featureModel );

			// Get Y Projections.
			final List< SpecPair > yFeatures = gc.getYFeatures();
			final List< FeatureProjection< O > > yProjections = new ArrayList<>( yFeatures.size() );
			for ( final SpecPair sp : yFeatures )
				yProjections.add( sp.getProjection( featureModel ) );

			// Y label
			final String yAxisLabel = dimension.getUnits( spaceUnits, timeUnits );

			// Title
			final String title = buildPlotTitle( gc );

			// Create dataset.
			final ModelDataset< O > dataset = new ModelDataset< O >( items, xProjection, yProjections );

			final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setDefaultLinesVisible( false );

			// The chart
			final JFreeChart chart = ChartFactory.createXYLineChart(
					title,
					xAxisLabel,
					yAxisLabel,
					dataset,
					PlotOrientation.VERTICAL, true, true, false );
			chart.getTitle().setFont( FONT );
			chart.getLegend().setItemFont( SMALL_FONT );
			chart.setBackgroundPaint( bgColor );
			chart.setBorderVisible( false );
			chart.getLegend().setBackgroundPaint( bgColor );

			// The plot
			final XYPlot plot = chart.getXYPlot();
			plot.setRenderer( renderer );
			plot.getRangeAxis().setLabelFont( FONT );
			plot.getRangeAxis().setTickLabelFont( SMALL_FONT );
			plot.getDomainAxis().setLabelFont( FONT );
			plot.getDomainAxis().setTickLabelFont( SMALL_FONT );
			plot.setOutlineVisible( false );
			plot.setDomainCrosshairVisible( false );
			plot.setDomainGridlinesVisible( false );
			plot.setRangeCrosshairVisible( false );
			plot.setRangeGridlinesVisible( false );
			plot.setBackgroundAlpha( 0f );

			// Plot range.
			( ( NumberAxis ) plot.getRangeAxis() ).setAutoRangeIncludesZero( false );

			// Ticks. Fewer of them.
			plot.getRangeAxis().setTickLabelInsets( new RectangleInsets( 20, 10, 20, 10 ) );
			plot.getDomainAxis().setTickLabelInsets( new RectangleInsets( 10, 20, 10, 20 ) );

			// The panel
			final ExportableChartPanel chartPanel = new ExportableChartPanel( chart );
			chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
			chartPanels.add( chartPanel );
		}

		return renderCharts( chartPanels );
	}

	/**
	 * Renders and display a frame containing all the char panels, grouped by
	 * dimension.
	 * 
	 * @return a new JFrame, not shown yet.
	 */
	private final JScrollPane renderCharts( final List< ExportableChartPanel > chartPanels )
	{
		// The Panel
		final JPanel panel = new JPanel();
		final BoxLayout panelLayout = new BoxLayout( panel, BoxLayout.Y_AXIS );
		panel.setLayout( panelLayout );

		final Iterator< ExportableChartPanel > iterator = chartPanels.iterator();
		panel.add( iterator.next() );

		while ( iterator.hasNext() )
		{
			panel.add( Box.createVerticalStrut( 5 ) );
			panel.add( new JSeparator() );
			panel.add( Box.createVerticalStrut( 5 ) );
			panel.add( iterator.next() );
		}

		// Scroll pane
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setViewportView( panel );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );
		scrollPane.setBorder( null );
		return scrollPane;
	}

	/**
	 * Returns a suitable plot title built from the given target features
	 * 
	 * @return the plot title.
	 */
	private static final String buildPlotTitle( final GraphConfig gc )
	{
		final StringBuilder sb = new StringBuilder( "Plot of " );
		final Iterator< SpecPair > it = gc.getYFeatures().iterator();
		sb.append( it.next() );
		while ( it.hasNext() )
		{
			sb.append( ", " );
			sb.append( it.next() );
		}
		sb.append( " vs " );
		sb.append( gc.getXFeature() );
		sb.append( "." );
		return sb.toString();
	}
}
