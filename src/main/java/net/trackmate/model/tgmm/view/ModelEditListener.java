package net.trackmate.model.tgmm.view;

public interface ModelEditListener
{
	public static class ModelEditEvent
	{}

	public void modelEdited( ModelEditEvent event );

}
