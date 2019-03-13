package org.mastodon.revised.util;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;

import org.mastodon.revised.util.GroupStrings.Group;

/**
 * Utility class used to automatically assign sensible mnemonics in JMenus.
 * <p>
 * One instance of this iterator is instantiated per menu. As the menu is built,
 * add the menu item with {@link #add(AbstractButton)} method. When all the menu
 * items have been created and added, call the {@link #assignMnenonics()}
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
		this.buttons = new HashMap< >();
	}

	public void add( final AbstractButton button )
	{
		buttons.put( button.getText(), button );
	}

	public void assignMnenonics()
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
