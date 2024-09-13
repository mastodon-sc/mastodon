package org.mastodon.mamut.io;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * This unit test can be used to test if the ProjectLoader class can properly load and close a project file multiple times without causing memory leaks.
 */
public class ProjectLoaderTest
{
	@Ignore( "The run time of this test is too long for a unit test that is run on every build." )
	@Test
	public void testLoadAndCloseProjectGarbageCollection() throws IOException, SpimDataException
	{
		Model model = new Model();
		Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		File mastodonFile = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			ProjectModel appModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			ProjectSaver.saveProject( mastodonFile, appModel );
		}
		for ( int i = 0; i < 100; i++ )
			loadAndCloseProjectModel( mastodonFile );
		assertTrue( true );
	}

	private void loadAndCloseProjectModel( final File mastodonFile ) throws SpimDataException, IOException
	{
		try (Context context = new Context())
		{
			ProjectModel projectModel = ProjectLoader.open( mastodonFile.getAbsolutePath(), context, false, true );
			projectModel.close();
		}
	}
}
