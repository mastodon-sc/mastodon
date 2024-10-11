/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.commandfinder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.gui.RoundBorder;

public class MultiInputTriggerPanelEditor extends JPanel
{

	@FunctionalInterface
	public static interface InputTriggerChangeListener
	{
		public void inputTriggerChanged();
	}

	private static final long serialVersionUID = 1L;

	private final List< KeyItem > keyItems;

	private final JTextField textField;

	private List< InputTrigger > triggers = Collections.singletonList( InputTrigger.NOT_MAPPED );

	public MultiInputTriggerPanelEditor()
	{
		this.keyItems = new ArrayList<>();

		setPreferredSize( new Dimension( 400, 26 ) );
		setMinimumSize( new Dimension( 26, 26 ) );
		setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );

		this.textField = new JTextField();
		textField.setColumns( 10 );
		textField.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		textField.setOpaque( false );
		textField.setEditable( false );

		add( textField );
		add( Box.createHorizontalGlue() );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		setBorder( UIManager.getBorder( "TextField.border" ) );
		setBackground( UIManager.getColor( "TextField.background" ) );
	}

	public void setInputTriggers( final List< InputTrigger > triggers )
	{
		this.triggers = triggers;
		regenKeyPanels();
	}

	public List< InputTrigger > getInputTrigger()
	{
		return triggers;
	}

	private void regenKeyPanels()
	{
		// Clear
		for ( final KeyItem keyItem : keyItems )
			remove( keyItem );
		keyItems.clear();

		final List< String[] > tokenList = new ArrayList<>( triggers.size() );
		for ( final InputTrigger trigger : triggers )
		{
			if ( trigger.equals( InputTrigger.NOT_MAPPED ) )
				tokenList.add( null );
			else
				tokenList.add( trigger.toString().split( " " ) );
		}

		int n = 0;
		for ( int i = 0; i < tokenList.size(); i++ )
		{
			final String[] tokens = tokenList.get( i );
			if ( tokens == null )
				continue;
			if ( n > 0 )
			{
				// Separator.
				final KeyItem tagp = new SeparatorKeyItem();
				keyItems.add( tagp );
				add( tagp, getComponentCount() - 2 );
			}

			sortTokens( tokens );
			for ( final String key : tokens )
			{
				final KeyItem tagp = new KeyItem( key, true );
				keyItems.add( tagp );
				add( tagp, getComponentCount() - 2 );
			}
			n++;
		}
		revalidate();
		repaint();
	}

	@Override
	public boolean requestFocusInWindow()
	{
		return textField.requestFocusInWindow();
	}

	/*
	 * INNER CLASSES
	 */

	private class SeparatorKeyItem extends KeyItem
	{

		private static final long serialVersionUID = 1L;

		public SeparatorKeyItem()
		{
			super( "or", true );
		}

		@Override
		protected void updateTxtLook()
		{
			if ( txt != null )
			{
				txt.setOpaque( false );
				Font font = UIManager.getFont( "Label.font" );
				font = font.deriveFont( font.getSize2D() - 4f );
				txt.setFont( font );
			}
		}
	}

	private class KeyItem extends JPanel
	{
		private static final long serialVersionUID = 1L;

		private final boolean valid;

		protected final JLabel txt;

		public KeyItem( final String tag, final boolean valid )
		{
			this.valid = valid;
			final String str = TRIGGER_SYMBOLS.containsKey( tag ) ? ( " " + TRIGGER_SYMBOLS.get( tag ) + " " ) : ( " " + tag + " " );
			txt = new JLabel( str );
			txt.setOpaque( true );
			updateTxtLook();

			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
			add( Box.createHorizontalStrut( 1 ) );
			add( txt );
			add( Box.createHorizontalStrut( 1 ) );
			setOpaque( false );
		}

		@Override
		public void updateUI()
		{
			super.updateUI();
			updateTxtLook();
		}

		protected void updateTxtLook()
		{
			if ( txt != null )
			{
				final Color tfg = UIManager.getColor( "TextField.foreground" );
				final Color tbg = UIManager.getColor( "TextField.background" );
				final Color bg = valid ? mix( tbg, tfg, 0.95 ) : mix( tbg, Color.red, 0.5 );
				final Color borderColor = mix( bg, tfg, 0.8 );
				txt.setBackground( bg );
				txt.setBorder( new RoundBorder( borderColor, MultiInputTriggerPanelEditor.this, 1 ) );

				Font font = UIManager.getFont( "Label.font" );
				font = font.deriveFont( font.getSize2D() - 2f );
				txt.setFont( font );
			}
		}
	}

	/** Contains the tags in the order we want them to appear in the panel. */
	private static final List< String > INPUT_TRIGGER_SYNTAX_TAGS = new ArrayList<>();

	/**
	 * Contains the tags sorted so that they can be searched by the autocomplete
	 * process.
	 */
	private static final List< String > INPUT_TRIGGER_SYNTAX_TAGS_SORTED = new ArrayList<>();

	/** Small-caps version of INPUT_TRIGGER_SYNTAX_TAGS_SORTED. */
	private static final List< String > INPUT_TRIGGER_SYNTAX_TAGS_SMALL_CAPS;

	/** Visual replacement for some tags. */
	private static final Map< String, String > TRIGGER_SYMBOLS = new HashMap<>();

	private static final Map< String, String > INPUT_TRIGGER_SYNTAX_TAG_REMAP = new HashMap<>();

	static
	{
		INPUT_TRIGGER_SYNTAX_TAGS.addAll(
				Arrays.asList(
						"all",
						"ctrl",
						"alt",
						"altGraph",
						"shift",
						"meta",
						"command",
						"cmd",
						"win",
						"ENTER",
						"BACK_SPACE",
						"TAB",
						"CANCEL",
						"CLEAR",
						"COMPOSE",
						"PAUSE",
						"CAPS_LOCK",
						"ESCAPE",
						"SPACE",
						"PAGE_UP",
						"PAGE_DOWN",
						"END",
						"HOME",
						"BEGIN",
						"COMMA",
						"PERIOD",
						"SLASH",
						"SEMICOLON",
						"EQUALS",
						"OPEN_BRACKET",
						"BACK_SLASH",
						"CLOSE_BRACKET",
						"LEFT",
						"UP",
						"RIGHT",
						"DOWN",
						"NUMPAD0",
						"NUMPAD1",
						"NUMPAD2",
						"NUMPAD3",
						"NUMPAD4",
						"NUMPAD5",
						"NUMPAD6",
						"NUMPAD7",
						"NUMPAD8",
						"NUMPAD9",
						"MULTIPLY",
						"ADD",
						"SEPARATOR",
						"SUBTRACT",
						"DECIMAL",
						"DIVIDE",
						"DELETE",
						"NUM_LOCK",
						"SCROLL_LOCK",
						"double-click",
						"button1",
						"button2",
						"button3",
						"scroll",
						"|" ) );
		for ( int i = 0; i < 26; i++ )
			INPUT_TRIGGER_SYNTAX_TAGS.add( String.valueOf( ( char ) ( 'A' + i ) ) );
		for ( int i = 0; i < 10; i++ )
			INPUT_TRIGGER_SYNTAX_TAGS.add( "" + i );
		for ( int i = 1; i <= 24; i++ )
			INPUT_TRIGGER_SYNTAX_TAGS.add( "F" + i );

		INPUT_TRIGGER_SYNTAX_TAGS_SORTED.addAll( INPUT_TRIGGER_SYNTAX_TAGS );
		INPUT_TRIGGER_SYNTAX_TAGS_SORTED.sort( String.CASE_INSENSITIVE_ORDER );
		INPUT_TRIGGER_SYNTAX_TAGS_SMALL_CAPS = new ArrayList<>( INPUT_TRIGGER_SYNTAX_TAGS_SORTED.size() );
		for ( final String tag : INPUT_TRIGGER_SYNTAX_TAGS_SORTED )
			INPUT_TRIGGER_SYNTAX_TAGS_SMALL_CAPS.add( tag.toLowerCase() );

		INPUT_TRIGGER_SYNTAX_TAG_REMAP.put( "cmd", "meta" );
		INPUT_TRIGGER_SYNTAX_TAG_REMAP.put( "command", "meta" );
		INPUT_TRIGGER_SYNTAX_TAG_REMAP.put( "windows", "win" );

		TRIGGER_SYMBOLS.put( "ENTER", "\u23CE" );
		TRIGGER_SYMBOLS.put( "BACK_SPACE", "\u232B" );
		TRIGGER_SYMBOLS.put( "DELETE", "\u2326" );
		TRIGGER_SYMBOLS.put( "TAB", "\u21E5" );
		TRIGGER_SYMBOLS.put( "PAUSE", "||" );
		TRIGGER_SYMBOLS.put( "CAPS_LOCK", "\u21EA" );
		TRIGGER_SYMBOLS.put( "PAGE_UP", "\u21DE" );
		TRIGGER_SYMBOLS.put( "PAGE_DOWN", "\u21DF" );
		TRIGGER_SYMBOLS.put( "END", "\u2198" );
		TRIGGER_SYMBOLS.put( "HOME", "\u2196" );
		TRIGGER_SYMBOLS.put( "ESCAPE", "\u238b" );
		TRIGGER_SYMBOLS.put( "LEFT", "\u2190" );
		TRIGGER_SYMBOLS.put( "UP", "\u2191" );
		TRIGGER_SYMBOLS.put( "RIGHT", "\u2192" );
		TRIGGER_SYMBOLS.put( "DOWN", "\u2193" );
		TRIGGER_SYMBOLS.put( "NUMPAD0", "\u24ea" );
		TRIGGER_SYMBOLS.put( "NUMPAD1", "\u2460" );
		TRIGGER_SYMBOLS.put( "NUMPAD2", "\u2461" );
		TRIGGER_SYMBOLS.put( "NUMPAD3", "\u2462" );
		TRIGGER_SYMBOLS.put( "NUMPAD4", "\u2463" );
		TRIGGER_SYMBOLS.put( "NUMPAD5", "\u2464" );
		TRIGGER_SYMBOLS.put( "NUMPAD6", "\u2465" );
		TRIGGER_SYMBOLS.put( "NUMPAD7", "\u2466" );
		TRIGGER_SYMBOLS.put( "NUMPAD8", "\u2467" );
		TRIGGER_SYMBOLS.put( "NUMPAD9", "\u2468" );
		TRIGGER_SYMBOLS.put( "MULTIPLY", "\u00d7" );
		TRIGGER_SYMBOLS.put( "DIVIDE", "\u00f7" );
		TRIGGER_SYMBOLS.put( "ADD", "+" );
		TRIGGER_SYMBOLS.put( "SUBTRACT", "-" );
		TRIGGER_SYMBOLS.put( "COMMA", "," );
		TRIGGER_SYMBOLS.put( "PERIOD", "." );
		TRIGGER_SYMBOLS.put( "SLASH", "/" );
		TRIGGER_SYMBOLS.put( "SEMICOLON", ";" );
		TRIGGER_SYMBOLS.put( "EQUALS", "=" );
		TRIGGER_SYMBOLS.put( "OPEN_BRACKET", "[" );
		TRIGGER_SYMBOLS.put( "BACK_SLASH", "\\" );
		TRIGGER_SYMBOLS.put( "CLOSE_BRACKET", "]" );
		TRIGGER_SYMBOLS.put( "ctrl", "\u2303" );
		TRIGGER_SYMBOLS.put( "alt", "\u2387" );
		TRIGGER_SYMBOLS.put( "shift", "\u21e7" );
		TRIGGER_SYMBOLS.put( "meta", isMac() ? "\u2318" : "\u25c6" );
		TRIGGER_SYMBOLS.put( "win", "\u2756" );
		// Vertical bar is special
		TRIGGER_SYMBOLS.put( "|", "    |    " );
	}

	/**
	 * Sort tokens in a visually pleasing way. Makes sure we do not mess with
	 * the '|' syntax.
	 */
	private static final void sortTokens( final String[] tokens )
	{
		int vbarIndex = -1;
		for ( int i = 0; i < tokens.length; i++ )
		{
			if ( tokens[ i ].equals( "|" ) )
			{
				vbarIndex = i;
				break;
			}
		}
		if ( vbarIndex >= 0 )
		{
			Arrays.sort( tokens, 0, vbarIndex, Comparator.comparingInt( INPUT_TRIGGER_SYNTAX_TAGS::indexOf ) );
			Arrays.sort( tokens, vbarIndex + 1, tokens.length, Comparator.comparingInt( INPUT_TRIGGER_SYNTAX_TAGS::indexOf ) );
		}
		else
			Arrays.sort( tokens, Comparator.comparingInt( INPUT_TRIGGER_SYNTAX_TAGS::indexOf ) );
	}

	private static boolean isMac()
	{
		final String OS = System.getProperty( "os.name", "generic" ).toLowerCase( Locale.ENGLISH );
		return ( OS.indexOf( "mac" ) >= 0 ) || ( OS.indexOf( "darwin" ) >= 0 );
	}

	/**
	 * Mix colors {@code c1} and {@code c2} by ratios {@code c1Weight} and
	 * {@code (1-c1Weight)}, respectively.
	 */
	static Color mix( final Color c1, final Color c2, final double c1Weight )
	{
		final double c2Weight = 1.0 - c1Weight;
		return new Color(
				( int ) ( c1.getRed() * c1Weight + c2.getRed() * c2Weight ),
				( int ) ( c1.getGreen() * c1Weight + c2.getGreen() * c2Weight ),
				( int ) ( c1.getBlue() * c1Weight + c2.getBlue() * c2Weight ) );
	}
}
