package net.trackmate.graph.object;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



public class ObjectGraphUtils
{

	public interface Function< I, O >
	{
		public O eval( I input );
	}

	public static final < K, L > Map< ObjectVertex< K >, ObjectVertex< L >> map( final ObjectGraph< K > source, final ObjectGraph< L > target, final Function< K, L > fun )
	{
		final Collection< ObjectVertex< K >> sVertices = source.getVertices();
		final Map< ObjectVertex< K >, ObjectVertex< L > > map = new HashMap< ObjectVertex< K >, ObjectVertex< L > >( sVertices.size() );

		for ( final ObjectVertex< K > vk : sVertices )
		{
			final L l = fun.eval( vk.getContent() );
			final ObjectVertex< L > vl = target.addVertex().init( l );
			map.put( vk, vl );
		}

		final Collection< ObjectEdge< K >> sEdges = source.getEdges();
		for ( final ObjectEdge< K > ek : sEdges )
		{
			final ObjectVertex< L > ls = map.get( ek.getSource() );
			final ObjectVertex< L > lt = map.get( ek.getTarget() );
			target.addEdge( ls, lt );
		}

		return map;
	}
	
	public static final < K > Map< ObjectVertex< K >, Integer > depth( final ObjectVertex< K > root )
	{
		final Map< ObjectVertex< K >, Integer > depthMap = new HashMap< ObjectVertex< K >, Integer >();
		depthMap.put( root, Integer.valueOf( 0 ) );

		recurseDepth( root, depthMap );
		return depthMap;
	}

	private static final < K > void recurseDepth( final ObjectVertex< K > v, final Map< ObjectVertex< K >, Integer > depthMap )
	{
		final ObjectEdges< ObjectEdge< K >> oEdges = v.outgoingEdges();
		final Integer val = Integer.valueOf( depthMap.get( v ) + 1 );
		for ( final ObjectEdge< K > edge : oEdges )
		{
			final ObjectVertex< K > target = edge.getTarget();
			depthMap.put( target, val );
			recurseDepth( target, depthMap );
		}
	}

	public static final < K > Map< ObjectVertex< K >, Integer > recursiveCumSum( final ObjectVertex< K > root, final Function< K, Integer > fun )
	{
		final Map< ObjectVertex< K >, Integer > sumMap = new HashMap< ObjectVertex< K >, Integer >();
		recurse( root, sumMap, fun );
		return sumMap;
	}
	
	private static final < K > void recurse( final ObjectVertex< K > vertex, final Map< ObjectVertex< K >, Integer > map, final Function< K, Integer > fun )
	{
		final ObjectEdges< ObjectEdge< K >> oEdges = vertex.outgoingEdges();
		if ( oEdges.isEmpty() )
		{
			final Integer val = fun.eval( vertex.getContent() );
			map.put( vertex, val );
			return;
		}

		int sum = 0;
		for ( final ObjectEdge< K > edge : oEdges )
		{
			final ObjectVertex< K > v = edge.getTarget();
			recurse( v, map, fun );
			sum += map.get( v ).intValue();
		}
		map.put( vertex, Integer.valueOf( sum ) );
	}

}
