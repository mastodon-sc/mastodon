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
package org.mastodon.mamut;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.ProjectSaver;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

/**
 * Tests {@link ProjectManager}.
 *
 * @author Matthias Arzt
 */
public class ProjectManagerTest
{

	private final Path tinyExampleProject = resourceAsFile( "/org/mastodon/mamut/examples/tiny/tiny-project.mastodon" );

	private final Context context = new Context();

	/** Test if a saved project contains a dataset.xml.backup. */
	@Test
	public void testSaveDatasetXmlBackup() throws IOException, SpimDataException
	{
		assumeFalse( GraphicsEnvironment.isHeadless() );
		final Path outputProject = Files.createTempFile( "test", ".mastodon" );
		openAndSaveMastodonProject( tinyExampleProject, outputProject );
		assertProjectContainsBackupDatasetXml( outputProject );
	}

	/** Test if a project doesn't lose its dataset.xml.backup if it's resaved. */
	@Test
	public void testSaveDatasetXmlBackupUnderManyConditions() throws IOException, SpimDataException
	{
		assumeFalse( GraphicsEnvironment.isHeadless() );
		// The following use case is tested.
		final Path projectA = Files.createTempFile( "test", ".mastodon" );
		// 1. Open mastodon project and save it
		openAndSaveMastodonProject( tinyExampleProject, projectA );
		assertProjectContainsBackupDatasetXml( projectA );
		// 2. Move mastodon project to new location, relative path to dataset.xml gets lost
		final Path newLocation = Files.createTempDirectory( "mastodon-test" );
		final Path projectB = newLocation.resolve( "moved-project.mastodon" );
		Files.move( projectA, projectB );
		assertProjectContainsBackupDatasetXml( projectB );
		// 3. Save mastodon project inplace
		openAndSaveMastodonProject( projectB, projectB );
		assertProjectContainsBackupDatasetXml( projectB );
		// 4. Save mastodon project (not inplace)
		final Path projectC = newLocation.resolve( "moved-project.mastodon" );
		openAndSaveMastodonProject( projectB, projectC );
		assertProjectContainsBackupDatasetXml( projectC );
	}

	private void openAndSaveMastodonProject( final Path open, final Path save )
			throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( open.toFile().getAbsolutePath() );
		final ProjectModel appModel = ProjectLoader.open( project, context, false, true );
		ProjectSaver.saveProject( save.toFile(), appModel );
	}

	private void assertProjectContainsBackupDatasetXml( final Path project )
			throws IOException
	{
		try (ZipFile zipFile = new ZipFile( project.toFile() ))
		{
			final boolean containsBackupXml = zipFile.stream()
					.anyMatch( entry -> "dataset.xml.backup".equals( entry.getName() ) );
			assertTrue( containsBackupXml );
		}
	}

	private Path resourceAsFile( final String resourceName )
	{
		try
		{
			return Paths.get( ProjectManagerTest.class.getResource( resourceName ).toURI() );
		}
		catch ( final URISyntaxException e )
		{
			throw new RuntimeException( e );
		}
	}
}
