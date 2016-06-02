package net.trackmate.graph.object;

import java.util.HashSet;

public class ObjectGraph< K > extends AbstractObjectGraph< ObjectVertex< K >, ObjectEdge< K > >
{
	public ObjectGraph()
	{
		super( new Factory<>(), new HashSet<>(), new HashSet<>() );
	}

	private static class Factory< K > implements AbstractObjectGraph.Factory< ObjectVertex< K >, ObjectEdge< K > >
	{
		@Override
		public ObjectVertex< K > createVertex()
		{
			return new ObjectVertex< K >();
		}

		@Override
		public ObjectEdge< K > createEdge( final ObjectVertex< K > source, final ObjectVertex< K > target )
		{
			return new ObjectEdge< K >( source, target );
		}
	}
}
