package org.mastodon.mamut.io;

import java.awt.Component;
import java.awt.Frame;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * Collect actions, descriptions and keys related to project loading, saving,
 * importing and exporting actions.
 */
public class ProjectActions
{
	public static final String CREATE_PROJECT = "create new project";
	public static final String CREATE_PROJECT_FROM_URL = "create new project from url";
	public static final String LOAD_PROJECT = "load project";
	public static final String SAVE_PROJECT = "save project";
	public static final String SAVE_PROJECT_AS = "save project as";
	public static final String IMPORT_TGMM = "import tgmm";
	public static final String IMPORT_SIMI = "import simi";
	public static final String IMPORT_MAMUT = "import mamut";
	public static final String EXPORT_MAMUT = "export mamut";

	static final String[] CREATE_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] CREATE_PROJECT_FROM_URL_KEYS = new String[] { "not mapped" };
	static final String[] LOAD_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] SAVE_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] SAVE_PROJECT_AS_KEYS = new String[] { "not mapped" };
	static final String[] IMPORT_TGMM_KEYS = new String[] { "not mapped" };
	static final String[] IMPORT_SIMI_KEYS = new String[] { "not mapped" };
	static final String[] IMPORT_MAMUT_KEYS = new String[] { "not mapped" };
	static final String[] EXPORT_MAMUT_KEYS = new String[] { "not mapped" };

	/**
	 * Install the global actions for creating, loading or importing a new
	 * project. These actions relates to the app context where an app model does
	 * not exist yet (they create it).
	 */
	public static void installGlobalActions( final Actions actions, final Context context, final Component parentComponent )
	{
		final RunnableAction createProjectAction = new RunnableAction( CREATE_PROJECT, () -> ProjectCreator.createProjectWithDialog( context, parentComponent ) );
		final RunnableAction createProjectFromUrlAction = new RunnableAction( CREATE_PROJECT_FROM_URL, () -> ProjectCreator.createProjectFromUrl( context, parentComponent ) );
		final RunnableAction loadProjectAction = new RunnableAction( LOAD_PROJECT, () -> ProjectLoader.openWithDialog( context, parentComponent ) );
		final RunnableAction importMamutAction = new RunnableAction( IMPORT_MAMUT, () -> ProjectImporter.openMamutWithDialog( parentComponent, context ) );

		actions.namedAction( createProjectAction, CREATE_PROJECT_KEYS );
		actions.namedAction( createProjectFromUrlAction, CREATE_PROJECT_FROM_URL_KEYS );
		actions.namedAction( loadProjectAction, LOAD_PROJECT_KEYS );
		actions.namedAction( importMamutAction, IMPORT_MAMUT_KEYS );
	}

	public static void installAppActions( final Actions actions, final ProjectModel appModel, final Frame parentComponent )
	{
		final RunnableAction saveProjectAction = new RunnableAction( SAVE_PROJECT, () -> ProjectSaver.saveProject( appModel, parentComponent ) );
		final RunnableAction saveProjectAsAction = new RunnableAction( SAVE_PROJECT_AS, () -> ProjectSaver.saveProjectAs( appModel, parentComponent ) );
		final RunnableAction importTgmmAction = new RunnableAction( IMPORT_TGMM, () -> ProjectImporter.importTgmmDataWithDialog( appModel, parentComponent ) );
		final RunnableAction importSimiAction = new RunnableAction( IMPORT_TGMM, () -> ProjectImporter.importSimiDataWithDialog( appModel, parentComponent ) );
		final RunnableAction exportMamutAction = new RunnableAction( EXPORT_MAMUT, () -> ProjectExporter.exportMamut( appModel, parentComponent ) );

		actions.namedAction( saveProjectAction, SAVE_PROJECT_KEYS );
		actions.namedAction( saveProjectAsAction, SAVE_PROJECT_AS_KEYS );
		actions.namedAction( importTgmmAction, IMPORT_TGMM_KEYS );
		actions.namedAction( importSimiAction, IMPORT_SIMI_KEYS );
		actions.namedAction( exportMamutAction, EXPORT_MAMUT_KEYS );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( CREATE_PROJECT, CREATE_PROJECT_KEYS, "Create a new project." );
			descriptions.add( CREATE_PROJECT_FROM_URL, CREATE_PROJECT_FROM_URL_KEYS, "Create a new project from URL." );
			descriptions.add( LOAD_PROJECT, LOAD_PROJECT_KEYS, "Load a project." );
			descriptions.add( SAVE_PROJECT, SAVE_PROJECT_KEYS, "Save the current project." );
			descriptions.add( SAVE_PROJECT_AS, SAVE_PROJECT_AS_KEYS, "Save the current project in a new file." );
			descriptions.add( IMPORT_TGMM, IMPORT_TGMM_KEYS,
					"Import tracks from TGMM xml files into the current project." );
			descriptions.add( IMPORT_SIMI, IMPORT_SIMI_KEYS,
					"Import tracks from a Simi Biocell .sbd into the current project." );
			descriptions.add( IMPORT_MAMUT, IMPORT_MAMUT_KEYS, "Import a MaMuT project." );
			descriptions.add( EXPORT_MAMUT, EXPORT_MAMUT_KEYS, "Export current project as a MaMuT project." );
		}
	}
}
