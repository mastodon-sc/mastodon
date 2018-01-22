package org.mastodon.revised.ui.keymap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KeymapsListIO
{
	public String defaultKeymapName;

	public LinkedHashMap< String, String > keymapNameToFileName;

	public KeymapsListIO( String defaultName, final List< String > keymapNames )
	{
		defaultKeymapName = defaultName;
		keymapNameToFileName = new LinkedHashMap<>();

		Set< String > existingNames = new HashSet<>();
		existingNames.add( "keymaps" );
		for ( String keymapName : keymapNames )
		{
			String name = keymapName.replaceAll( "\\W+", "" );
			if ( existingNames.contains( name ) )
			{
				final Pattern pattern = Pattern.compile( "(.+)_(\\d+)$" );
				final Matcher matcher = pattern.matcher( name );
				int n;
				String prefix;
				if ( matcher.matches() )
				{
					final String nstr = matcher.group( 2 );
					n = Integer.parseInt( nstr );
					prefix = matcher.group( 1 );
				}
				else
				{
					n = 1;
					prefix = name;
				}

				do
					name = prefix + "_" + ( ++n );
				while ( existingNames.contains( name ) );
			}
			keymapNameToFileName.put( keymapName, name + ".yaml" );
		}
	}

	public Map< String, String > getFileNameToKeymapName()
	{
		Map< String, String > map = new LinkedHashMap<>();
		for ( Map.Entry< String, String > entry : keymapNameToFileName.entrySet() )
			map.put( entry.getValue(), entry.getKey() );
		return map;
	}

	public KeymapsListIO()
	{
	}
}

