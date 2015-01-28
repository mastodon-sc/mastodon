package net.trackmate.trackscheme;

public class ScreenVertex
{
	// could be internalPoolIndex of original vertex?
	private final int id;

	// screen coordinate x
	private final double x;

	// screen coordinate y
	private final double y;

	private final String label;

	private final boolean selected;

	public ScreenVertex(
			final int id,
			final double x,
			final double y,
			final String label,
			final boolean selected )
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.label = label;
		this.selected = selected;
	}

	public int getId()
	{
		return id;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isSelected()
	{
		return selected;
	}
}