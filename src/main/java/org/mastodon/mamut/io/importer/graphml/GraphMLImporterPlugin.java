/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.importer.graphml;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.mastodon.app.MastodonIcons;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.viewer.SourceAndConverter;

@Plugin( type = MamutPlugin.class )
public class GraphMLImporterPlugin implements MamutPlugin
{

	private static final String IMPORT_GRAPHML = "import graphml";
	private static final String[] IMPORT_GRAPHML_KEYS = new String[] { "not mapped" };

	private ProjectModel projectModel;

	private final RunnableAction importGraphMLAction;

	private GraphMLImporterPanel panel;

	public GraphMLImporterPlugin()
	{
		this.importGraphMLAction = new RunnableAction( IMPORT_GRAPHML, this::importGraphML );
	}

	@Override
	public void setAppPluginModel( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
		final ArrayList< SourceAndConverter< ? > > sources = projectModel.getSharedBdvData().getSources();
		this.panel = new GraphMLImporterPanel( sources, projectModel.getModel().getSpaceUnits() );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		final Map< String, String > menuTexts = new HashMap<>();
		menuTexts.put( IMPORT_GRAPHML, "Import GraphML" );
		return menuTexts;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins", menu( "Imports", item( IMPORT_GRAPHML ) ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( importGraphMLAction, IMPORT_GRAPHML_KEYS );
	}

	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add(
					IMPORT_GRAPHML,
					IMPORT_GRAPHML_KEYS,
					"Import a GraphML file into the current model." );
		}
	}

	private void importGraphML()
	{
		final int answer = JOptionPane.showConfirmDialog( null,
				panel,
				"GraphML importer",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				MastodonIcons.MASTODON_ICON_MEDIUM );
		if ( answer != JOptionPane.OK_OPTION )
			return;

		final int setupID = panel.getSetupID();
		final double radius = panel.getRadius();
		final String path = panel.getPath();
		try
		{
			GraphMLImporter.importGraphML( path, projectModel, setupID, radius );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog(
					null,
					"Problem importing file " + path + "\n" + e.getMessage(),
					"GraphML importer",
					JOptionPane.ERROR_MESSAGE,
					MastodonIcons.MASTODON_ICON_MEDIUM );
		}
	}
}
