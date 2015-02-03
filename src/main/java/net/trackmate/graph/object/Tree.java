package net.trackmate.graph.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.trackmate.graph.Vertex;

public class Tree
{

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


}
