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
package org.mastodon.mamut.feature.branch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Feature;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Specialized {@link FeatureSerializer} for the branch features we have in the
 * Mamut app.
 * <p>
 * The de/serialization process is a bit complex in the case of branch features.
 * We serialize only the core model, not the branch graph. So we get access to a
 * map the maps the core graph object (spots ot links) to file ID. But the
 * features are defined over a branch spot or a branch link. We we need this
 * interface that invalidates the mother interface (calling the non-specialized
 * methods will trigger a {@link IllegalArgumentException}) and can deal with
 * the issue.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <F>
 *            the type of feature de/serialized by this serializer.
 * @param <O>
 *            the type of object the feature is defined for.
 * @param <Q>
 *            the type of object the map object -&gt; file ID is defined for.
 */
public interface BranchFeatureSerializer< F extends Feature< O >, O, Q > extends FeatureSerializer< F, O >
{

	public F deserialize(
			FileIdToObjectMap< Q > idmap,
			ObjectInputStream ois,
			ModelBranchGraph branchGraph,
			ModelGraph graph ) throws ClassNotFoundException, IOException;

	public void serialize(
			final F feature,
			final ObjectToFileIdMap< Q > idmap,
			final ObjectOutputStream oos,
			ModelBranchGraph branchGraph,
			final ModelGraph graph ) throws IOException;

	/*
	 * Deserialization utils.
	 */

	public static IntPropertyMap< BranchLink > mapToBranchLinkMap( final IntPropertyMap< Link > map,
			final ModelBranchGraph branchGraph )
	{
		final IntPropertyMap< BranchLink > bmap = new IntPropertyMap<>( branchGraph.edges(), -1 );
		final BranchLink beref = branchGraph.edgeRef();
		try
		{
			for ( final Link l : map.getMap().keySet() )
			{
				final BranchLink bl = branchGraph.getBranchEdge( l, beref );
				bmap.set( bl, map.get( l ) );
			}
		}
		finally
		{
			branchGraph.releaseRef( beref );
		}
		return bmap;
	}

	public static DoublePropertyMap< BranchLink > mapToBranchLinkMap( final DoublePropertyMap< Link > map,
			final ModelBranchGraph branchGraph )
	{
		final DoublePropertyMap< BranchLink > bmap = new DoublePropertyMap<>( branchGraph.edges(), Double.NaN );
		final BranchLink beref = branchGraph.edgeRef();
		try
		{
			for ( final Link l : map.getMap().keySet() )
			{
				final BranchLink bl = branchGraph.getBranchEdge( l, beref );
				bmap.set( bl, map.get( l ) );
			}
		}
		finally
		{
			branchGraph.releaseRef( beref );
		}
		return bmap;
	}

	public static IntPropertyMap< BranchSpot > mapToBranchSpotMap( final IntPropertyMap< Spot > map,
			final ModelBranchGraph branchGraph )
	{
		final IntPropertyMap< BranchSpot > bmap = new IntPropertyMap<>( branchGraph.vertices(), -1 );
		final BranchSpot beref = branchGraph.vertexRef();
		try
		{
			for ( final Spot l : map.getMap().keySet() )
			{
				final BranchSpot bl = branchGraph.getBranchVertex( l, beref );
				bmap.set( bl, map.get( l ) );
			}
		}
		finally
		{
			branchGraph.releaseRef( beref );
		}
		return bmap;
	}

	public static DoublePropertyMap< BranchSpot > mapToBranchSpotMap( final DoublePropertyMap< Spot > map,
			final ModelBranchGraph branchGraph )
	{
		final DoublePropertyMap< BranchSpot > bmap = new DoublePropertyMap<>( branchGraph.vertices(), Double.NaN );
		final BranchSpot beref = branchGraph.vertexRef();
		try
		{
			for ( final Spot l : map.getMap().keySet() )
			{
				final BranchSpot bl = branchGraph.getBranchVertex( l, beref );
				bmap.set( bl, map.get( l ) );
			}
		}
		finally
		{
			branchGraph.releaseRef( beref );
		}
		return bmap;
	}

	/*
	 * Serialization utils.
	 */

	public static IntPropertyMap< Link > branchLinkMapToMap( final IntPropertyMap< BranchLink > map,
			final ModelBranchGraph branchGraph, final ModelGraph graph )
	{
		final IntPropertyMap< Link > lmap = new IntPropertyMap<>( graph.edges(), -1 );
		final Link eref = graph.edgeRef();
		try
		{
			for ( final BranchLink bl : map.getMap().keySet() )
			{
				final Link l = branchGraph.getLinkedEdge( bl, eref );
				lmap.set( l, map.get( bl ) );
			}
		}
		finally
		{
			graph.releaseRef( eref );
		}
		return lmap;
	}

	public static DoublePropertyMap< Link > branchLinkMapToMap( final DoublePropertyMap< BranchLink > map,
			final ModelBranchGraph branchGraph, final ModelGraph graph )
	{
		final DoublePropertyMap< Link > lmap = new DoublePropertyMap<>( graph.edges(), Double.NaN );
		final Link eref = graph.edgeRef();
		try
		{
			for ( final BranchLink bl : map.getMap().keySet() )
			{
				final Link l = branchGraph.getLinkedEdge( bl, eref );
				lmap.set( l, map.get( bl ) );
			}
		}
		finally
		{
			graph.releaseRef( eref );
		}
		return lmap;
	}

	public static IntPropertyMap< Spot > branchSpotMapToMap( final IntPropertyMap< BranchSpot > map,
			final ModelBranchGraph branchGraph, final ModelGraph graph )
	{
		final IntPropertyMap< Spot > lmap = new IntPropertyMap<>( graph.vertices(), -1 );
		final Spot eref = graph.vertexRef();
		try
		{
			for ( final BranchSpot bl : map.getMap().keySet() )
			{
				final Spot l = branchGraph.getLastLinkedVertex( bl, eref );
				lmap.set( l, map.get( bl ) );
			}
		}
		finally
		{
			graph.releaseRef( eref );
		}
		return lmap;
	}

	public static DoublePropertyMap< Spot > branchSpotMapToMap( final DoublePropertyMap< BranchSpot > map,
			final ModelBranchGraph branchGraph, final ModelGraph graph )
	{
		final DoublePropertyMap< Spot > lmap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
		final Spot eref = graph.vertexRef();
		try
		{
			for ( final BranchSpot bl : map.getMap().keySet() )
			{
				final Spot l = branchGraph.getLastLinkedVertex( bl, eref );
				lmap.set( l, map.get( bl ) );
			}
		}
		finally
		{
			graph.releaseRef( eref );
		}
		return lmap;
	}

	/*
	 * Invalidated overriden methods.
	 */

	// FIXME Imperfect. Use another interface?
	@Override
	default void serialize( final F feature, final ObjectToFileIdMap< O > idmap, final ObjectOutputStream oos )
			throws IOException
	{
		throw new IllegalArgumentException( "Branch features cannot be serialized by this method call." );
	}

	@Override
	default F deserialize( final FileIdToObjectMap< O > idmap, final RefCollection< O > pool,
			final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		throw new IllegalArgumentException( "Branch features cannot be deserialized by this method call." );
	}

}
