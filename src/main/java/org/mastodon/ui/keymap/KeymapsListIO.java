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
package org.mastodon.ui.keymap;

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

	public KeymapsListIO( final String defaultName, final List< String > keymapNames )
	{
		defaultKeymapName = defaultName;
		keymapNameToFileName = new LinkedHashMap<>();

		final Set< String > existingNames = new HashSet<>();
		existingNames.add( "keymaps" );
		for ( final String keymapName : keymapNames )
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
		final Map< String, String > map = new LinkedHashMap<>();
		for ( final Map.Entry< String, String > entry : keymapNameToFileName.entrySet() )
			map.put( entry.getValue(), entry.getKey() );
		return map;
	}

	public KeymapsListIO() // default constructor needed for snakeyaml
	{}
}
