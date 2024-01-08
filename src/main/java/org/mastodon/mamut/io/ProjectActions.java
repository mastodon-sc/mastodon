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
package org.mastodon.mamut.io;

import java.awt.Component;
import java.awt.Frame;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.launcher.LauncherUtil;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
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
	 * 
	 * @param actions
	 *            the {@link Actions} to register to.
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent when showing dialogs. Can be
	 *            <code>null</code>.
	 */
	public static void installGlobalActions( final Actions actions, final Context context, final Component parentComponent )
	{
		final BasicErrorLogger errorLogger = new BasicErrorLogger( parentComponent );
		final RunnableAction createProjectAction = new RunnableAction( CREATE_PROJECT, () -> LauncherUtil.createProjectWithDialog( context, parentComponent, errorLogger ) );
		final RunnableAction loadProjectAction = new RunnableAction( LOAD_PROJECT, () -> LauncherUtil.openWithDialog( context, parentComponent, errorLogger ) );
		final RunnableAction importMamutAction = new RunnableAction( IMPORT_MAMUT, () -> ProjectImporter.openMamutWithDialog( parentComponent, context, errorLogger ) );

		actions.namedAction( createProjectAction, CREATE_PROJECT_KEYS );
		actions.namedAction( loadProjectAction, LOAD_PROJECT_KEYS );
		actions.namedAction( importMamutAction, IMPORT_MAMUT_KEYS );
	}

	/**
	 * Installs the save and import actions onto the specified {@link Actions}
	 * object.
	 * 
	 * @param actions
	 *            the actions to install to.
	 * @param appModel
	 *            the project model.
	 * @param parentComponent
	 *            a component to use as parent in dialog (can be
	 *            <code>null</code>).
	 */
	public static void installAppActions( final Actions actions, final ProjectModel appModel, final Frame parentComponent )
	{
		final RunnableAction saveProjectAction = new RunnableAction( SAVE_PROJECT, runInNewThread( () -> ProjectSaver.saveProject( appModel, parentComponent ) ) );
		final RunnableAction saveProjectAsAction = new RunnableAction( SAVE_PROJECT_AS, runInNewThread( () -> ProjectSaver.saveProjectAs( appModel, parentComponent ) ) );
		final RunnableAction importTgmmAction = new RunnableAction( IMPORT_TGMM, () -> ProjectImporter.importTgmmDataWithDialog( appModel, parentComponent ) );
		final RunnableAction importSimiAction = new RunnableAction( IMPORT_TGMM, () -> ProjectImporter.importSimiDataWithDialog( appModel, parentComponent ) );
		final RunnableAction exportMamutAction = new RunnableAction( EXPORT_MAMUT, () -> ProjectExporter.exportMamut( appModel, parentComponent ) );

		actions.namedAction( saveProjectAction, SAVE_PROJECT_KEYS );
		actions.namedAction( saveProjectAsAction, SAVE_PROJECT_AS_KEYS );
		actions.namedAction( importTgmmAction, IMPORT_TGMM_KEYS );
		actions.namedAction( importSimiAction, IMPORT_SIMI_KEYS );
		actions.namedAction( exportMamutAction, EXPORT_MAMUT_KEYS );
	}

	private static Runnable runInNewThread( Runnable o )
	{
		return () -> {
			new Thread( () -> {
				try
				{
					o.run();
				}
				catch ( Throwable t )
				{
					t.printStackTrace();
				}
			} ).start();
		};
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
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

	private static final class BasicErrorLogger implements Consumer< String >
	{

		private final Component parent;

		public BasicErrorLogger( final Component parent )
		{
			this.parent = parent;
		}

		@Override
		public void accept( final String error )
		{
			JOptionPane.showMessageDialog(
					parent,
					"Problem reading Mastodon file:\n" + error,
					"Error reading Mastodon file",
					JOptionPane.ERROR_MESSAGE );
		}
	};
}
