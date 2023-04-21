package org.mastodon.mamut.feature;

import org.mastodon.feature.Feature;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FeatureSerializerTestUtils
{

	public static Feature< ? > saveAndReload( Context context, Model model, Feature< ? > feature ) throws IOException
	{
		File projectRoot = Files.createTempDirectory( "mamut" ).toFile();
		File datasetXmlFile = Files.createTempFile( "project", ".xml" ).toFile();
		MamutProject saveProject = new MamutProject( projectRoot, datasetXmlFile );

		// save the model to temporary files
		try (MamutProject.ProjectWriter writer = saveProject.openForWriting())
		{
			model.getFeatureModel().declareFeature( feature );
			RawGraphIO.GraphToFileIdMap< Spot, Link > graphToFileIdMap;
			graphToFileIdMap = model.saveRaw( writer );
			if ( graphToFileIdMap == null )
				throw new RuntimeException( "Graph to FileId map was not generated during saving model" );
			MamutRawFeatureModelIO.serialize( context, model, graphToFileIdMap, writer );
		}

		MamutProject reloadProject;
		reloadProject = new MamutProject( projectRoot, datasetXmlFile );
		Model modelReloaded = new Model();

		// Add all vertices of the given model to the reloaded model.
		model.getGraph().vertices().forEach( spot -> modelReloaded.getGraph().addVertex( spot ) );

		// reload the model from temporary files
		try (MamutProject.ProjectReader reader = reloadProject.openForReading())
		{
			RawGraphIO.FileIdToGraphMap< Spot, Link > fileIdToGraphMap;
			fileIdToGraphMap = modelReloaded.loadRaw( reader );
			if ( fileIdToGraphMap == null )
				throw new RuntimeException( "FileId to Graph map was not generated during loading model" );
			try
			{
				MamutRawFeatureModelIO.deserialize( context, modelReloaded, fileIdToGraphMap, reader );
			}
			catch ( ClassNotFoundException e )
			{
				throw new RuntimeException( "Could not find feature class. Message: " + e.getMessage() );
			}
		}
		return modelReloaded.getFeatureModel().getFeature( feature.getSpec() );
	}
}
