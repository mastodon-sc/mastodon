/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views.trackscheme;

import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.views.trackscheme.wrap.DefaultTrackSchemeProperties;

public class MamutHierarchyTrackSchemeProperties extends DefaultTrackSchemeProperties< BranchSpot, BranchLink >
{

	private final InverseDepthFirstIterator< BranchSpot, BranchLink > it;

	public MamutHierarchyTrackSchemeProperties( final ModelBranchGraph graph )
	{
		this.it = new InverseDepthFirstIterator<>( graph );
	}

	/*
	 * NOT THREAD SAFE! If issues arise when multithreading TS graph creation,
	 * they will be caused here. But for now, there is no concurrent creation of
	 * vertices or editing of vertex properties.
	 */
	@Override
	public int getTimepoint( final BranchSpot v )
	{
		it.reset( v );
		int level = 0;
		while ( it.hasNext() && it.next().incomingEdges().size() > 0 )
			level++;
		return level;
	}

	@Override
	public String getFirstLabel( final BranchSpot v )
	{
		return v.getFirstLabel();
	}

	@Override
	public void addVertexLabelListener( final PropertyChangeListener< BranchSpot > listener )
	{}

	@Override
	public void removeVertexLabelListener( final PropertyChangeListener< BranchSpot > listener )
	{}
}
