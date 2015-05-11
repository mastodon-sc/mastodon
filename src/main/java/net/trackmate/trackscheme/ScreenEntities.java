package net.trackmate.trackscheme;

import java.util.List;

/**
 * A collection of layouted screen objects to paint. Comprises lists of
 * {@link ScreenVertex}, {@link ScreenEdge}, and {@link ScreenVertexRange}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class ScreenEntities
{
	public final List< ScreenVertex > vertices;

	public final List< ScreenEdge > edges;

	public final List< ScreenVertexRange > vertexRanges;

	public ScreenEntities( final List< ScreenVertex > vertices, final List< ScreenEdge > edges, final List<ScreenVertexRange> vertexRanges )
	{
		this.vertices = vertices;
		this.edges = edges;
		this.vertexRanges = vertexRanges;
	}
}
