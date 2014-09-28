package net.trackmate.graph;

public interface Vertex< E extends Edge< ? > >
{
	public Edges< E > incomingEdges();

	public Edges< E > outgoingEdges();

	public Edges< E > edges();
}
