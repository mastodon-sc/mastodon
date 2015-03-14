package net.trackmate.graph.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.DFI;

public class Tree
{

	private static final char V_BAR_CHAR = '│';

	private static final char CORNER_BR_CHAR = '┌';

	private static final char CORNER_BL_CHAR = '┐';

	private static final char TRIANGLE_B_CHAR = '┬';

	private static final char TRIANGLE_U_CHAR = '┴';

	private static final char SPACE_CHAR = ' ';

	private static final char H_BAR_CHAR = '─';

	public static boolean isTree( final Iterator< Vertex< ? >> it )
	{
		while ( it.hasNext() )
		{
			final Vertex< ? > v = it.next();
			final int vin = v.incomingEdges().size();
			if ( vin > 1 ) { return false; }
		}
		return true;
	}

	public static < K extends Vertex< ? >> Collection< K > findRoot( final Iterator< K > it )
	{
		final List< K > roots = new ArrayList< K >();
		while ( it.hasNext() )
		{
			final K v = it.next();
			if ( v.incomingEdges().size() == 0 )
			{
				roots.add( v );
			}
		}
		return roots;
	}

	public static final boolean isLeaf( final Vertex< ? > v )
	{
		return v.outgoingEdges().size() == 0;
	}

	public static final String toString( final Vertex< ? > root, final Graph< ?, ? > graph )
	{
		final Function< Vertex< ? >, Integer > fun = new Function< Vertex< ? >, Integer >()
		{
			@Override
			public Integer eval( final Vertex< ? > input )
			{
				return Integer.valueOf( input.toString().length() + 2 );
			}
		};

		final Map< Vertex< ? >, Integer > widthMap = recursiveCumSum( root, fun );
		final Map< Vertex< ? >, Integer > depthMap = depth( root );

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

		final Map< Vertex< ? >, Integer > writeTo = new HashMap< Vertex< ? >, Integer >( widthMap.size() );
		@SuppressWarnings( { "unchecked", "rawtypes" } )
		final DFI it = new DFI( root, graph );
		while ( it.hasNext() )
		{
			final Vertex< ? > vi = it.next();
			final int row = depthMap.get( vi ).intValue();
			final int width = widthMap.get( vi ).intValue();

			writeTo.put( vi, Integer.valueOf( columns[ row ] ) );
			columns[ row ] += width;

			final StringBuffer sb = new StringBuffer( width );
			sb.append( spaces( width ) );
			final String s = vi.toString();
			final int sl = s.length();
			final int start = width / 2 - sl / 2;
			final int end = start + sl;
			sb.replace( start, end, s );
			str[ row ].append( sb );

			final StringBuffer sb1 = new StringBuffer( width );
			sb1.append( spaces( width ) );
			sb1.setCharAt( width / 2, V_BAR_CHAR );
			above1[ row ].append( sb1 );

			final StringBuffer sb2 = new StringBuffer( width );
			sb2.append( spaces( width ) );
			char c;
			if ( !vi.incomingEdges().isEmpty() && vi.incomingEdges().get( 0 ).getSource().outgoingEdges().size() > 1 )
			{
				if ( vi.incomingEdges().get( 0 ).getSource().outgoingEdges().get( 0 ).equals( vi.incomingEdges().get( 0 ) ) )
				{
					c = CORNER_BL_CHAR;
				}
				else if ( vi.incomingEdges().get( 0 ).getSource().outgoingEdges().get( vi.incomingEdges().get( 0 ).getSource().outgoingEdges().size() - 1 ).equals( vi.incomingEdges().get( 0 ) ) )
				{
					c = CORNER_BR_CHAR;
				}
				else
				{
					c = TRIANGLE_B_CHAR;
				}

			}
			else
			{
				c = V_BAR_CHAR;
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

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		final DFI it2 = new DFI( root, graph );
		while ( it2.hasNext() )
		{
			final Vertex< ? > vi = it2.next();
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
				c = TRIANGLE_U_CHAR;
			}
			else if ( vi.outgoingEdges().size() > 0 )
			{
				c = V_BAR_CHAR;
			}
			else
			{
				c = SPACE_CHAR;
			}
			above2[ row + 1 ].setCharAt( col + width / 2, c );

			int fi = -1;
			for ( int i = col + 1; i < col + width; i++ )
			{
				final char d = above2[ row + 1 ].charAt( i );
				if ( d == V_BAR_CHAR || d == TRIANGLE_B_CHAR || d == CORNER_BL_CHAR || d == CORNER_BR_CHAR )
				{
					fi = i;
					break;
				}
			}
			if ( fi < 0 )
			{
				continue;
			}

			int li = -1;
			for ( int i = col + width - 1; i >= col + 1; i-- )
			{
				final char d = above2[ row + 1 ].charAt( i );
				if ( d == V_BAR_CHAR || d == TRIANGLE_B_CHAR || d == CORNER_BL_CHAR || d == CORNER_BR_CHAR )
				{
					li = i;
					break;
				}
			}
			if ( li < 0 )
			{
				continue;
			}


			for ( int i = fi; i < li; i++ )
			{
				if ( above2[ row + 1 ].charAt( i ) == SPACE_CHAR )
				{
					above2[ row + 1 ].setCharAt( i, H_BAR_CHAR );
				}
			}

		}

		// Cat
		final StringBuffer text = new StringBuffer();
		text.append( str[ 0 ] );
		for ( int i = 1; i < above2.length; i++ )
		{
			text.append( '\n' );
			text.append( above2[ i ] );
			text.append( '\n' );
			text.append( above1[ i ] );
			text.append( '\n' );
			text.append( str[ i ] );
		}

		return text.toString();
	}

	private static final Map< Vertex< ? >, Integer > recursiveCumSum( final Vertex< ? > root, final Function< Vertex< ? >, Integer > fun )
	{
		final Map< Vertex< ? >, Integer > sumMap = new HashMap< Vertex< ? >, Integer >();
		recurse( root, sumMap, fun );
		return sumMap;
	}

	private static final void recurse( final Vertex< ? > vertex, final Map< Vertex< ? >, Integer > map, final Function< Vertex< ? >, Integer > fun )
	{
		final Edges< ? > oEdges = vertex.outgoingEdges();
		if ( oEdges.isEmpty() )
		{
			final Integer val = fun.eval( vertex );
			map.put( vertex, val );
			return;
		}

		int sum = 0;
		for ( final Edge< ? > edge : oEdges )
		{
			final Vertex< ? > v = edge.getTarget();
			recurse( v, map, fun );
			sum += map.get( v ).intValue();
		}

		sum = Math.max( sum, fun.eval( vertex ).intValue() );
		map.put( vertex, Integer.valueOf( sum ) );
	}

	private static final Map< Vertex< ? >, Integer > depth( final Vertex< ? > root )
	{
		final Map< Vertex< ? >, Integer > depthMap = new HashMap< Vertex< ? >, Integer >();
		depthMap.put( root, Integer.valueOf( 0 ) );

		recurseDepth( root, depthMap );
		return depthMap;
	}

	private static final void recurseDepth( final Vertex< ? > v, final Map< Vertex< ? >, Integer > depthMap )
	{
		final Edges< ? > oEdges = v.outgoingEdges();
		final Integer val = Integer.valueOf( depthMap.get( v ) + 1 );
		for ( final Edge< ? > edge : oEdges )
		{
			final Vertex< ? > target = edge.getTarget();
			depthMap.put( target, val );
			recurseDepth( target, depthMap );
		}
	}

	private static final char[] spaces( final int n )
	{
		final char[] spaces = new char[ n ];
		Arrays.fill( spaces, ' ' );
		return spaces;
	}

	private interface Function< I, O >
	{
		public O eval( I input );
	}

}
