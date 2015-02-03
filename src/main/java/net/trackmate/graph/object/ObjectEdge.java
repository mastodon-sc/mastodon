package net.trackmate.graph.object;

import net.trackmate.graph.Edge;

public class ObjectEdge< K > implements Edge< ObjectVertex< K > >
{

	private final ObjectVertex< K > source;

	private final ObjectVertex< K > target;

	ObjectEdge( final ObjectVertex< K > source, final ObjectVertex< K > target )
	{
		this.source = source;
		this.target = target;
	}

	@Override
	public ObjectVertex< K > getSource()
	{
		return source;
	}

	@Override
	public ObjectVertex< K > getSource( final ObjectVertex< K > vertex )
	{
		return source;
	}

	@Override
	public ObjectVertex< K > getTarget()
	{
		return target;
	}

	@Override
	public ObjectVertex< K > getTarget( final ObjectVertex< K > vertex )
	{
		return target;
	}

}
