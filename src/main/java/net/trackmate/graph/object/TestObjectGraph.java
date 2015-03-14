package net.trackmate.graph.object;

import java.util.Collection;

import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

public class TestObjectGraph
{
	public static void main( final String[] args )
	{
		final ObjectGraph< String > graph = createCElegansLineage();

		final Collection< ObjectVertex< String >> roots = Tree.findRoot( graph.getVertices().iterator() );
		for ( final ObjectVertex< String > root : roots )
		{
			System.out.println( Tree.toString( root, graph ) );
		}

		System.out.println();
		System.out.println();

		final TrackSchemeGraph tsg = createTrackMateGraph();
		final Collection< TrackSchemeVertex > tsRoots = Tree.findRoot( tsg.vertices().iterator() );
		for ( final TrackSchemeVertex tsRoot : tsRoots )
		{
			System.out.println( Tree.toString( tsRoot, graph ) );
		}
	}

	public static final TrackSchemeGraph createTrackMateGraph()
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex A = graph.addVertex().init( "A", 0, true );

		final TrackSchemeVertex B = graph.addVertex().init( "B", 1, true );
		graph.addEdge( A, B );

		final TrackSchemeVertex C = graph.addVertex().init( "C", 1, true );
		graph.addEdge( A, C );

		final TrackSchemeVertex E = graph.addVertex().init( "E", 1, true );
		graph.addEdge( A, E );

		final TrackSchemeVertex D = graph.addVertex().init( "D", 2, true );
		graph.addEdge( B, D );

		final TrackSchemeVertex F = graph.addVertex().init( "F", 2, true );
		graph.addEdge( B, F );

		final TrackSchemeVertex G = graph.addVertex().init( "G", 2, true );
		graph.addEdge( C, G );

		return graph;
	}

	public static ObjectGraph< String > createCElegansLineage()
	{
		final ObjectGraph< String > graph = new ObjectGraph< String >();

		// AB lineage

		final ObjectVertex< String > AB = graph.addVertex().init( "AB" );
		final ObjectVertex< String > ABa = graph.addVertex().init( "AB.a" );
		final ObjectVertex< String > ABp = graph.addVertex().init( "AB.p" );
		graph.addEdge( AB, ABa );
		graph.addEdge( AB, ABp );

		final ObjectVertex< String > ABal = graph.addVertex().init( "AB.al" );
		final ObjectVertex< String > ABar = graph.addVertex().init( "AB.ar" );
		graph.addEdge( ABa, ABal );
		graph.addEdge( ABa, ABar );

		final ObjectVertex< String > ABala = graph.addVertex().init( "AB.ala" );
		final ObjectVertex< String > ABalp = graph.addVertex().init( "AB.alp" );
		graph.addEdge( ABal, ABala );
		graph.addEdge( ABal, ABalp );

		final ObjectVertex< String > ABara = graph.addVertex().init( "AB.ara" );
		final ObjectVertex< String > ABarp = graph.addVertex().init( "AB.arp" );
		graph.addEdge( ABar, ABara );
		graph.addEdge( ABar, ABarp );

		final ObjectVertex< String > ABpl = graph.addVertex().init( "AB.pl" );
		final ObjectVertex< String > ABpr = graph.addVertex().init( "AB.pr" );
		graph.addEdge( ABp, ABpl );
		graph.addEdge( ABp, ABpr );

		final ObjectVertex< String > ABpla = graph.addVertex().init( "AB.pla" );
		final ObjectVertex< String > ABplp = graph.addVertex().init( "AB.plp" );
		graph.addEdge( ABpl, ABpla );
		graph.addEdge( ABpl, ABplp );

		final ObjectVertex< String > ABpra = graph.addVertex().init( "AB.pra" );
		final ObjectVertex< String > ABprp = graph.addVertex().init( "AB.prp" );
		graph.addEdge( ABpr, ABpra );
		graph.addEdge( ABpr, ABprp );

		// P1 lineage

		final ObjectVertex< String > P1 = graph.addVertex().init( "P1" );
		final ObjectVertex< String > P2 = graph.addVertex().init( "P2" );
		final ObjectVertex< String > EMS = graph.addVertex().init( "EMS" );
		graph.addEdge( P1, P2 );
		graph.addEdge( P1, EMS );

		final ObjectVertex< String > P3 = graph.addVertex().init( "P3" );
		graph.addEdge( P2, P3 );

		final ObjectVertex< String > C = graph.addVertex().init( "C" );
		graph.addEdge( P2, C );

		// C

		final ObjectVertex< String > Ca = graph.addVertex().init( "C.a" );
		final ObjectVertex< String > Cp = graph.addVertex().init( "C.p" );
		graph.addEdge( C, Ca );
		graph.addEdge( C, Cp );

		final ObjectVertex< String > Caa = graph.addVertex().init( "C.aa" );
		final ObjectVertex< String > Cap = graph.addVertex().init( "C.ap" );
		graph.addEdge( Ca, Caa );
		graph.addEdge( Ca, Cap );

		final ObjectVertex< String > Cpa = graph.addVertex().init( "C.pa" );
		final ObjectVertex< String > Cpp = graph.addVertex().init( "C.pp" );
		graph.addEdge( Cp, Cpa );
		graph.addEdge( Cp, Cpp );

		// E

		final ObjectVertex< String > E = graph.addVertex().init( "E" );
		graph.addEdge( EMS, E );

		final ObjectVertex< String > Ea = graph.addVertex().init( "E.a" );
		final ObjectVertex< String > Ep = graph.addVertex().init( "E.p" );
		graph.addEdge( E, Ea );
		graph.addEdge( E, Ep );

		final ObjectVertex< String > Eal = graph.addVertex().init( "E.al" );
		final ObjectVertex< String > Ear = graph.addVertex().init( "E.ar" );
		graph.addEdge( Ea, Eal );
		graph.addEdge( Ea, Ear );

		final ObjectVertex< String > Epl = graph.addVertex().init( "E.pl" );
		final ObjectVertex< String > Epr = graph.addVertex().init( "E.pr" );
		graph.addEdge( Ep, Epl );
		graph.addEdge( Ep, Epr );

		// MS

		final ObjectVertex< String > MS = graph.addVertex().init( "MS" );
		graph.addEdge( EMS, MS );
		final ObjectVertex< String > MSa = graph.addVertex().init( "MS.a" );
		final ObjectVertex< String > MSp = graph.addVertex().init( "MS.p" );
		graph.addEdge( MS, MSa );
		graph.addEdge( MS, MSp );

		// P3


		final ObjectVertex< String > D = graph.addVertex().init( "D" );
		graph.addEdge( P3, D );
		final ObjectVertex< String > P4 = graph.addVertex().init( "P4" );
		graph.addEdge( P3, P4 );

		final ObjectVertex< String > Z2 = graph.addVertex().init( "Z2" );
		final ObjectVertex< String > Z3 = graph.addVertex().init( "Z3" );
		graph.addEdge( P4, Z2 );
		graph.addEdge( P4, Z3 );

		// Zygote

		final ObjectVertex< String > zygote = graph.addVertex().init( "Zygote" );
		graph.addEdge( zygote, AB );
		graph.addEdge( zygote, P1 );

		// Extras, to get a long branch

		final ObjectVertex< String > ABplaa = graph.addVertex().init( "AB.plaa" );
		graph.addEdge( ABpla, ABplaa );

		final ObjectVertex< String > ABplaaa = graph.addVertex().init( "AB.plaaa" );
		graph.addEdge( ABplaa, ABplaaa );

		final ObjectVertex< String > ABplaaaa = graph.addVertex().init( "AB.plaaaa" );
		graph.addEdge( ABplaaa, ABplaaaa );

		return graph;

	}
}
