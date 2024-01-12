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
package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Mother class for color generators that return a color based on feature
 * defined for a branch edge 'upward' or 'downward' in time.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertex in the core graph.
 * @param <E>
 *            the type of edge in the core graph.
 * @param <BV>
 *            the type of vertices in he branch graph.
 * @param <BE>
 *            the type of vertices in he branch graph.
 */
public abstract class AbstractBranchEdgeColorGenerator< V extends Vertex< E >, E extends Edge< V >,
		BV extends Vertex< BE >, BE extends Edge< BV > >
{

	protected final FeatureEdgeColorGenerator< BV, BE > colorGenerator;

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	public AbstractBranchEdgeColorGenerator(
			final FeatureProjection< BE > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		this.branchGraph = branchGraph;
		this.colorGenerator = new FeatureEdgeColorGenerator<>( featureProjection, colorMap, min, max );
	}

}
