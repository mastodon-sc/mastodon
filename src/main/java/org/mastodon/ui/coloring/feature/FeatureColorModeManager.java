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
package org.mastodon.ui.coloring.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mastodon.app.ui.AbstractStyleManagerYaml;
import org.scijava.listeners.Listeners;
import org.yaml.snakeyaml.Yaml;

public class FeatureColorModeManager extends AbstractStyleManagerYaml< FeatureColorModeManager, FeatureColorMode >
{
	private static final String COLOR_MODE_FILE = System.getProperty( "user.home" ) + "/.mastodon/colormodes.yaml";

	public interface FeatureColorModesListener
	{
		public void featureColorModesChanged();
	}

	private final Listeners.List< FeatureColorModesListener > featureColorModesListeners;

	public FeatureColorModeManager()
	{
		this( true );
	}

	public FeatureColorModeManager( final boolean loadModes )
	{
		if ( loadModes )
			loadModes();
		featureColorModesListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public void set( final FeatureColorModeManager other )
	{
		super.set( other );
		notifyListeners();
	}

	public Listeners< FeatureColorModesListener > listeners()
	{
		return featureColorModesListeners;
	}

	@Override
	public void saveStyles()
	{
		saveStyles( COLOR_MODE_FILE );
	}

	public void loadModes()
	{
		loadStyles( COLOR_MODE_FILE );
	}

	@Override
	protected List< FeatureColorMode > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( FeatureColorMode.defaults ) );
	}

	private void notifyListeners()
	{
		featureColorModesListeners.list.forEach( FeatureColorModesListener::featureColorModesChanged );
	}

	@Override
	protected Yaml createYaml()
	{
		return FeatureColorModeIO.createYaml();
	}
}
