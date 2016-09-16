package org.mastodon.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.revised.context.ContextChooser;
import org.mastodon.revised.trackscheme.TrackSchemeFocus;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeHighlight;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeSelection;
import org.mastodon.revised.trackscheme.display.TrackSchemeNavigator.NavigatorEtiquette;
import org.mastodon.revised.ui.context.ContextChooserPanel;
import org.mastodon.revised.ui.grouping.GroupHandle;
import org.mastodon.revised.ui.grouping.GroupLocksPanel;
import org.mastodon.revised.ui.util.InvokeOnEDT;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BehaviourTransformEventHandler;
import bdv.viewer.InputActionBindings;
import bdv.viewer.TriggerBehaviourBindings;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.util.GuiUtil;

public class TrackSchemeFrame extends JFrame
{
	private final TrackSchemePanel trackschemePanel;

	private final JPanel settingsPanel;

	private boolean isSettingsPanelVisible;

	private final InputActionBindings keybindings;

	private final TriggerBehaviourBindings triggerbindings;

	private final EditFocusVertexBehaviour editFocusVertex;

	private final UndoPointMarker undoPointMarker;

	final TrackSchemeSearchPanel searchField;

	public TrackSchemeFrame(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final UndoPointMarker undoPointMarker,
			final GroupHandle groupHandle,
			final ContextChooser< ? > contextChooser )
	{
		this( graph, highlight, focus, selection, navigation, undoPointMarker, groupHandle, contextChooser, TrackSchemeOptions.options() );
	}

	public TrackSchemeFrame(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final UndoPointMarker undoPointMarker,
			final GroupHandle groupHandle,
			final ContextChooser< ? > contextChooser,
			final TrackSchemeOptions optional )
	{
		super( "TrackScheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		this.undoPointMarker = undoPointMarker;
		getRootPane().setDoubleBuffered( true );

		trackschemePanel = new TrackSchemePanel(
				graph,
				highlight,
				focus,
				selection,
				navigation,
				optional );
		add( trackschemePanel, BorderLayout.CENTER );

		settingsPanel = new JPanel();
		settingsPanel.setLayout( new BoxLayout( settingsPanel, BoxLayout.LINE_AXIS ) );

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		settingsPanel.add( contextChooserPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		searchField = new TrackSchemeSearchPanel( graph, navigation, trackschemePanel.getDisplay() );
		settingsPanel.add( searchField );

		add( settingsPanel, BorderLayout.NORTH );
		isSettingsPanelVisible = true;

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
		trackschemePanel.getNavigator().installBehaviourBindings( triggerbindings, inputConf );
		trackschemePanel.getNavigator().installActionBindings( keybindings, inputConf, NavigatorEtiquette.FINDER_LIKE );

		editFocusVertex = new EditFocusVertexBehaviour( focus, graph, undoPointMarker, trackschemePanel.getDisplay() );
		trackschemePanel.getDisplay().addTransformListener( editFocusVertex );
		trackschemePanel.getOffsetDecorations().addOffsetHeadersListener( editFocusVertex );

		TrackSchemeActions.installActionBindings( keybindings, this, inputConf );
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

	public EditFocusVertexBehaviour getEditFocusVertex()
	{
		return editFocusVertex;
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}

	public boolean isSettingsPanelVisible()
	{
		return isSettingsPanelVisible;
	}

	public void setSettingsPanelVisible( final boolean visible )
	{
		try
		{
			InvokeOnEDT.invokeAndWait( () -> setSettingsPanelVisibleSynchronized( visible ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	private synchronized void setSettingsPanelVisibleSynchronized( final boolean visible )
	{
		if ( isSettingsPanelVisible != visible )
		{
			isSettingsPanelVisible = visible;
			if ( visible )
				add( settingsPanel, BorderLayout.NORTH );
			else
				remove( settingsPanel );
			invalidate();
			pack();
		}
	}
}
