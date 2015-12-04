package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.imglib2.ui.util.GuiUtil;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.ui.grouping.GroupHandle;
import net.trackmate.revised.ui.grouping.GroupLocksPanel;

public class TrackSchemeFrame extends JFrame
{
	private final TrackSchemePanel trackschemePanel;

	public TrackSchemeFrame(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final GroupHandle groupHandle )
	{
		this( graph, highlight, focus, selection, navigation, groupHandle, TrackSchemeOptions.options() );
	}

	public TrackSchemeFrame(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final GroupHandle groupHandle,
			final TrackSchemeOptions optional )
	{
		super( "TrackScheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		getRootPane().setDoubleBuffered( true );

		trackschemePanel = new TrackSchemePanel(
				graph,
				highlight,
				focus,
				selection,
				navigation,
				optional );
		add( trackschemePanel, BorderLayout.CENTER );

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		add( navigationLocksPanel, BorderLayout.NORTH );

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
