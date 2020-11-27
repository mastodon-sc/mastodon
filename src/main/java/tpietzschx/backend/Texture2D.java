package tpietzschx.backend;

public interface Texture2D extends Texture
{
	@Override
	default int texDims()
	{
		return 2;
	}

	@Override
	default int texDepth()
	{
		return 1;
	}
}
