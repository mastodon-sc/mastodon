package org.mastodon.mamut.feature;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

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

		MamutProject reloadProject = new MamutProject( projectRoot, datasetXmlFile );
		Model modelReloaded = new Model();

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

	/**
	 * Checks, if the two features have the same spec and the same projection values for a given object.
	 * <p>
	 * The latter is done by comparing the values of the projections
	 * of the two given features having the same key for the given object.
	 *
	 * @param feature1 first feature to compare
	 * @param feature2 second feature to compare
	 * @param object object to compare the projections on
	 * @return {@code true} if the two features have the same spec and the same projection values for the given object,
	 * 		   {@code false} otherwise.
	 * @param <T> the type of the object
	 */
	public static < T > boolean checkFeatureProjectionEquality( Feature< T > feature1, Feature< T > feature2, T object )
	{
		if ( feature1 == null || feature2 == null )
			return false;
		if ( !feature1.getSpec().equals( feature2.getSpec() ) )
			return false;

		Set< FeatureProjection< T > > featureProjections1 = feature1.projections();
		Set< FeatureProjection< T > > featureProjections2 = feature2.projections();
		for ( FeatureProjection< T > featureProjection1 : featureProjections1 )
		{
			for ( FeatureProjection< T > featureProjection2 : featureProjections2 )
			{
				if ( featureProjection1.getKey().equals( featureProjection2.getKey() ) )
				{
					double value1 = featureProjection1.value( object );
					double value2 = featureProjection2.value( object );
					if ( value1 != value2 )
						return false;
				}
			}
		}
		return true;
	}
}
