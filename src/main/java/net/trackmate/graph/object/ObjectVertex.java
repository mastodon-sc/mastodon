package net.trackmate.graph.object;

import net.trackmate.graph.Vertex;

public class ObjectVertex< K > implements Vertex< ObjectEdge< K > >
{
	private K content;

	private final ObjectEdges< ObjectEdge< K > > incomingEdges = new ObjectEdges< ObjectEdge< K > >();

	private final ObjectEdges< ObjectEdge< K > > outgoingEdges = new ObjectEdges< ObjectEdge< K > >();

	private final ObjectEdges< ObjectEdge< K > > allEdges = new ObjectEdges< ObjectEdge< K > >();

	ObjectVertex()
	{}

	public K getContent()
	{
		return content;
	}

	public ObjectVertex< K > init( final K content )
	{
		this.content = content;
		return this;
	}

	@Override
	public ObjectEdges< ObjectEdge< K > > incomingEdges()
	{
		return incomingEdges;
	}

	@Override
	public ObjectEdges< ObjectEdge< K > > outgoingEdges()
	{
		return outgoingEdges;
	}

	@Override
	public ObjectEdges< ObjectEdge< K > > edges()
	{
		return allEdges;
	}

	@Override
	public String toString()
	{
		return content.toString();
	}

}
