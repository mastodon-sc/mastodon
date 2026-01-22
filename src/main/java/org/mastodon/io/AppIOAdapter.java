package org.mastodon.io;

import java.io.IOException;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectReader;
import org.mastodon.mamut.io.project.MamutProject.ProjectWriter;
import org.mastodon.model.AbstractModelBranch.BranchModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.app.BdvAppModel;
import org.mastodon.model.app.WindowManager;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

/**
 * Adapter interface for app-specific project saving operations.
 *
 * @param <AM>
 *            the AppModel type (e.g., MamutAppModel, LeviathanAppModel)
 * @param <M>
 *            the DataModel type (e.g., Model, LeviathanModel)
 * @param <V>
 *            the graph vertex type (e.g., Spot, Segment)
 * @param <E>
 *            the graph edge type (e.g., Link, Arc)
 */
public interface AppIOAdapter<
		AM extends BdvAppModel< AM, M, ?, V, E >,
		M extends MastodonModel< ?, V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
{
	/**
	 * Gets the project from the app model.
	 *
	 * @param appModel
	 *            the app model
	 * @return the project
	 */
	MamutProject getProject( AM appModel );

	/**
	 * Gets the data model from the app model.
	 *
	 * @param appModel
	 *            the app model
	 * @return the data model
	 */
	default M getDataModel( final AM appModel )
	{
		return appModel.dataModel();
	}

	/**
	 * Gets the window manager from the app model.
	 *
	 * @param appModel
	 *            the app model
	 * @return the window manager
	 */
	default WindowManager< AM > getWindowManager( final AM appModel )
	{
		return appModel.windowManager();
	}

	/**
	 * Synchronizes the branch graph with the main graph before saving or after
	 * loading. This is useful only for {@link BranchModel}s.
	 *
	 * @param dataModel
	 *            the data model
	 */
	void syncBranchGraph( M dataModel );

	/**
	 * Saves the raw graph model and returns the vertex-to-ID mapping.
	 *
	 * @param dataModel
	 *            the data model
	 * @param writer
	 *            the project writer
	 * @return the graph ID mapping
	 * @throws IOException
	 *             if an error occurs
	 */
	GraphToFileIdMap< V, E > saveRawGraphModel( M dataModel, ProjectWriter writer ) throws java.io.IOException;

	/**
	 * Serializes the feature model to the project.
	 *
	 * @param context
	 *            the SciJava context
	 * @param dataModel
	 *            the data model
	 * @param idmap
	 *            the graph ID mapping
	 * @param writer
	 *            the project writer
	 * @throws IOException
	 *             if an error occurs
	 */
	void serializeFeatureModel( Context context, M dataModel, GraphToFileIdMap< V, E > idmap, ProjectWriter writer ) throws java.io.IOException;

	/**
	 * Sets the save point on the data model.
	 *
	 * @param dataModel
	 *            the data model
	 */
	void setSavePoint( M dataModel );

	/**
	 * Creates a new app model instance after image conversion.
	 *
	 * @param context
	 *            the SciJava context
	 * @param dataModel
	 *            the data model
	 * @param sbdv
	 *            the shared BDV data.
	 * @param project
	 *            the project
	 * @return the new app model
	 */
	AM createAppModel( Context context, M dataModel, SharedBigDataViewerData sbdv, MamutProject project );

	/**
	 * Opens the main window for the app model. This is called after loading the
	 * project.
	 *
	 * @param appModel
	 *            the app model
	 */
	void openMainWindow( AM appModel );

	/**
	 * Gets the file extension for this app's projects (without the dot).
	 *
	 * @return the file extension, e.g., "mastodon"
	 */
	String getFileExtension();

	/**
	 * Gets the project type name for display in dialogs.
	 *
	 * @return the project type name, e.g., "Mastodon Project"
	 */
	String getProjectTypeName();

	/**
	 * Loads the raw graph model and returns the file-ID-to-vertex mapping.
	 *
	 * @param dataModel
	 *            the data model to load into.
	 * @param reader
	 *            the project reader.
	 * @return the file ID to graph mapping.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	FileIdToGraphMap< V, E > loadRawGraphModel( M dataModel, ProjectReader reader ) throws IOException;

	/**
	 * Deserializes the feature model from the project.
	 *
	 * @param context
	 *            the SciJava context.
	 * @param dataModel
	 *            the data model.
	 * @param idmap
	 *            the file ID to graph mapping.
	 * @param reader
	 *            the project reader.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 * @throws ClassNotFoundException
	 */
	void deserializeFeatureModel( Context context, M dataModel, FileIdToGraphMap< V, E > idmap, ProjectReader reader ) throws IOException, ClassNotFoundException;

	/**
	 * Declares default features on the data model.
	 *
	 * @param dataModel
	 *            the data model.
	 */
	void declareDefaultFeatures( M dataModel );

	/**
	 * Initializes a new empty data model for a new project, that will be
	 * fleshed out when loading.
	 *
	 * @param project
	 *            the project.
	 * @return the new data model.
	 */
	M initializeNewModel( MamutProject project );

	/**
	 * Creates a string specifying an empty image size to be used as a dummy BDV
	 * image, large enough to encompass all existing objects in the model.
	 *
	 * @param model
	 *            the model.
	 * @return a dummy BDV string, in the line of "x=%s y=%s z=%s t=%s.dummy"
	 */
	String requiredImageSizeAsString( M model );
}