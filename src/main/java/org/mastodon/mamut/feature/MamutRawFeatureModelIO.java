/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.io.FeatureSerializationService;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.mamut.feature.branch.BranchFeatureSerializer;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.project.MamutProject.ProjectReader;
import org.mastodon.mamut.project.MamutProject.ProjectWriter;
import org.scijava.Context;

public class MamutRawFeatureModelIO
{

	public static void serialize(
			final Context context,
			final Model model,
			final GraphToFileIdMap< Spot, Link > idmap,
			final ProjectWriter writer )
			throws IOException
	{
		final FeatureModel featureModel = model.getFeatureModel();
		final FeatureSerializationService featureSerializationService =
				context.getService( FeatureSerializationService.class );
		for ( final FeatureSpec< ?, ? > spec : featureModel.getFeatureSpecs() )
		{
			final Feature< ? > rawFeature = featureModel.getFeature( spec );
			final FeatureSerializer< ?, ? > rawSerializer =
					featureSerializationService.getFeatureSerializerFor( rawFeature.getSpec() );
			if ( null == rawSerializer )
				continue;

			final Class< ? > specTargetClass = spec.getTargetClass();
			if ( specTargetClass == Spot.class )
				write( rawFeature, rawSerializer, idmap.vertices(), writer );
			else if ( specTargetClass == Link.class )
				write( rawFeature, rawSerializer, idmap.edges(), writer );
			else if ( specTargetClass == BranchSpot.class )
			{
				if ( !BranchFeatureSerializer.class.isInstance( rawSerializer ) )
				{
					System.err.println( "The branch feature: " + spec.getKey()
							+ " requires a serializer that can work on the branch graph.\n"
							+ "The serializer discovered for this feature was of class: "
							+ rawSerializer.getClass().getName() );
					continue;
				}
				@SuppressWarnings( "rawtypes" )
				final BranchFeatureSerializer branchFeatureSerializer = ( BranchFeatureSerializer ) rawSerializer;
				writeBranchFeature(
						rawFeature,
						branchFeatureSerializer,
						idmap.vertices(),
						model.getBranchGraph(),
						model.getGraph(),
						writer );
			}
			else if ( specTargetClass == BranchLink.class )
			{
				if ( !BranchFeatureSerializer.class.isInstance( rawSerializer ) )
				{
					System.err.println( "The branch feature: " + spec.getKey()
							+ " requires a serializer that can work on the branch graph.\n"
							+ "The serializer discovered for this feature was of class: "
							+ rawSerializer.getClass().getName() );
					continue;
				}
				@SuppressWarnings( "rawtypes" )
				final BranchFeatureSerializer branchFeatureSerializer = ( BranchFeatureSerializer ) rawSerializer;
				writeBranchFeature(
						rawFeature,
						branchFeatureSerializer,
						idmap.edges(),
						model.getBranchGraph(),
						model.getGraph(),
						writer );
			}
			else
				System.err.println( "Do not know how to serialize a feature that targets " + specTargetClass );
		}
	}

	public static void deserialize(
			final Context context,
			final Model model,
			final FileIdToGraphMap< Spot, Link > idmap,
			final ProjectReader reader ) throws ClassNotFoundException, IOException
	{
		final FeatureSerializationService featureSerializationService =
				context.getService( FeatureSerializationService.class );
		final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
		final Collection< String > featureKeys = reader.getFeatureKeys();
		final FeatureModel featureModel = model.getFeatureModel();
		featureModel.pauseListeners();
		featureModel.clear();

		// Regen branch-graph.
		model.getBranchGraph().graphRebuilt();

		for ( final String featureKey : featureKeys )
		{
			final FeatureSpec< ?, ? > spec = featureSpecsService.getSpec( featureKey );
			if ( null == spec )
			{
				System.err.println( "Unknown feature: " + featureKey );
				continue;
			}
			final FeatureSerializer< ?, ? > serializer = featureSerializationService.getFeatureSerializerFor( spec );
			if ( null == serializer )
			{
				System.err.println( "Do not know how to deserialize the feature with key: " + featureKey );
				continue;
			}

			final Class< ? > targetClass = serializer.getFeatureSpec().getTargetClass();
			@SuppressWarnings( "rawtypes" )
			Feature feature;
			if ( targetClass == Spot.class )
				feature = read(
						serializer,
						idmap.vertices(),
						model.getGraph().vertices(),
						reader );
			else if ( targetClass == Link.class )
				feature = read(
						serializer,
						idmap.edges(),
						model.getGraph().edges(),
						reader );
			else if ( targetClass == BranchSpot.class )
			{
				if ( !BranchFeatureSerializer.class.isInstance( serializer ) )
				{
					System.err.println( "The branch feature: " + spec.getKey()
							+ " requires a serializer that can work on the branch graph.\n"
							+ "The serializer discovered for this feature was of class: "
							+ serializer.getClass().getName() );
					continue;
				}
				@SuppressWarnings( "rawtypes" )
				final BranchFeatureSerializer branchFeatureSerializer = ( BranchFeatureSerializer ) serializer;
				feature = readBranchFeature(
						branchFeatureSerializer,
						idmap.vertices(),
						model.getBranchGraph(),
						model.getGraph(),
						reader );
			}
			else if ( targetClass == BranchLink.class )
			{
				if ( !BranchFeatureSerializer.class.isInstance( serializer ) )
				{
					System.err.println( "The branch feature: " + spec.getKey()
							+ " requires a serializer that can work on the branch graph.\n"
							+ "The serializer discovered for this feature was of class: "
							+ serializer.getClass().getName() );
					continue;
				}
				@SuppressWarnings( "rawtypes" )
				final BranchFeatureSerializer branchFeatureSerializer = ( BranchFeatureSerializer ) serializer;
				feature = readBranchFeature(
						branchFeatureSerializer,
						idmap.edges(),
						model.getBranchGraph(),
						model.getGraph(),
						reader );
			}
			else
			{
				System.err.println( "Do not know how to deserialize a feature that targets " + targetClass );
				continue;
			}

			/*
			 * Only declare the feature if it was properly deserialized. A null
			 * value indicates that the feature we are trying to deserialize is
			 * anyway computed on the fly and does not require deserialization.
			 */
			if ( feature != null )
				featureModel.declareFeature( feature );
		}
		featureModel.resumeListeners();
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static void write(
			final Feature< ? > rawFeature,
			final FeatureSerializer< ?, ? > rawSerializer,
			final ObjectToFileIdMap< ? > idmap,
			final ProjectWriter writer ) throws IOException
	{
		final Feature feature = rawFeature;
		final FeatureSerializer serializer = rawSerializer;
		try (
				final OutputStream fos = writer.getFeatureOutputStream( rawFeature.getSpec().getKey() );
				final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) ))
		{
			serializer.serialize( feature, idmap, oos );
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static void writeBranchFeature(
			final Feature< ? > rawFeature,
			final BranchFeatureSerializer< ?, ?, ? > branchFeatureSerializer,
			final ObjectToFileIdMap< ? > idmap,
			final ModelBranchGraph branchGraph,
			final ModelGraph graph,
			final ProjectWriter writer ) throws IOException
	{
		final BranchFeatureSerializer serializer = branchFeatureSerializer;
		try (
				final OutputStream fos = writer.getFeatureOutputStream( rawFeature.getSpec().getKey() );
				final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) ))
		{
			serializer.serialize( rawFeature, idmap, oos, branchGraph, graph );
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static Feature read(
			final FeatureSerializer< ?, ? > rawSerializer,
			final FileIdToObjectMap< ? > idmap,
			final RefCollection< ? > pool,
			final ProjectReader reader ) throws IOException, ClassNotFoundException
	{
		final FeatureSerializer serializer = rawSerializer;
		try (
				final InputStream fis = reader.getFeatureInputStream( serializer.getFeatureSpec().getKey() );
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) ))
		{
			return serializer.deserialize( idmap, pool, ois );
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static Feature readBranchFeature(
			final BranchFeatureSerializer< ?, ?, ? > rawSerializer,
			final FileIdToObjectMap< ? > idmap,
			final ModelBranchGraph branchGraph,
			final ModelGraph graph,
			final ProjectReader reader ) throws IOException, ClassNotFoundException
	{
		final BranchFeatureSerializer serializer = rawSerializer;
		try (
				final InputStream fis = reader.getFeatureInputStream( serializer.getFeatureSpec().getKey() );
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) ))
		{
			return serializer.deserialize( idmap, ois, branchGraph, graph );
		}
	}
}
