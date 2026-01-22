package org.mastodon.mamut.io;

import java.io.IOException;

import org.mastodon.io.BaseProjectLoader;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.io.ProjectSaver.MamutAdapter;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

/**
 * Project loader for Mamut projects.
 */
public class ProjectLoader
{

	private static final MamutProjectLoader LOADER = new MamutProjectLoader();

	/**
	 * Opens a project. The GUI state is not restored.
	 *
	 * @param mastodonFile
	 *            path to a Mastodon file.
	 * @param context
	 *            the current context.
	 * @return the loaded {@link MamutAppModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static MamutAppModel open( final String mastodonFile, final Context context ) throws IOException, SpimDataException
	{
		return LOADER.open( mastodonFile, context );
	}

	/**
	 * Opens a project. The GUI state is not restored.
	 *
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @return the loaded {@link MamutAppModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static MamutAppModel open( final MamutProject project, final Context context ) throws IOException, SpimDataException
	{
		return LOADER.open( project, context );
	}

	/**
	 * Opens a specified project.
	 *
	 * @param mastodonFile
	 *            path to a Mastodon file.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return the loaded {@link MamutAppModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static MamutAppModel open( final String mastodonFile, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		return LOADER.open( mastodonFile, context, restoreGUIState, authorizeSubstituteDummyData );
	}

	/**
	 * Opens a specified project.
	 *
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return the loaded {@link MamutAppModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static MamutAppModel open( final MamutProject project, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		return LOADER.open( project, context, restoreGUIState, authorizeSubstituteDummyData );
	}

	/**
	 * Loads the image data stored in a project, and wraps in a
	 * {@link SharedBigDataViewerData}.
	 *
	 * @param project
	 *            the project.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return a new {@link SharedBigDataViewerData}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly, and
	 *             <code>authorizeSubstituteDummyData</code> is false.
	 */
	public static SharedBigDataViewerData loadImageData( final MamutProject project, final boolean authorizeSubstituteDummyData ) throws SpimDataException, IOException
	{
		return LOADER.loadImageData( project, authorizeSubstituteDummyData );
	}

	/**
	 * Loads a {@link Model} from a project file.
	 *
	 * @param project
	 *            the project to load from.
	 * @param context
	 *            the current context, used to get feature serializers.
	 * @return a new model.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	public static final Model loadModel( final MamutProject project, final Context context ) throws IOException
	{
		return LOADER.loadModel( project, context );
	}

	/**
	 * Recreates the GUI configuration saved in the specified project. When
	 * calling this method, windows are created and shown on the display.
	 *
	 * @param project
	 *            the project to read from.
	 * @param appModel
	 *            the application model, used to restore the GUI state into.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	public static final void loadGUI( final MamutProject project, final MamutAppModel appModel ) throws IOException
	{
		LOADER.loadGUI( project, appModel );
	}

	private static class MamutProjectLoader extends BaseProjectLoader< MamutAppModel, Model, Spot, Link >
	{

		protected MamutProjectLoader()
		{
			super( new MamutAdapter() );
		}
	}
}
