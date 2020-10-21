package org.mastodon.views.dbvv;

import gnu.trove.map.TIntObjectArrayMap;
import org.mastodon.graph.GraphListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class DBvvEntities implements GraphListener< Spot, Link >, DColoredEllipsoidsPerTimepoint
{
	public DBvvEntities( final ModelGraph graph )
	{
		this.graph = graph;
		graph.addGraphListener( this );
		graphRebuilt();
	}

	@Override
	public void graphRebuilt()
	{
		map.clear();

		for ( final Spot v : graph.vertices() )
			forTimepoint( v.getTimepoint() ).addOrUpdate( v );

		// TODO: edges
	}

	@Override
	public void vertexAdded( final Spot vertex )
	{
		forTimepoint( vertex.getTimepoint() ).addOrUpdate( vertex );
	}

	@Override
	public void vertexRemoved( final Spot vertex )
	{
		forTimepoint( vertex.getTimepoint() ).remove( vertex );
	}

	@Override
	public void edgeAdded( final Link edge )
	{
		// TODO: edges
	}

	@Override
	public void edgeRemoved( final Link edge )
	{
		// TODO: edges
	}

	private final ModelGraph graph;

	// Maps timepoint to DColoredEllipsoids
	private final TIntObjectArrayMap< DColoredEllipsoids > map = new TIntObjectArrayMap<>();

	@Override
	public DColoredEllipsoids forTimepoint( final int timepoint )
	{
		DColoredEllipsoids ellipsoids = map.get( timepoint );
		if ( ellipsoids == null )
		{
			ellipsoids = new DColoredEllipsoids( graph );
			map.put( timepoint, ellipsoids );
		}
		return ellipsoids;
	}
}
