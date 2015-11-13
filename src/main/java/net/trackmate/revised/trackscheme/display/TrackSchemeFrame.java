package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.imglib2.ui.util.GuiUtil;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;

public class TrackSchemeFrame extends JFrame
{
	private final TrackSchemePanel trackschemePanel;

	public TrackSchemeFrame(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight< ?, ? > highlight )
	{
		this( graph, highlight, TrackSchemeOptions.options() );
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
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight< ?, ? > highlight,
			final TrackSchemeOptions optional )
	{
		super( "TrackScheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		getRootPane().setDoubleBuffered( true );

		trackschemePanel = new TrackSchemePanel( graph, highlight, optional );
		add( trackschemePanel, BorderLayout.CENTER );
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
