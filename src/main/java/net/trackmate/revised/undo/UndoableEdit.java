package net.trackmate.revised.undo;

public interface UndoableEdit
{
	public void undo();

	public void redo();

	public boolean isUndoPoint();

	public void setUndoPoint( boolean isUndoPoint );
}
