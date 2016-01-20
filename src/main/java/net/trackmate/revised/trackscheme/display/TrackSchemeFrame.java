package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BehaviourTransformEventHandler;
import bdv.viewer.InputActionBindings;
import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.ui.TransformEventHandler;
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

	private final InputActionBindings keybindings;

	private final TriggerBehaviourBindings triggerbindings;

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

		keybindings = new InputActionBindings();
		SwingUtilities.replaceUIActionMap( getRootPane(), keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		triggerbindings = new TriggerBehaviourBindings();
		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		trackschemePanel.getDisplay().addHandler( mouseAndKeyHandler );

		final TransformEventHandler< ? > tfHandler = trackschemePanel.getDisplay().getTransformEventHandler();
		if ( tfHandler instanceof BehaviourTransformEventHandler )
			( ( BehaviourTransformEventHandler< ? > ) tfHandler ).install( triggerbindings );

		final InputTriggerConfig inputConf = getKeyConfig( optional );
		trackschemePanel.getNavigator().installActionBindings( keybindings, inputConf );
		trackschemePanel.getSelectionBehaviours().installBehaviourBindings( triggerbindings, inputConf );

		final EditFocusVertexBehaviour editFocus = new EditFocusVertexBehaviour( focus, graph, trackschemePanel.getDisplay() );
		trackschemePanel.getDisplay().addTransformListener( editFocus );
		editFocus.installActionBindings( keybindings, inputConf );
	}

	protected InputTriggerConfig getKeyConfig( final TrackSchemeOptions optional )
	{
		final InputTriggerConfig conf = optional.values.getInputTriggerConfig();
		return conf != null ? conf : new InputTriggerConfig();
	}

	public TrackSchemePanel getTrackschemePanel()
	{
		return trackschemePanel;
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}
}
