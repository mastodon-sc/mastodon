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
 *                                                Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 1, X=0,00, Y=0,00, Z=0,00, tp=1 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                       ┌───────────────────────────────────────────┴──────────────────────┐
 *                       │                                                                  │
 *  Spot( 11, X=12,00, Y=24,00, Z=36,00, tp=3 )                         Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                       │                                                                  │
 *                       │                                                                  │
 *                       │                                              Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=4 )
 *                       │                                            ┌─────────────────────┴─────────────────────┐
 *                       │                                            │                                           │
 *  Spot( 13, X=14,00, Y=28,00, Z=42,00, tp=5 )   Spot( 8, X=9,00, Y=18,00, Z=27,00, tp=5 )   Spot( 5, X=6,00, Y=12,00, Z=18,00, tp=5 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                                                    │                       Spot( 6, X=0,00, Y=0,00, Z=0,00, tp=6 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 10, X=11,00, Y=22,00, Z=33,00, tp=7 )  Spot( 7, X=8,00, Y=16,00, Z=24,00, tp=7 )
 * </pre>
 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
 * <pre>
 *                        branchSpotA
 * 	       ┌──────────────┴─────────────────┐
 * 	       │                                │
 * 	   branchSpotC                      branchSpotB
 * 	                                 ┌──────┴───────┐
 * 	                                 │              │
 * 	                             branchSpotE branchSpotD
 * </pre>
 */
public class ExampleGraph2 extends AbstractExampleGraph
{

	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final Spot spot5;

	public final Spot spot6;

	public final Spot spot7;

	public final Spot spot8;

	public final Spot spot10;

	public final Spot spot11;

	public final Spot spot13;

	public final BranchSpot branchSpotA;

	public final BranchSpot branchSpotB;

	public final BranchSpot branchSpotC;

	public final BranchSpot branchSpotD;

	public final BranchSpot branchSpotE;

	public ExampleGraph2()
	{
		spot0 = addNode( "0", 0, new double[] { 1d, 2d, 3d } );
		spot1 = addNode( "1", 1, new double[] { 0d, 0d, 0d } );
		spot2 = addNode( "2", 2, new double[] { 3d, 6d, 9d } );
		spot3 = addNode( "3", 3, new double[] { 4d, 8d, 12d } );
		spot4 = addNode( "4", 4, new double[] { 5d, 10d, 15d } );
		spot5 = addNode( "5", 5, new double[] { 6d, 12d, 18d } );
		spot6 = addNode( "6", 6, new double[] { 0d, 0d, 0d } );
		spot7 = addNode( "7", 7, new double[] { 8d, 16d, 24d } );
		spot8 = addNode( "8", 5, new double[] { 9d, 18d, 27d } );
		spot10 = addNode( "10", 7, new double[] { 11d, 22d, 33d } );
		spot11 = addNode( "11", 3, new double[] { 12d, 24d, 36d } );
		spot13 = addNode( "13", 5, new double[] { 14d, 28d, 42d } );

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );
		addEdge( spot2, spot3 );
		addEdge( spot3, spot4 );
		addEdge( spot4, spot5 );
		addEdge( spot5, spot6 );
		addEdge( spot6, spot7 );
		addEdge( spot4, spot8 );
		addEdge( spot8, spot10 );
		addEdge( spot2, spot11 );
		addEdge( spot11, spot13 );

		branchSpotA = getBranchSpot( spot0 );
		branchSpotB = getBranchSpot( spot4 );
		branchSpotC = getBranchSpot( spot11 );
		branchSpotD = getBranchSpot( spot5 );
		branchSpotE = getBranchSpot( spot8 );
	}
}
