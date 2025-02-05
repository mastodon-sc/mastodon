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
package org.mastodon.mamut.io;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Model;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectSaverTest
{
	@Test
	public void testSaveProjectToZip() throws IOException
	{
		try (Context context = new Context())
		{
			Model model = new Model();
			Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			File mastodonFile = File.createTempFile( "test", ".mastodon" );
			ProjectModel appModel = wrapAsAppModel( image, model, context, mastodonFile );
			ProjectSaver.saveProject( mastodonFile, appModel );
			assertNotNull( mastodonFile );
			assertTrue( mastodonFile.exists() );
			assertTrue( mastodonFile.isFile() );
			assertTrue( mastodonFile.length() > 0 );
			assertEquals( mastodonFile, appModel.getProject().getProjectRoot() );

			ProjectSaver.saveProject( mastodonFile, appModel ); // Overwrite again
			assertTrue( mastodonFile.exists() );
			assertTrue( mastodonFile.isFile() );
			assertTrue( mastodonFile.length() > 0 );
			assertEquals( mastodonFile, appModel.getProject().getProjectRoot() );
		}
	}

	@Test
	public void testSaveProjectToDirectory() throws IOException
	{
		try (Context context = new Context())
		{
			Model model = new Model();
			Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			File mastodonDirectory = Files.createTempDirectory( "test" ).toFile();
			ProjectModel appModel = wrapAsAppModel( image, model, context, mastodonDirectory );
			ProjectSaver.saveProject( mastodonDirectory, appModel );
			assertNotNull( mastodonDirectory );
			assertTrue( mastodonDirectory.exists() );
			assertTrue( mastodonDirectory.isDirectory() );
			assertEquals( mastodonDirectory, appModel.getProject().getProjectRoot() );
			File[] files = mastodonDirectory.listFiles();
			assertNotNull( files );
			assertEquals( 6, files.length );

			ProjectSaver.saveProject( mastodonDirectory, appModel ); // Overwrite again
			assertTrue( mastodonDirectory.exists() );
			assertTrue( mastodonDirectory.isDirectory() );
			assertEquals( mastodonDirectory, appModel.getProject().getProjectRoot() );
			files = mastodonDirectory.listFiles();
			assertNotNull( files );
			assertEquals( 6, files.length );
		}
	}

	private static ProjectModel wrapAsAppModel( final Img< FloatType > image, final Model model, final Context context, final File file )
			throws IOException
	{
		final SharedBigDataViewerData sharedBigDataViewerData = asSharedBdvDataXyz( image );
		MamutProject mamutProject = new MamutProject( file );
		File datasetXmlFile = File.createTempFile( "test", ".xml" );
		mamutProject.setDatasetXmlFile( datasetXmlFile );
		return ProjectModel.create( context, model, sharedBigDataViewerData, mamutProject );
	}

	private static SharedBigDataViewerData asSharedBdvDataXyz( final Img< FloatType > image1 )
	{
		final ImagePlus image =
				ImgToVirtualStack.wrap( new ImgPlus<>( image1, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.TIME } ) );
		return Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( image ) );
	}
}
