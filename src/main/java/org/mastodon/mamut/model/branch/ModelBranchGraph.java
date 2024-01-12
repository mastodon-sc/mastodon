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
package org.mastodon.mamut.model.branch;

import org.mastodon.graph.branch.BranchGraphImp;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A branch-graph specific for {@link ModelGraph}, whose vertices implements the
 * {@link RealLocalizable} and {@link HasTimepoint} interfaces, exposing the
 * {@link Spot} they are linked to.
 *
 * @author Jean-Yves Tinevez.
 *
 */
public class ModelBranchGraph
		extends BranchGraphImp< Spot, Link, BranchSpot, BranchLink, BranchSpotPool, BranchLinkPool, ByteMappedElement >
{
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public ModelBranchGraph( final ModelGraph graph )
	{
		super( graph, new BranchLinkPool( 1024, new BranchSpotPool( 1024, graph.vertices().getRefPool() ) ) );
	}

	public ModelBranchGraph( final ModelGraph graph, final int initialCapacity )
	{
		super( graph, new BranchLinkPool( initialCapacity,
				new BranchSpotPool( initialCapacity, graph.vertices().getRefPool() ) ) );
	}

	public ReentrantReadWriteLock getLock()
	{
		return lock;
	}

	@Override
	public BranchSpot init( final BranchSpot branchVertex, final Spot branchStart, final Spot branchEnd )
	{
		return branchVertex.init( branchStart, branchEnd );
	}

	@Override
	public BranchLink init( final BranchLink branchEdge, final Link edge )
	{
		return branchEdge.init();
	}

	@Override
	public void graphRebuilt()
	{
		if ( lock == null )
		{
			// NB: graphRebuilt() is called the first time, even before the lock
			// is initialized. This is because the super class (BranchGraphImp)
			// calls graphRebuilt() in its constructor.
			// But that's not a problem. We don't need to use the lock while the
			// constructor is run, since no other thread can access the
			// branch graph before the constructor finished.
			super.graphRebuilt();
		}
		else
		{
			lock.writeLock().lock();
			try
			{
				super.graphRebuilt();
			}
			finally
			{
				lock.writeLock().unlock();
			}
		}
	}
}
