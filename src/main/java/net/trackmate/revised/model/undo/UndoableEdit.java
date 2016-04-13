package net.trackmate.revised.model.undo;

public interface UndoableEdit
{
	public void undo();

	public void redo();

	public boolean isUndoPoint();

	public void setUndoPoint( boolean isUndoPoint );
}
