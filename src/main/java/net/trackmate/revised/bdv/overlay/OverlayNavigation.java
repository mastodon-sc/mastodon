package net.trackmate.revised.bdv.overlay;


public interface OverlayNavigation< O extends OverlayVertex< ?, ? > >
{
	public void navigateToOverlayVertex( O vertex );

	public void notifyListeners( O vertex );
}
