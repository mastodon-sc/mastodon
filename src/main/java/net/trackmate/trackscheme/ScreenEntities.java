package net.trackmate.trackscheme;

import java.util.List;

public class ScreenEntities
{
	public List< ScreenVertex > vertices;

	public List< ScreenEdge > edges;

	public List< ScreenVertexRange > vertexRanges;

	public ScreenEntities( final List< ScreenVertex > vertices, final List< ScreenEdge > edges, final List<ScreenVertexRange> vertexRanges )
	{
		this.vertices = vertices;
		this.edges = edges;
		this.vertexRanges = vertexRanges;
	}
}
