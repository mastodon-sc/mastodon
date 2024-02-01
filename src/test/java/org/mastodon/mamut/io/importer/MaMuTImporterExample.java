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
package org.mastodon.mamut.io.importer;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.ProjectSaver;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MaMuTImporterExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

//		final File mamutFile = new File( "D:/Projects/JYTinevez/MaMuT/Mastodon-dataset/MaMuT_Parhyale_demo-mamut.xml" );
		final File mamutFile = new File( "/Users/tinevez/Projects/JYTinevez/MaMuT/Mastodon-dataset/MaMuT_Parhyale_demo-mamut.xml" );
		final File targetMastodonFile = new File( "samples/trackmateimported.mastodon" );

		importFromMaMuTAndSave( mamutFile, targetMastodonFile );

		reloadAfterSave( targetMastodonFile );
	}

	private static void importFromMaMuTAndSave( final File mamutFile, final File targetMastodonFile ) throws IOException, SpimDataException
	{
		final Context context = new Context();
		final TrackMateImporter importer = new TrackMateImporter( mamutFile );
		final ProjectModel appModel = ProjectLoader.open( importer.createProject(), context );
		final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
		importer.readModel( appModel.getModel(), featureSpecsService );
		ProjectSaver.saveProject( targetMastodonFile, appModel );
	}

	private static void reloadAfterSave( final File targetMastodonFile ) throws IOException, SpimDataException
	{
		final ProjectModel appModel = ProjectLoader.open( MamutProjectIO.load( targetMastodonFile.getAbsolutePath() ), new Context() );
		final Model model = appModel.getModel();
		System.out.println( "After reloading the saved MaMuT import:" );
		System.out.println( ModelUtils.dump( model, 5 ) );
	}
}
