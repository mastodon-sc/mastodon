package net.trackmate.trackscheme;

public interface SelectionListener
{
	public void selectAt( final ScreenTransform transform, int x, int y );

	public void selectWithin( ScreenTransform transform, int x1, int y1, int x2, int y2 );
}
