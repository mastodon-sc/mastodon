package org.mastodon.mamut.io;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.io.AppIOAdapter;
import org.mastodon.io.BaseProjectSaver;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectReader;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

/**
 * Project saver for Mamut projects.
 */
public class ProjectSaver
{

	private static final MamutProjectSaver SAVER = new MamutProjectSaver();

	public static final String EXT_DOT_MASTODON = "mastodon";

	/**
	 * Saves the specified project to the specified file.
	 *
	 * @param saveTo
	 *            the file to save the project to.
	 * @param appModel
	 *            the project model.
	 * @throws IOException
	 *             if there is an error writing to the file.
	 */
	public static synchronized void saveProjectAs( final MamutAppModel appModel, final Component parentComponent )
	{
		SAVER.saveProjectAs( appModel, parentComponent );
	}

	/**
	 * Saves the specified project to the file specified in its
	 * {@link MamutProject} object.
	 *
	 * @param appModel
	 *            the project model.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 */
	public static void saveProject( final MamutAppModel appModel, final Component parentComponent )
	{
		SAVER.saveProject( appModel, parentComponent );
	}

	/**
	 * Saves the specified project to the specified file. The file should be a
	 * path ending in <code>.mastodon</code> (but folders from previous versions
	 * are supported).
	 *
	 * @param saveTo
	 *            the file to save the project to.
	 * @param appModel
	 *            the project model.
	 * @throws IOException
	 *             if there is an error writing to the file.
	 */
	public static synchronized void saveProject( final File saveTo, final MamutAppModel appModel ) throws IOException
	{
		SAVER.saveProject( saveTo, appModel );
	}

	private static class MamutProjectSaver extends BaseProjectSaver< MamutAppModel, Model, Spot, Link >
	{

		private MamutProjectSaver()
		{
			super( new MamutAdapter() );
		}
	}

	/**
	 * Adapter implementation for Mamut.
	 */
	static class MamutAdapter implements AppIOAdapter< MamutAppModel, Model, Spot, Link >
	{

		@Override
		public MamutProject getProject( final MamutAppModel appModel )
		{
			return appModel.getProject();
		}

		@Override
		public void syncBranchGraph( final Model dataModel )
		{
			dataModel.branchGraphSync().sync();
		}

		@Override
		public GraphToFileIdMap< Spot, Link > saveRawGraphModel( final Model dataModel, final org.mastodon.mamut.io.project.MamutProject.ProjectWriter writer ) throws IOException
		{
			return dataModel.saveRaw( writer );
		}

		@Override
		public void serializeFeatureModel( final Context context, final Model dataModel, final GraphToFileIdMap< Spot, Link > idmap, final org.mastodon.mamut.io.project.MamutProject.ProjectWriter writer ) throws IOException
		{
			MamutRawFeatureModelIO.serialize( context, dataModel, idmap, writer );
		}

		@Override
		public void setSavePoint( final Model dataModel )
		{
			dataModel.setSavePoint();
		}

		@Override
		public MamutAppModel createAppModel( final Context context, final Model dataModel, final SharedBigDataViewerData sbdv, final MamutProject project )
		{
			return MamutAppModel.create( context, dataModel, sbdv, project );
		}

		@Override
		public void openMainWindow( final MamutAppModel appModel )
		{
			new org.mastodon.mamut.MainWindow( appModel ).setVisible( true );
		}

		@Override
		public String getFileExtension()
		{
			return EXT_DOT_MASTODON;
		}

		@Override
		public String getProjectTypeName()
		{
			return "Mastodon Project";
		}

		@Override
		public FileIdToGraphMap< Spot, Link > loadRawGraphModel( final Model dataModel, final ProjectReader reader ) throws IOException
		{
			return dataModel.loadRaw( reader );
		}

		@Override
		public void deserializeFeatureModel( final Context context, final Model dataModel, final FileIdToGraphMap< Spot, Link > idmap, final ProjectReader reader ) throws IOException, ClassNotFoundException
		{
			MamutRawFeatureModelIO.deserialize(
					context,
					dataModel,
					idmap,
					reader );
		}

		@Override
		public void declareDefaultFeatures( final Model dataModel )
		{
			dataModel.declareDefaultFeatures();
		}

		@Override
		public Model initializeNewModel( final MamutProject project )
		{
			return new Model( project.getSpaceUnits(), project.getTimeUnits() );
		}

		@Override
		public String requiredImageSizeAsString( final Model model )
		{
			int time = 0;
			double x = 0;
			double y = 0;
			double z = 0;
			for ( final Spot spot : model.getGraph().vertices() )
		{
				time = Math.max( time, spot.getTimepoint() );
				final double radius = Math.sqrt( spot.getBoundingSphereRadiusSquared() );
				x = Math.max( x, spot.getDoublePosition( 0 ) + radius );
				y = Math.max( y, spot.getDoublePosition( 1 ) + radius );
				z = Math.max( z, spot.getDoublePosition( 2 ) + radius );
			}
			return String.format( "x=%s y=%s z=%s t=%s.dummy",
					roundUp( x ) + 1,
					roundUp( y ) + 1,
					roundUp( z ) + 1,
					time + 1 );
		}

		private static long roundUp( final double x )
		{
			return ( long ) Math.ceil( x );
		}

	}
}
