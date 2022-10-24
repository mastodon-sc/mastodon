/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * Utility to register number listeners on components.
 * <p>
 * The listeners of this class are {@link KeyListener}s that listen to the user
 * typing a number, building a string representation as he types, then
 * converting the string to a double after a wait time. So that if the user
 * types
 *
 * <pre>
 * '4', '9', '.', '3,
 * </pre>
 *
 * then wait for 1 second without typing anything, the component will by
 * notified with the number 49.3. Pressing space or enter commits the number
 * immediately without waiting.
 * <p>
 * Different methods allow to register a listener that can listen to positive
 * only double or integer numbers. They return the actual {@link KeyListener}
 * instance that fuel this, so that consumers can de-register them after use.
 *
 * @author Jean-Yves Tinevez
 */
public class NumberListeners
{

	/**
	 * How much time we wait before committing a number.
	 */
	private static final long WAIT_DELAY = 1; // s

	public static void doubleListener( final ActionMap actionMap, final InputMap inputMap, final DoubleConsumer notify )
	{
		new NumberListener( actionMap, inputMap, notify, true, true );
	}

	public static void positiveDoubleListener( final ActionMap actionMap, final InputMap inputMap, final DoubleConsumer notify )
	{
		new NumberListener( actionMap, inputMap, notify, true, false );
	}

	public static void integerListener( final ActionMap actionMap, final InputMap inputMap, final DoubleConsumer notify )
	{
		new NumberListener( actionMap, inputMap, notify, false, true );
	}

	public static void positiveIntegerListener( final ActionMap actionMap, final InputMap inputMap, final DoubleConsumer notify )
	{
		new NumberListener( actionMap, inputMap, notify, false, false );
	}

	private static class NumberListener
	{

		private String strNumber = "";

		private ScheduledExecutorService ex;

		private ScheduledFuture< ? > future;

		private final ActionMap actionMap;

		private final InputMap inputMap;

		private boolean dotAdded = false;

		private final DoubleConsumer notify;

		private final boolean acceptDot;

		private final boolean acceptSign;

		public NumberListener(
				final ActionMap actionMap,
				final InputMap inputMap,
				final DoubleConsumer notify,
				final boolean acceptDot,
				final boolean acceptSign )
		{
			this.actionMap = actionMap;
			this.inputMap = inputMap;
			this.notify = notify;
			this.acceptDot = acceptDot;
			this.acceptSign = acceptSign;
			setup();
		}

		private final Runnable command = new Runnable()
		{
			@Override
			public void run()
			{
				// Convert to double.
				try
				{
					final double number = Double.parseDouble( strNumber );
					notify.accept( number );
				}
				catch ( final NumberFormatException nfe )
				{}
				// Reset
				ex = null;
				strNumber = "";
				dotAdded = false;
			}
		};

		private void setup()
		{
			// Digit keys.
			for ( int i = 0; i < 10; i++ )
			{
				final String actionName = "digit " + i;
				final int digit = i;
				final KeyStroke digitKey = KeyStroke.getKeyStroke( ( char ) ( '0' + i ) );
				final Action digitAction = new AbstractAction( actionName )
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						restartTimer();
						strNumber += "" + digit;
					}

					private static final long serialVersionUID = 1L;
				};
				inputMap.put( digitKey, actionName );
				actionMap.put( actionName, digitAction );
			}

			// Dot key.
			if ( acceptDot )
			{
				final String actionName = "dot";
				final KeyStroke dotKey = KeyStroke.getKeyStroke( '.' );
				final Action dotAction = new AbstractAction( "dot" )
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						if ( dotAdded )
							return;

						restartTimer();
						dotAdded = true;
						strNumber += ".";
					}

					private static final long serialVersionUID = 1L;
				};
				inputMap.put( dotKey, actionName );
				actionMap.put( actionName, dotAction );
			}

			// Sign keys.
			if ( acceptSign )
			{
				final char[] signChars = new char[] { '+', '-' };
				for ( final char signChar : signChars )
				{
					final String actionName = "sign " + signChar;
					final KeyStroke signKey = KeyStroke.getKeyStroke( signChar );
					final Action signAction = new AbstractAction( "sign " + signChar )
					{
						@Override
						public void actionPerformed( final ActionEvent e )
						{
							// Only accept sign as a first character.
							if ( strNumber.length() > 1 )
								return;

							restartTimer();
							strNumber += "" + signChar;
						}

						private static final long serialVersionUID = 1L;
					};

					inputMap.put( signKey, actionName );
					actionMap.put( actionName, signAction );
				}
			}

			// Commit keys.
			final int[] commitKeyCodes = new int[] {
					KeyEvent.VK_ENTER,
					KeyEvent.VK_SPACE,
					KeyEvent.VK_TAB
			};
			for ( final int commitKeyCode : commitKeyCodes )
			{
				final String actionName = "commit with " + KeyEvent.getKeyText( commitKeyCode );
				final KeyStroke commitKey = KeyStroke.getKeyStroke( commitKeyCode, 0 );
				final Action commitAction = new AbstractAction( actionName )
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						command.run();
					}

					private static final long serialVersionUID = 1L;
				};

				inputMap.put( commitKey, actionName );
				actionMap.put( actionName, commitAction );
			}
		}

		private void restartTimer()
		{
			if ( ex == null )
			{
				// Create new waiting line
				ex = Executors.newSingleThreadScheduledExecutor();
				future = ex.schedule( command, WAIT_DELAY, TimeUnit.SECONDS );
			}
			else
			{
				// Reset waiting line
				future.cancel( false );
				future = ex.schedule( command, WAIT_DELAY, TimeUnit.SECONDS );
			}
		}
	}
}
