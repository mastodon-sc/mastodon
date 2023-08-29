package org.mastodon.mamut.io.importer.graphml;

import java.io.IOException;

import javax.swing.JFrame;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class GraphMLImportDemo
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		try (Context context = new Context())
		{
			final String mastodonProject = "/Users/tinevez/Downloads/data_sharing/pos1_t1-50_cropped_H2B.mastodon";
			final String graphMLFile = "/Users/tinevez/Downloads/data_sharing/graph.graphml";
			
			final ProjectModel pm = ProjectLoader.open( mastodonProject, context );
			GraphMLImporter.importGraphML( graphMLFile, pm, 0, 2.5 );
			
			final MainWindow mainWindow = new MainWindow( pm );
			mainWindow.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			mainWindow.setVisible( true );
		}
	}
}
