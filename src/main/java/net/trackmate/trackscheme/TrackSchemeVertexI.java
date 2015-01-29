package net.trackmate.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public interface TrackSchemeVertexI< V extends TrackSchemeVertexI< V, E >, E extends Edge< V > > extends Vertex< E >
{
	// could be internalPoolIndex?
	public int getId();

	public String getLabel();

	public int getTimePoint();

	public int getScreenVertexIndex();

	public void setScreenVertexIndex( final int screenVertexIndex );

	public double getLayoutX();

	public void setLayoutX( double x );

	public boolean isSelected();

	public void setSelected( boolean selected );
}
