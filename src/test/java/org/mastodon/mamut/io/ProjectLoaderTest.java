package org.mastodon.mamut.io;

import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
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
import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * This unit test can be used to test if the ProjectLoader class can properly load and close a project file multiple times without causing memory leaks.
 */
public class ProjectLoaderTest
{
	@Test
	public void testLoadAndCloseProjectGarbageCollection() throws IOException, SpimDataException
	{
		Model model = new Model();
		Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		File mastodonFile = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			ProjectModel appModel = wrapAsAppModel( image, model, context, mastodonFile );
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
