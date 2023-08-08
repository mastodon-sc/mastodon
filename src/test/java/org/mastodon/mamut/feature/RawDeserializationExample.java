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
package org.mastodon.mamut.feature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.util.StopWatch;

public class RawDeserializationExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		// Load project.
		final WindowManager windowManager = new WindowManager( new Context() );
		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.mastodon" );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		// Compute features.
		final MamutFeatureComputerService featureComputerService =
				MamutFeatureComputerService.newInstance( windowManager.getContext() );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "\nComputing features..." );
		final StopWatch stopWatch = StopWatch.createAndStart();
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features =
				featureComputerService.compute( featureComputerService.getFeatureSpecs() );
		featureModel.clear();
		features.values().forEach( featureModel::declareFeature );
		stopWatch.stop();
		System.out.println( String.format( "Done in %.1f s.", stopWatch.nanoTime() / 1e9 ) );

		final File targetFile = new File( "samples/featureserialized-folder" );
		targetFile.mkdir();
		System.out.println( "\nResaving in a project folder." );
		windowManager.getProjectManager().saveProject( targetFile );
		System.out.println( "Done." );
		System.out.println( "----------------------------------\n\n" );

		/*
		 * Now access the .raw files directly. A method is required for each
		 * file, as we cannot use the generic feature serializer. We need to
		 * know how the feature was serialized.
		 */
		deserializeRawSpotNLinks( "samples/featureserialized-folder/features/Spot N links.raw" );
		System.out.println( "----------------------------------\n\n" );
		deserializeRawSpotIntensity( "samples/featureserialized-folder/features/Spot gaussian-filtered intensity.raw" );
		System.out.println( "----------------------------------\n\n" );
		deserializeRawLinkVelocity( "samples/featureserialized-folder/features/Link velocity.raw" );
	}

	private static void deserializeRawLinkVelocity( final String filename ) throws IOException
	{
		final StringBuilder str = new StringBuilder();
		try (
				final FileInputStream fis = new FileInputStream( filename );
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) ))
		{
			// UNITS.
			final String units = ois.readUTF();
			str.append( filename + ", units = " + units + "\n" );
			readDoubleMap( str, ois );
		}
		System.out.println( str.toString() );
	}

	private static void deserializeRawSpotIntensity( final String filename ) throws FileNotFoundException, IOException
	{
		final StringBuilder str = new StringBuilder();
		try (
				final FileInputStream fis = new FileInputStream( filename );
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) ))
		{
			// NUMBER OF SOURCES.
			final int nSources = ois.readInt();
			str.append( filename + ": " + nSources + " sources.\n" );
			for ( int i = 0; i < nSources; i++ )
			{
				str.append( "--------------------------\nMean of source " + i + "\n" );
				readDoubleMap( str, ois );
				str.append( "--------------------------\nStd of source " + i + "\n" );
				readDoubleMap( str, ois );
			}
		}
		System.out.println( str.toString() );
	}

	private static void readDoubleMap( final StringBuilder str, final ObjectInputStream ois ) throws IOException
	{
		// NUMBER OF ENTRIES
		final int size = ois.readInt();
		str.append( String.format( "%9s | %9s\n", "key", "value" ) );
		str.append( "--------------------------\n" );

		// ENTRIES
		for ( int i = 0; i < size; i++ )
		{
			final int key = ois.readInt();
			final double value = ois.readDouble();
			str.append( String.format( "%9d | %9.1f\n", key, value ) );
		}
	}

	private static void deserializeRawSpotNLinks( final String filename ) throws FileNotFoundException, IOException
	{
		final StringBuilder str = new StringBuilder();
		try (
				final FileInputStream fis = new FileInputStream( filename );
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) ))
		{
			// NUMBER OF ENTRIES.
			final int size = ois.readInt();
			str.append( filename + ": " + size + " entries.\n" );
			str.append( String.format( "%9s | %9s\n", "key", "value" ) );
			str.append( "--------------------------\n" );

			// ENTRIES
			for ( int i = 0; i < size; i++ )
			{
				final int key = ois.readInt();
				final int value = ois.readInt();
				str.append( String.format( "%9d | %9d\n", key, value ) );
			}
		}
		System.out.println( str.toString() );
	}
}
