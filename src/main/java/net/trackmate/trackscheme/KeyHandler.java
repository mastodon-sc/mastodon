package net.trackmate.trackscheme;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class KeyHandler
{
	private static final double ZOOM_SPEED = 1.1;

	private static final double ZOOM_SPEED_QUICK = 2.;

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

		final KeyStroke ks = KeyStroke.getKeyStroke( 'd' );
		registerAction( ks, ActionBank.getDeleteSelectionAction( trackscheme ) );

		/*
		 * NAVIGATE.
		 */

		final KeyStroke arrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 );
		registerAction( arrowDown, ActionBank.getNavigateToChildAction( trackscheme ) );

		final KeyStroke arrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 );
		registerAction( arrowUp, ActionBank.getNavigateToParentAction( trackscheme ) );

		final KeyStroke arrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 );
		registerAction( arrowRight, ActionBank.getNavigateToRightSibblingAction( trackscheme ) );

		final KeyStroke arrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 );
		registerAction( arrowLeft, ActionBank.getNavigateToLeftSibblingAction( trackscheme ) );

		final KeyStroke shiftArrowDown = KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowDown, ActionBank.getAddChildToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowUp = KeyStroke.getKeyStroke( KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowUp, ActionBank.getAddParentToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowRight = KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowRight, ActionBank.getAddRightSibblingToSelectionAction( trackscheme ) );

		final KeyStroke shiftArrowLeft = KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftArrowLeft, ActionBank.getAddLeftSibblingToSelectionAction( trackscheme ) );

		/*
		 * ZOOM.
		 */

		// All.

		final KeyStroke plusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, 0 );
		registerAction( plusKeypad, ActionBank.getZoomInQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke minusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, 0 );
		registerAction( minusKeypad, ActionBank.getZoomOutQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke metaPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.META_DOWN_MASK );
		registerAction( metaPlusKeypad, ActionBank.getZoomInAction( trackscheme, ZOOM_SPEED ) );

		final KeyStroke metaMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.META_DOWN_MASK );
		registerAction( metaMinusKeypad, ActionBank.getZoomOutAction( trackscheme, ZOOM_SPEED ) );

		// X.

		final KeyStroke shiftPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftPlusKeypad, ActionBank.getZoomInXQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke shiftMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.SHIFT_DOWN_MASK );
		registerAction( shiftMinusKeypad, ActionBank.getZoomOutXQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

		// Y.

		final KeyStroke altPlusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK );
		registerAction( altPlusKeypad, ActionBank.getZoomInYQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

		final KeyStroke altMinusKeypad = KeyStroke.getKeyStroke( KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK );
		registerAction( altMinusKeypad, ActionBank.getZoomOutYQuickAction( trackscheme, ZOOM_SPEED_QUICK ) );

	}

	private void registerAction( final KeyStroke ks, final AbstractNamedAction action )
	{
		trackscheme.canvas.getInputMap().put( ks, action.name() );
		AbstractNamedAction.put( trackscheme.canvas.getActionMap(), action );

//		trackscheme.canvas.addKeyListener( new KeyAdapter()
//		{
//			@Override
//			public void keyPressed( final KeyEvent e )
//			{
//				System.out.println( "Pressed " + e );// DEBUG
//			};
//
//			@Override
//			public void keyTyped( final KeyEvent e )
//			{
//				System.out.println( "Typed " + e );// DEBUG
//			};
//		} );
	}


}
