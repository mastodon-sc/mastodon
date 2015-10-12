package net.trackmate.kdtree;

public enum KDTreeNodeFlags implements KDTreeNodeFlag
{
	NODE_INVALID_FLAG( 1 );

	private final int intValue;

	@Override
	public int intValue()
	{
		return intValue;
	}

	private KDTreeNodeFlags( final int intValue )
	{
		this.intValue = intValue;
	}
}
