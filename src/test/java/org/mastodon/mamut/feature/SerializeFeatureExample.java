/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.ProjectSaver;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.util.StopWatch;

public class SerializeFeatureExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		// Load project.
		final MamutProject project = MamutProjectIO.load( "samples/mamutproject.mastodon" );
		final ProjectModel appModel = ProjectLoader.open( project, new Context() );
		final Model model = appModel.getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		// Compute features.
		final MamutFeatureComputerService featureComputerService =
				MamutFeatureComputerService.newInstance( appModel.getContext() );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( appModel.getSharedBdvData() );
		System.out.println( "\nComputing features..." );
		final StopWatch stopWatch = StopWatch.createAndStart();
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features =
				featureComputerService.compute( featureComputerService.getFeatureSpecs() );
		featureModel.clear();
		features.values().forEach( featureModel::declareFeature );
		stopWatch.stop();
		System.out.println( String.format( "Done in %.1f s.", stopWatch.nanoTime() / 1e9 ) );

		final File targetFile = new File( "samples/featureserialized.mastodon" );

		System.out.println( "\nResaving." );
		ProjectSaver.saveProject( targetFile, appModel );
		System.out.println( "Done." );

		System.out.println( "\nReloading." );
		final MamutProject project2 = MamutProjectIO.load( targetFile.getAbsolutePath() );
		final ProjectModel appModel2 = ProjectLoader.open( project2, appModel.getContext() );
		System.out.println( "Done." );

		System.out.println( "\n" + ModelUtils.dump( appModel2.getModel(), 4 ) );
	}
}
