package net.trackmate.spatial;

public interface VertexPositionChangeProvider< V >
{
	public boolean addVertexPositionListener( final VertexPositionListener< V > listener );

	public boolean removeVertexPositionListener( final VertexPositionListener< V > listener );
}
