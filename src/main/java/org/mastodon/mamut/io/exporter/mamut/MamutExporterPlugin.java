package org.mastodon.mamut.io.exporter.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.mastodon.app.MastodonIcons;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.trackmate.MamutExporter;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.DialogType;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = MamutPlugin.class )
public class MamutExporterPlugin implements MamutPlugin
{

	private static final String EXPORT_MAMUT = "export mamut";

	private static final String[] EXPORT_MAMUT_KEYS = new String[] { "not mapped" };

	private ProjectModel projectModel;

	private final RunnableAction exportAction;

	public MamutExporterPlugin()
	{
		this.exportAction = new RunnableAction( EXPORT_MAMUT, this::export );
	}

	@Override
	public void setAppPluginModel( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		final Map< String, String > menuTexts = new HashMap<>();
		menuTexts.put( EXPORT_MAMUT, "Export to MaMuT file" );
		return menuTexts;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins", menu( "Exports", item( EXPORT_MAMUT ) ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportAction, EXPORT_MAMUT_KEYS );
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
					EXPORT_MAMUT,
					EXPORT_MAMUT_KEYS,
					"Export the current data to a MaMuT file." );
		}
	}

	private void export()
	{
		final Component parent = null;
		final String selectedFile = projectModel.getProjectName() + "-mamut.xml";
		final String dialogTitle = "Export to a MaMuT file";
		final DialogType dialogType = DialogType.SAVE;
		final Image image = MastodonIcons.MAMUT_EXPORT_ICON_LARGE.getImage();
		final File file = FileChooser.chooseFile( parent, selectedFile, new XmlFileFilter(), dialogTitle, dialogType, image );
		if ( file == null )
			return;

		try
		{
			MamutExporter.export( file, projectModel.getModel(), projectModel.getProject() );
		}
		catch ( final IOException e )
		{
			final Object message = "Error export to MaMuT:\n" + e.getMessage();
			final String title = "Error exporting to MaMuT";
			JOptionPane.showMessageDialog( null, message, title,
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}
}
