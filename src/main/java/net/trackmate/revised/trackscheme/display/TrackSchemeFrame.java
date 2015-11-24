package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.imglib2.ui.util.GuiUtil;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.spatial.HasTimepoint;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;

public class TrackSchemeFrame< V extends Vertex< E > & HasTimepoint, E extends Edge< V > > extends JFrame
{
	private final TrackSchemePanel< V > trackschemePanel;

	public TrackSchemeFrame(
			final TrackSchemeGraph< V, E > graph,
			final ReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final TrackSchemeHighlight highlight,
			final TrackSchemeSelection selection,
			final NavigationHandler< V > navigationHandler )
	{
		this( graph, modelGraph, idmap, highlight, selection, navigationHandler, TrackSchemeOptions.options() );
	}

	/**
	 *
	 * @param sources
	 *            the {@link SourceAndConverter sources} to display.
	 * @param numTimePoints
	 *            number of available timepoints.
	 * @param cache
	 *            handle to cache. This is used to control io timing.
	 * @param optional
	 *            optional parameters. See {@link ViewerOptions#options()}.
	 */
	public TrackSchemeFrame(
			final TrackSchemeGraph< V, E > graph,
			final ReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final TrackSchemeHighlight highlight,
			final TrackSchemeSelection selection,
			final NavigationHandler< V > navigationHandler,
			final TrackSchemeOptions optional )
	{
		super( "TrackScheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		getRootPane().setDoubleBuffered( true );

		trackschemePanel = new TrackSchemePanel< V >( graph, idmap.vertexIdBimap(), highlight, selection, optional );
		add( trackschemePanel, BorderLayout.CENTER );

		final NavigationLocksPanel navigationLocksPanel = new NavigationLocksPanel();
		navigationHandler.addNavigationListener( trackschemePanel, navigationLocksPanel );
		add( navigationLocksPanel, BorderLayout.NORTH );

		final HighlightNavigator< V, E > highlightNavigator = new HighlightNavigator< V, E >( graph, modelGraph, idmap.vertexIdBimap(), trackschemePanel.layout, highlight, navigationLocksPanel, navigationHandler );
		trackschemePanel.display.addTransformListener( highlightNavigator );

		final KeyHandler keyHandler = new KeyHandler( trackschemePanel.display, highlightNavigator, highlight, selection );
		trackschemePanel.display.addHandler( keyHandler );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				trackschemePanel.stop();
			}
		} );
	}

	public TrackSchemePanel getTrackschemePanel()
	{
		return trackschemePanel;
	}
}
