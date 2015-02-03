package net.trackmate.graph.object;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.trackmate.graph.object.ObjectGraphUtils.Function;

public class TestObjectGraph
{
	public static void main( final String[] args )
	{
		final ObjectGraph< String > graph = createCElegansLineage();

		final Function< String, Integer > fun = new Function< String, Integer >()
		{
			@Override
			public Integer eval( final String input )
			{
				return Integer.valueOf( input.length() + 2 );
			}
		};

		final Collection< ObjectVertex< String >> roots = Tree.findRoot( graph.getVertices().iterator() );
		for ( final ObjectVertex< String > root : roots )
		{
			final Map< ObjectVertex< String >, Integer > widthMap = ObjectGraphUtils.recursiveCumSum( root, fun );
			final Map< ObjectVertex< String >, Integer > depthMap = ObjectGraphUtils.depth( root );

			/*
			 * Max depth and text holder.
			 */

			int maxDepth = -1;
			for ( final Integer depth : depthMap.values() )
			{
				final int d = depth.intValue();
				if ( d > maxDepth )
				{
					maxDepth = d;
				}
			}
			final StringBuffer[] str = new StringBuffer[ maxDepth + 1 ];
			final StringBuffer[] above1 = new StringBuffer[ maxDepth + 1 ];
			final StringBuffer[] above2 = new StringBuffer[ maxDepth + 1 ];
			final int[] columns = new int[ maxDepth + 1 ];

			for ( int i = 0; i < str.length; i++ )
			{
				str[ i ] = new StringBuffer();
				above1[ i ] = new StringBuffer();
				above2[ i ] = new StringBuffer();
			}

			/*
			 * Iterate into the tree.
			 */

			final Map< ObjectVertex< String >, Integer > writeTo = new HashMap< ObjectVertex< String >, Integer >( widthMap.size() );
			final Iterator< ObjectVertex< String >> it = new DepthFirstIterator< String >( root );
			while ( it.hasNext() )
			{
				final ObjectVertex< String > vi = it.next();
				final int row = depthMap.get( vi ).intValue();
				final int width = widthMap.get( vi ).intValue();
				
				columns[ row ] += width;
				writeTo.put( vi, Integer.valueOf( columns[ row ] ) );

				final StringBuffer sb = new StringBuffer( width );
				sb.append( spaces( width ) );
				final String s = vi.getContent();
				final int sl = s.length();
				final int start = width/2 - sl/2;
				final int end = start + sl;
				sb.replace( start , end , s );
				str[ row ].append( sb );
				
				final StringBuffer sb1 = new StringBuffer( width );
				sb1.append( spaces( width ) );
				sb1.setCharAt( width / 2, '|' );
				above1[ row ].append( sb1 );

				final StringBuffer sb2 = new StringBuffer( width );
				sb2.append( spaces( width ) );
				char c;
				if ( !vi.incomingEdges().isEmpty() && vi.incomingEdges().get( 0 ).getSource().outgoingEdges().size() > 1 )
				{
					c = '+';

				}
				else
				{
					c = '|';
				}
				sb2.setCharAt( width / 2, c );
				above2[ row ].append( sb2 );

				if ( Tree.isLeaf( vi ) )
				{
					for ( int i = row + 1; i <= maxDepth; i++ )
					{
						final char[] spaces = spaces( width );
						str[ i ].append( spaces( width ) );
						above1[ i ].append( spaces );
						above2[ i ].append( spaces );
						columns[ i ] += width;
					}
				}
			}
			
			/*
			 * Second iteration
			 */
			
			final Iterator< ObjectVertex< String >> it2 = new DepthFirstIterator< String >( root );
			while ( it2.hasNext() )
			{
				final ObjectVertex< String > vi = it2.next();
				final int row = depthMap.get( vi ).intValue();
				if ( row == maxDepth )
				{
					continue;
				}
				final int col = writeTo.get( vi ).intValue();
				final int width = widthMap.get( vi ).intValue();

				char c;
				if ( vi.outgoingEdges().size() > 1 )
				{
					c = '+';
				}
				else
				{
					c = ' ';
				}
				above2[ row + 1 ].setCharAt( col - width / 2, c );
			}

			// Cat
			final StringBuffer text = new StringBuffer();
			for ( int i = 0; i < above2.length; i++ )
			{
				text.append( above2[ i ] );
				text.append( '\n' );
				text.append( above1[ i ] );
				text.append( '\n' );
				text.append( str[ i ] );
				text.append( '\n' );
			}

			System.out.println( text );
			System.out.println( '\n' );
		}

	}

	private static final char[] spaces( final int n )
	{
		final char[] spaces = new char[ n ];
		Arrays.fill( spaces, ' ' );
		return spaces;
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

		return graph;
	}
}
