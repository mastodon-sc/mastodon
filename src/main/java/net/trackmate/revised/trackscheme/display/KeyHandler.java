package net.trackmate.revised.trackscheme.display;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import bdv.util.AbstractNamedAction;

public class KeyHandler
{
	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final HighlightNavigator selectionNavigator;

	private final TrackSchemeHighlight highlight;

	private final TrackSchemeSelection selection;

	public KeyHandler(
			final InteractiveDisplayCanvasComponent< ScreenTransform > display,
			final HighlightNavigator selectionNavigator,
			final TrackSchemeHighlight highlight,
			final TrackSchemeSelection selection )
	{
		this.display = display;
		this.selectionNavigator = selectionNavigator;
		this.highlight = highlight;
		this.selection = selection;
		install();
	}

	private void install()
	{
		/*
		 * NAVIGATE.
		 */

		final KeyStroke arrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 );
		registerAction( arrowDown, TrackSchemeActionBank.getNavigateToChildAction( selectionNavigator ) );

		final KeyStroke arrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 );
		registerAction( arrowUp, TrackSchemeActionBank.getNavigateToParentAction( selectionNavigator ) );

		final KeyStroke arrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 );
		registerAction( arrowRight, TrackSchemeActionBank.getNavigateToRightSiblingAction( selectionNavigator ) );

		final KeyStroke arrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 );
		registerAction( arrowLeft, TrackSchemeActionBank.getNavigateToLeftSiblingAction( selectionNavigator ) );

		final KeyStroke shiftArrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowDown, TrackSchemeActionBank.getAddChildToSelectionAction( selectionNavigator, selection ) );

		final KeyStroke shiftArrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowUp, TrackSchemeActionBank.getAddParentToSelectionAction( selectionNavigator, selection ) );

		final KeyStroke shiftArrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowRight, TrackSchemeActionBank.getAddRightSiblingToSelectionAction( selectionNavigator, selection ) );

		final KeyStroke shiftArrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowLeft, TrackSchemeActionBank.getAddLeftSiblingToSelectionAction( selectionNavigator, selection ) );

		final KeyStroke spaceKey = KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0 );
		registerAction( spaceKey, TrackSchemeActionBank.getToggleSelectionOfHighlightAction( highlight, selection ) );
	}

	private void registerAction( final KeyStroke ks, final AbstractNamedAction action )
	{
		display.getInputMap().put( ks, action.name() );
		AbstractNamedAction.put( display.getActionMap(), action );
	}


}
