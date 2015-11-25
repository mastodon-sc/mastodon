package net.trackmate.revised.bdv.overlay;

public interface OverlayNavigation< O extends OverlayVertex< ?, ? > >
{
	public void navigateTo( O vertex );

}
