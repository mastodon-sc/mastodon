package net.trackmate.trackscheme;

/**
 * Layouted dense vertex area.
 *
 * TODO: make this a PoolObject.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertexRange
{
	private final double minX;

	private final double maxX;

	private final double minY;

	private final double maxY;

	public ScreenVertexRange( final double minX, final double maxX, final double minY, final double maxY )
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public double getMinX()
	{
		return minX;
	}

	public double getMaxX()
	{
		return maxX;
	}

	public double getMinY()
	{
		return minY;
	}

	public double getMaxY()
	{
		return maxY;
	}
}
