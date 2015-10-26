package net.trackmate.revised.trackscheme.display;


public class ScreenTransform
{
	double minX;

	double maxX;

	double minY;

	double maxY;

	int screenWidth;

	int screenHeight;

	public ScreenTransform()
	{}

	public ScreenTransform( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public ScreenTransform copy()
	{
		return new ScreenTransform( minX, maxX, minY, maxY, screenWidth, screenHeight );
	}

	public void set( final ScreenTransform t )
	{
		this.minX = t.minX;
		this.maxX = t.maxX;
		this.minY = t.minY;
		this.maxY = t.maxY;
		this.screenWidth = t.screenWidth;
		this.screenHeight = t.screenHeight;
	}

	void setScreenTranslated( final int dX, final int dY, final ScreenTransform source )
	{
		final double xInvScale = ( maxX - minX ) / ( screenWidth - 1 );
		final double yInvScale = ( maxY - minY ) / ( screenHeight - 1 );
		minX = source.minX + xInvScale * dX;
		maxX = source.maxX + xInvScale * dX;
		minY = source.minY + yInvScale * dY;
		maxY = source.maxY + yInvScale * dY;
	}

	double screenToLayoutX( final int x )
	{
		final double xInvScale = ( maxX - minX ) / ( screenWidth - 1 );
		return minX + xInvScale * x;
	}

	double screenToLayoutY( final int y )
	{
		final double yInvScale = ( maxY - minY ) / ( screenHeight - 1 );
		return minY + yInvScale * y;
	}

	void scale( final double scale, final int x, final int y )
	{
		final double lX = screenToLayoutX( x );
		final double lY = screenToLayoutY( y );
		final double newSizeX = ( maxX - minX ) * scale;
		final double newSizeY = ( maxY - minY ) * scale;
		final double newXInvScale = newSizeX / ( screenWidth - 1 );
		final double newYInvScale = newSizeY / ( screenHeight - 1 );
		minX = lX - newXInvScale * x;
		maxX = minX + newSizeX;
		minY = lY - newYInvScale * y;
		maxY = minY + newSizeY;
	}

	void scaleX( final double scale, final int x, final int y )
	{
		final double lX = screenToLayoutX( x );
		final double newSizeX = ( maxX - minX ) * scale;
		final double newXInvScale = newSizeX / ( screenWidth - 1 );
		minX = lX - newXInvScale * x;
		maxX = minX + newSizeX;
	}

	void scaleY( final double scale, final int x, final int y )
	{
		final double lY = screenToLayoutY( y );
		final double newSizeY = ( maxY - minY ) * scale;
		final double newYInvScale = newSizeY / ( screenHeight - 1 );
		minY = lY - newYInvScale * y;
		maxY = minY + newSizeY;
	}

	@Override
	public String toString()
	{
		return "X: " + minX + " -> " + maxX + ", Y: " + minY + " -> " + maxY + ", width = " + screenWidth + ", height = " + screenHeight;
	}
}
