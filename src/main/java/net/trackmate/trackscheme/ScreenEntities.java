package net.trackmate.trackscheme;

import java.util.List;

public class ScreenEntities
{
	public List< ScreenVertex > vertices;

	public List< ScreenEdge > edges;

	public ScreenEntities( final List< ScreenVertex > vertices, final List< ScreenEdge > edges )
	{
		this.vertices = vertices;
		this.edges = edges;
	}
}
