/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.util;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;

import org.mastodon.util.GroupStrings.Group;

/**
 * Utility class used to automatically assign sensible mnemonics in JMenus.
 * <p>
 * One instance of this iterator is instantiated per menu. As the menu is built,
 * add the menu item with {@link #add(AbstractButton)} method. When all the menu
 * items have been created and added, call the {@link #assignMnemonics()}
 * methods. The mnemonic assigned will be unique for all the menu items, and
 * will be taken from the first letter of the name not already assigned to
 * another mnemonic. If all letters of the name are taken, then no mnemonics are
 * assigned.
 * 
 * @author Jean-Yves Tinevez
 */
public class MnemonicsAssigner
{

	private final Map< String, AbstractButton > buttons;

	public MnemonicsAssigner()
	{
		this.buttons = new HashMap<>();
	}

	public void add( final AbstractButton button )
	{
		buttons.put( button.getText(), button );
	}

	public void assignMnemonics()
	{
		final GroupStrings grouper = new GroupStrings();

		for ( final String text : buttons.keySet() )
			grouper.add( text );

		final Set< Integer > mnemonics = new HashSet<>();
		final Collection< Group > groups = grouper.group();
		for ( final Group group : groups )
		{
			for ( final String string : group.strings )
			{
				final AbstractButton button = buttons.get( string );
				final String suffix = group.suffix( string );
				for ( final char c : suffix.toLowerCase().toCharArray() )
				{
					if ( c < 'a' || c > 'z' )
						continue;
					final int keyCode = KeyEvent.getExtendedKeyCodeForChar( c );
					if ( !mnemonics.contains( Integer.valueOf( keyCode ) ) )
					{
						mnemonics.add( Integer.valueOf( keyCode ) );
						button.setMnemonic( keyCode );
						break;
					}
				}
			}
		}
	}
}
