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

	public final BranchSpot branchSpotA;

	public ExampleGraph1()
	{
		super();
		Spot spot0 = addNode( "0", 0, new double[] { 1d, 2d, 3d } );
		Spot spot1 = addNode( "1", 1, new double[] { 2d, 4d, 6d } );
		Spot spot2 = addNode( "2", 2, new double[] { 3d, 6d, 9d } );
		Spot spot3 = addNode( "3", 3, new double[] { 4d, 8d, 12d } );
		Spot spot4 = addNode( "4", 3, new double[] { 5d, 10d, 15d } );

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );
		addEdge( spot2, spot3 );
		addEdge( spot3, spot4 );

		branchSpotA = getBranchSpot( spot0 );
	}
}
