package net.trackmate.trackscheme;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class KeyHandler
{
	private static final double ZOOM_SPEED = 1.1;

	private static final double ZOOM_SPEED_QUICK = 2.;

	private static final double MOVE_SPEED = 0.05;

	private static final double MOVE_SPEED_QUICK = 0.5;

	private final ShowTrackScheme trackscheme;

	public KeyHandler( final ShowTrackScheme trackscheme )
	{
		this.trackscheme = trackscheme;
		install();
	}

	private void install()
	{
		/*
		 * EDIT MODEL.
		 */

//		final KeyStroke ks = KeyStroke.getKeyStroke( 'd' );
//		registerAction( ks, ActionBank.getDeleteSelectionAction( trackscheme ) );

		/*
		 * NAVIGATE.
		 */

		final KeyStroke arrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 );
		registerAction( arrowDown, ActionBank.getNavigateToChildAction( trackscheme ) );

		final KeyStroke arrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 );
		registerAction( arrowUp, ActionBank.getNavigateToParentAction( trackscheme ) );

		final KeyStroke arrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 );
		registerAction( arrowRight, ActionBank.getNavigateToRightSiblingAction( trackscheme ) );

		final KeyStroke arrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 );
		registerAction( arrowLeft, ActionBank.getNavigateToLeftSiblingAction( trackscheme ) );

		final KeyStroke shiftArrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowDown, ActionBank.getAddChildToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowUp, ActionBank.getAddParentToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowRight, ActionBank.getAddRightSiblingToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowLeft, ActionBank.getAddLeftSiblingToSelectionAction( trackscheme ) );

		/*
		 * SELECT
		 */

		final KeyStroke keypad5 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD5, 0 );
		registerAction( keypad5, ActionBank.getSelectVertexAtCenterAction( trackscheme, true ) );

		final KeyStroke shiftKeypad5 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD5, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftKeypad5, ActionBank.getSelectVertexAtCenterAction( trackscheme, false ) );

		/*
		 * ZOOM.
		 */

		// All.

		final KeyStroke plusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, 0 );
		registerAction( plusKeypad, ActionBank.getZoomInAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke minusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, 0 );
		registerAction( minusKeypad, ActionBank.getZoomOutAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke metaPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.META_DOWN_MASK );
		registerAction( metaPlusKeypad, ActionBank.getZoomInAction( trackscheme, ZOOM_SPEED ) );

		final KeyStroke metaMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.META_DOWN_MASK );
		registerAction( metaMinusKeypad, ActionBank.getZoomOutAction( trackscheme, ZOOM_SPEED ) );

		// X.

		final KeyStroke shiftPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftPlusKeypad, ActionBank.getZoomInXAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke shiftMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftMinusKeypad, ActionBank.getZoomOutXAction( trackscheme, ZOOM_SPEED_QUICK ) );

		// Y.

		final KeyStroke ctrlPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK );
		registerAction( ctrlPlusKeypad, ActionBank.getZoomInYAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke ctrlMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK );
		registerAction( ctrlMinusKeypad, ActionBank.getZoomOutYAction( trackscheme, ZOOM_SPEED_QUICK ) );

		/*
		 * MOVE.
		 */

		//

		final KeyStroke keypad4 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD4, 0 );
		registerAction( keypad4, ActionBank.getMoveLeftAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad7 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD7, 0 );
		registerAction( keypad7, ActionBank.getMoveUpLeftAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad8 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD8, 0 );
		registerAction( keypad8, ActionBank.getMoveUpAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad9 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD9, 0 );
		registerAction( keypad9, ActionBank.getMoveUpRightAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad6 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD6, 0 );
		registerAction( keypad6, ActionBank.getMoveRightAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad3 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD3, 0 );
		registerAction( keypad3, ActionBank.getMoveDownRightAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad2 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD2, 0 );
		registerAction( keypad2, ActionBank.getMoveDownAction( trackscheme, MOVE_SPEED_QUICK ) );

		final KeyStroke keypad1 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD1, 0 );
		registerAction( keypad1, ActionBank.getMoveDownLeftAction( trackscheme, MOVE_SPEED_QUICK ) );

		// Slow

		final KeyStroke metaKeypad4 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD4, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad4, ActionBank.getMoveLeftAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad7 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD7, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad7, ActionBank.getMoveUpLeftAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad8 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD8, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad8, ActionBank.getMoveUpAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad9 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD9, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad9, ActionBank.getMoveUpRightAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad6 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD6, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad6, ActionBank.getMoveRightAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad3 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD3, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad3, ActionBank.getMoveDownRightAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad2 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD2, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad2, ActionBank.getMoveDownAction( trackscheme, MOVE_SPEED ) );

		final KeyStroke metaKeypad1 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD1, InputEvent.META_DOWN_MASK );
		registerAction( metaKeypad1, ActionBank.getMoveDownLeftAction( trackscheme, MOVE_SPEED ) );

		/*
		 * RESET VIEW
		 */

		final KeyStroke keypad0 = KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD0, 0 );
		registerAction( keypad0, ActionBank.getResetViewAction( trackscheme ) );

	}

	private void registerAction( final KeyStroke ks, final AbstractNamedAction action )
	{
		trackscheme.canvas.getInputMap().put( ks, action.name() );
		AbstractNamedAction.put( trackscheme.canvas.getActionMap(), action );
	}


}
