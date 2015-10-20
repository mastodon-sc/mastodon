package net.trackmate.graph.algorithm;

import java.util.Arrays;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefObjectMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.traversal.DepthFirstIterator;

/**
 * An algorithm that can output the tree below a specified vertex.
 * <p>
 * It only works for graph that are trees, that is, graphs without loops and
 * where all vertices have at most one predecessor. If this class is provided
 * with a graph that is not a tree, vertices that are not accessible by
 * descending from the specified root will be plainly ignored.
 *
 * @author Jean-Yves Tinevez
 */
public class TreeOutputter< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{

	private static final char V_BAR_CHAR = '│';

	private static final char CORNER_BR_CHAR = '┌';

	private static final char CORNER_BL_CHAR = '┐';

	private static final char TRIANGLE_B_CHAR = '┬';

	private static final char TRIANGLE_U_CHAR = '┴';

	private static final char SPACE_CHAR = ' ';

	private static final char H_BAR_CHAR = '─';

	private RefSet< V > visited;

	public TreeOutputter( final ReadOnlyGraph< V, E > graph )
	{
		super( graph );
	}

	/**
	 * Returns the string representation of the tree of children below the
	 * specified vertex.
	 * <p>
	 * This method works faithfully only for trees, that is: for graphs without
	 * loops and where all vertices have at most one parent. If this class is
	 * provided with a graph that do not fulfill these conditions, the returned
	 * representation will ignore loops and multiple incoming edges.
	 *
	 * @param root
	 *            the vertex to start the tree representation with.
	 * @return a String representation of the tree.
	 */
	public String get( final V root )
	{
		visited = createVertexSet();
		final RefObjectMap< V, Integer > widthMap = recursiveCumSum( root, fun );
		final RefObjectMap< V, Integer > depthMap = depth( root );

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

		final RefObjectMap< V, Integer > writeTo = createVertexObjectMap( Integer.class );
		final Iterator< V > it = new DepthFirstIterator< V, E >( root, graph );
		visited.clear();
		while ( it.hasNext() )
		{
			final V vi = it.next();
			visited.add( vi );

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

			/*
			 * Determine if this vertex is a leaf, or if it has children that
			 * already have been visited. In any of these 2 cases, we need to
			 * create space below what will be a childless node (on the
			 * representation).
			 */
			boolean doSpace = isLeaf( vi );
			if ( !doSpace )
			{
				final V tmp = vertexRef();
				for ( final Edge< V > edge : vi.outgoingEdges() )
				{
					final V target = edge.getTarget( tmp );
					if ( visited.contains( target ) )
					{
						doSpace = true;
						break;
					}
				}
			}

			if ( doSpace )
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

		final Iterator< V > it2 = new DepthFirstIterator< V, E >( root, graph );
		while ( it2.hasNext() )
		{
			final V vi = it2.next();
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

	private boolean isLeaf( final V vi )
	{
		return vi.outgoingEdges().isEmpty();
	}

	private RefObjectMap< V, Integer > recursiveCumSum( final V root, final Function< V, Integer > fun )
	{
		final RefObjectMap< V, Integer > sumMap = createVertexObjectMap( Integer.class );
		recurse( root, sumMap, fun );
		return sumMap;
	}

	private boolean recurse( final V vertex, final RefObjectMap< V, Integer > map, final Function< V, Integer > fun )
	{
		if ( visited.contains( vertex ) ) { return false; }
		visited.add( vertex );
		final Edges< E > oEdges = vertex.outgoingEdges();
		if ( oEdges.isEmpty() )
		{
			final Integer val = fun.eval( vertex );
			map.put( vertex, val );
			return true;
		}

		int sum = 0;
		final V tmp = vertexRef();
		for ( final E edge : oEdges )
		{
			final V v = edge.getTarget( tmp );
			if ( recurse( v, map, fun ) )
			{
				sum += map.get( v ).intValue();
			}
		}

		sum = Math.max( sum, fun.eval( vertex ).intValue() );
		map.put( vertex, Integer.valueOf( sum ) );
		return true;
	}

	private RefObjectMap< V, Integer > depth( final V root )
	{
		final RefObjectMap< V, Integer > depthMap = createVertexObjectMap( Integer.class );
		depthMap.put( root, Integer.valueOf( 0 ) );
		recurseDepth( root, depthMap );
		return depthMap;
	}

	private void recurseDepth( final V v, final RefObjectMap< V, Integer > depthMap )
	{
		final Edges< E > oEdges = v.outgoingEdges();
		final Integer val = Integer.valueOf( depthMap.get( v ) + 1 );
		final V tmp = vertexRef();
		for ( final E edge : oEdges )
		{
			final V target = edge.getTarget( tmp );
			if ( !depthMap.containsKey( target ) )
			{
				depthMap.put( target, val );
				recurseDepth( target, depthMap );
			}
		}
	}

	private static final char[] spaces( final int n )
	{
		final char[] spaces = new char[ n ];
		Arrays.fill( spaces, ' ' );
		return spaces;
	}

	private static interface Function< I, O >
	{
		public O eval( I input );
	}

	private final Function< V, Integer > fun = new Function< V, Integer >()
	{
		@Override
		public Integer eval( final V input )
		{
			return Integer.valueOf( input.toString().length() + 2 );
		}
	};

	public static < V extends Vertex< E >, E extends Edge< V > > String output( final Graph< V, E > graph, final V root )
	{
		return new TreeOutputter< V, E >( graph ).get( root );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > String output( final Graph< V, E > graph )
	{
		final RefSet< V > roots = RootFinder.getRoots( graph );
		if ( roots.isEmpty() ) { return ""; }
		return new TreeOutputter< V, E >( graph ).get( roots.iterator().next() );
	}
}
