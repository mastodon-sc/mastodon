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
package org.mastodon.ui.keymap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;

/**
 * Extends {@link KeymapManager} so that we can load the builtin keymaps for
 * Mastodon, and save keymaps in the Mastodon folder.
 */
public class MastodonKeymapManager extends KeymapManager
{

	private static final String CONFIG_DIR = System.getProperty( "user.home" ) + "/.mastodon/";

	private static List< Keymap > loadedBuiltinStyles = null;

	public MastodonKeymapManager()
	{
		super();
	}

	public MastodonKeymapManager( final boolean loadStyles )
	{
		super( CONFIG_DIR );
	}

	@Override
	protected List< Keymap > loadBuiltinStyles()
	{
		try
		{
			synchronized ( KeymapManager.class )
			{
				if ( loadedBuiltinStyles == null )
					loadedBuiltinStyles = Arrays.asList(
							loadBuiltinStyle( "Default", "keyconf_mastodon.yaml" ),
							loadBuiltinStyle( "All BDV keys", "keyconf_fullbdv.yaml" ) );
				return loadedBuiltinStyles;
			}
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static Keymap loadBuiltinStyle( final String name, final String filename ) throws IOException
	{
		final InputStreamReader reader = new InputStreamReader( MastodonKeymapManager.class.getResourceAsStream( filename ) );
		final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( reader ) );
		reader.close();
		return new Keymap( name, config );
	}
}
