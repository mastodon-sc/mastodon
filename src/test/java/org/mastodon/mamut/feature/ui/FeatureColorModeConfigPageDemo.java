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
package org.mastodon.mamut.feature.ui;

import java.util.Locale;

import javax.swing.UIManager;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Model;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.Context;

import bdv.ui.keymap.Keymap;

public class FeatureColorModeConfigPageDemo
{
	private static final String FEATURECOLORMODE_SETTINGSPAGE_TREEPATH = "Feature Color Modes";

	public static void main( final String[] args ) throws Exception
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		try (final Context context = new Context( FeatureSpecsService.class ))
		{

			final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
			final MamutFeatureProjectionsManager featureProjectionsManager = new MamutFeatureProjectionsManager(
					context.getService( FeatureSpecsService.class ),
					featureColorModeManager );

			final PreferencesDialog settings =
					new PreferencesDialog( null, new Keymap(), new String[] { KeyConfigContexts.MASTODON } );
			settings.addPage( new FeatureColorModeConfigPage( FEATURECOLORMODE_SETTINGSPAGE_TREEPATH,
					featureColorModeManager,
					featureProjectionsManager,
					"Spot", "Link" ) );

			final MamutProject project = MamutProjectIO.load( "samples/drosophila_crop.mastodon" );
			final Model model = new Model();
			model.loadRaw( project.openForReading() );
			featureProjectionsManager.setModel( model, 3 );

			settings.pack();
			settings.setLocationRelativeTo( null );
			settings.setVisible( true );

		}
	}
}
