package net.trackmate.trackscheme;

public class ScreenEdge
{
	// could be internalPoolIndex of original vertex?
	private final int id;

	private final int sourceScreenVertexIndex;

	private final int targetScreenVertexIndex;

	private final boolean selected;

	public ScreenEdge(
			final int id,
			final int sourceScreenVertexIndex,
			final int targetScreenVertexIndex,
			final boolean selected )
	{
		this.id = id;
		this.sourceScreenVertexIndex = sourceScreenVertexIndex;
		this.targetScreenVertexIndex = targetScreenVertexIndex;
		this.selected = selected;
	}

	public int getId()
	{
		return id;
	}

	public int getSourceScreenVertexIndex()
	{
		return sourceScreenVertexIndex;
	}

	public int getTargetScreenVertexIndex()
	{
		return targetScreenVertexIndex;
	}

	public boolean isSelected()
	{
		return selected;
	}
}