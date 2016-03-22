package net.trackmate.revised.bdv.overlay;

public interface OverlayNavigation< V extends OverlayVertex< ?, ? >, E extends OverlayEdge< ?, ? > >
{
	public void notifyNavigateToVertex( V vertex );

	public void notifyNavigateToEdge( E edge );
}
