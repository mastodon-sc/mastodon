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

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

/**
 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph} and {@link ModelBranchGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 *    Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                      │
 *                      │
 *   Spot( 1, X=2,00, Y=4,00, Z=6,00, tp=1 )
 *                      │
 *                      │
 *   Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                      │
 *                      │
 *  Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                      │
 *                      │
 *  Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=3 )
 * </pre>
 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
 *
 *     branchSpotA
 *
 */
public class ExampleGraph1 extends AbstractExampleGraph
{
	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final BranchSpot branchSpotA;

	public ExampleGraph1()
	{
		super();
		spot0 = addNode( "0", 0, new double[] { 1d, 2d, 3d } );
		spot1 = addNode( "1", 1, new double[] { 2d, 4d, 6d } );
		spot2 = addNode( "2", 2, new double[] { 3d, 6d, 9d } );
		spot3 = addNode( "3", 3, new double[] { 4d, 8d, 12d } );
		spot4 = addNode( "4", 3, new double[] { 5d, 10d, 15d } );

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );
		addEdge( spot2, spot3 );
		addEdge( spot3, spot4 );

		branchSpotA = getBranchSpot( spot0 );
	}
}
