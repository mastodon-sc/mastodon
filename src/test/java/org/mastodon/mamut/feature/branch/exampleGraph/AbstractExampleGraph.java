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
package org.mastodon.mamut.feature.branch.exampleGraph;

import javax.annotation.Nonnull;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

public abstract class AbstractExampleGraph
{
	@Nonnull
	protected final Model model;

	@Nonnull
	protected final ModelGraph modelGraph;

	private boolean branchGraphRequiresRebuild;

	@Nonnull
	private final ModelBranchGraph modelBranchGraph;

	public AbstractExampleGraph()
	{
		this.model = new Model();
		this.modelGraph = model.getGraph();
		this.modelBranchGraph = model.getBranchGraph();
		this.branchGraphRequiresRebuild = false;
	}

	@Nonnull
	public Model getModel()
	{
		rebuiltGraphIfRequired();
		return model;
	}

	public BranchSpot getBranchSpot(@Nonnull Spot spot)
	{
		rebuiltGraphIfRequired();
		return modelBranchGraph.getBranchVertex( spot, modelBranchGraph.vertexRef() );
	}

	public BranchLink getBranchLink( @Nonnull Link link )
	{
		rebuiltGraphIfRequired();
		return modelBranchGraph.getBranchEdge( link, modelBranchGraph.edgeRef() );
	}

	private void rebuiltGraphIfRequired()
	{
		if ( ! branchGraphRequiresRebuild )
			return;
		this.model.getBranchGraph().graphRebuilt();
		branchGraphRequiresRebuild = false;
	}

	protected Spot addNode( @Nonnull String label, int timepoint, double[] xyz )
	{
		Spot spot = modelGraph.addVertex();
		spot.init( timepoint, xyz, 0 );
		spot.setLabel( label );
		branchGraphRequiresRebuild = true;
		return spot;
	}

	protected Link addEdge( @Nonnull Spot source, @Nonnull Spot target )
	{
		Link link = modelGraph.addEdge( source, target );
		branchGraphRequiresRebuild = true;
		return link;
	}
}
