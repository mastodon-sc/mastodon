package net.trackmate.revised.trackscheme.display;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import bdv.util.AbstractNamedAction;

public class FocusHandler extends MouseAdapter
{
	private static final int MOUSE_MASK_CLICK = InputEvent.BUTTON1_MASK;

	private final TrackSchemeNavigator navigator;

	private final TrackSchemeSelection selection;

	private final TrackSchemeFocus focus;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeNavigation navigation;

	private final TrackSchemeGraph< ?, ? > graph;

	public FocusHandler(
			final TrackSchemeNavigator navigator,
			final TrackSchemeNavigation navigation,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeGraph< ?, ? > graph,
			final AbstractTrackSchemeOverlay graphOverlay )
	{
		this.navigator = navigator;
		this.navigation = navigation;
		this.focus = focus;
		this.selection = selection;
		this.graph = graph;
		this.graphOverlay = graphOverlay;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		if ( e.getModifiers() == MOUSE_MASK_CLICK )
		{
			final int vertexId = graphOverlay.getVertexIdAt( e.getX(), e.getY() );
			if ( vertexId >= 0 )
			{
				// Single click: We set the focus to the clicked vertex but do not navigate.
				focus.focusVertex( vertexId );

				if ( e.getClickCount() == 2 )
				{
					// Double click: We navigate to the clicked vertex.
					final TrackSchemeVertex ref = graph.vertexRef();
					graph.getVertexPool().getByInternalPoolIndex( vertexId, ref );
					navigation.notifyListeners( ref );
					graph.releaseRef( ref );
				}
			}
			else
			{
				// Click outside. We clear the focus.
				focus.focusVertex( -1 );
			}
		}
	}

	// TODO: redo when the configurable-keys branch is merged
	public void installOn( final JComponent component )
	{
		/*
		 * NAVIGATE.
		 */

		final KeyStroke arrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 );
		registerAction( arrowDown, TrackSchemeActionBank.getNavigateToChildAction( navigator ), component );

		final KeyStroke arrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 );
		registerAction( arrowUp, TrackSchemeActionBank.getNavigateToParentAction( navigator ), component );

		final KeyStroke arrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 );
		registerAction( arrowRight, TrackSchemeActionBank.getNavigateToRightSiblingAction( navigator ), component );

		final KeyStroke arrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 );
		registerAction( arrowLeft, TrackSchemeActionBank.getNavigateToLeftSiblingAction( navigator ), component );

		final KeyStroke shiftArrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowDown, TrackSchemeActionBank.getAddChildToSelectionAction( navigator, selection ), component );

		final KeyStroke shiftArrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowUp, TrackSchemeActionBank.getAddParentToSelectionAction( navigator, selection ), component );

		final KeyStroke shiftArrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowRight, TrackSchemeActionBank.getAddRightSiblingToSelectionAction( navigator, selection ), component );

		final KeyStroke shiftArrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowLeft, TrackSchemeActionBank.getAddLeftSiblingToSelectionAction( navigator, selection ), component );

		final KeyStroke spaceKey = KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0 );
		registerAction( spaceKey, TrackSchemeActionBank.getToggleSelectionOfHighlightAction( focus, selection ), component );
	}

	private void registerAction( final KeyStroke ks, final AbstractNamedAction action, final JComponent component )
	{
		component.getInputMap().put( ks, action.name() );
		AbstractNamedAction.put( component.getActionMap(), action );
	}


}
