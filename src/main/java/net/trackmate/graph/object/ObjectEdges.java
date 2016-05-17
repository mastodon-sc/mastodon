package net.trackmate.graph.object;

import java.util.ArrayList;
import java.util.Iterator;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.Edges;

public class ObjectEdges< E extends Edge< ? >> extends ArrayList< E > implements Edges< E >
{

	private static final long serialVersionUID = 1L;

	@Override
	public E get( final int i, final E edge )
	{
		return get( i );
	}

	@Override
	public Iterator< E > safe_iterator()
	{
		return iterator();
	}

}
