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
package org.mastodon.mamut.feature;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import net.imglib2.util.Cast;

public class FeatureSerializerTestUtils
{

	public static < T > Feature< T > saveAndReload( Context context, Model model, Feature< T > feature ) throws IOException
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
			MamutRawFeatureModelIO.serialize( context, model, graphToFileIdMap, writer );
		}

		MamutProject reloadProject = new MamutProject( projectRoot, datasetXmlFile );
		Model modelReloaded = new Model();

		// reload the model from temporary files
		try (MamutProject.ProjectReader reader = reloadProject.openForReading())
		{
			RawGraphIO.FileIdToGraphMap< Spot, Link > fileIdToGraphMap = modelReloaded.loadRaw( reader );
			MamutRawFeatureModelIO.deserialize( context, modelReloaded, fileIdToGraphMap, reader );
		}
		catch ( ClassNotFoundException e )
		{
			throw new RuntimeException( "Could not find feature class.", e );
		}
		return Cast.unchecked( modelReloaded.getFeatureModel().getFeature( feature.getSpec() ) );
	}

	/**
	 * Checks, if the two features have the same spec and the same projection values for the given objects.
	 *
	 * @param feature1 first feature to compare
	 * @param feature2 second feature to compare
	 * @param objects objects to compare the projections on
	 * @return {@code true} if the two features have the same spec and the same projection values for the given objects,
	 * 		   {@code false} otherwise.
	 * @param <T> the type of the objects
	 */
	public static < T > boolean checkFeatureProjectionEquality( Feature< T > feature1, Feature< T > feature2, Collection< T > objects )
	{
		if ( feature1 == null || feature2 == null )
			return false;

		FeatureSpec< ? extends Feature< T >, T > spec1 = feature1.getSpec();
		FeatureSpec< ? extends Feature< T >, T > spec2 = feature2.getSpec();
		if ( !spec1.equals( spec2 ) )
			return false;

		Map< FeatureProjectionKey, FeatureProjection< T > > projections1 = feature1.projections().stream()
				.collect( Collectors.toMap( FeatureProjection::getKey, x -> x ) );

		Map< FeatureProjectionKey, FeatureProjection< T > > projections2 = feature1.projections().stream()
				.collect( Collectors.toMap( FeatureProjection::getKey, x -> x ) );

		if ( !projections1.keySet().equals( projections2.keySet() ) )
			return false;

		for ( FeatureProjectionKey key : projections1.keySet() )
			if ( !checkProjectionEquals( projections1.get( key ), projections2.get( key ), objects ) )
				return false;

		return true;
	}

	private static < T > boolean checkProjectionEquals(
			FeatureProjection< T > projection1,
			FeatureProjection< T > projection2,
			Collection< T > objects )
	{
		for ( T object : objects )
		{
			boolean set1 = projection1.isSet( object );
			boolean set2 = projection2.isSet( object );

			if ( set1 != set2 )
				return false;

			if ( projection1.value( object ) != projection2.value( object ) )
				return false;
		}
		return true;
	}
}
