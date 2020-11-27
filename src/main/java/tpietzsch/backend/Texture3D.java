package tpietzsch.backend;

public interface Texture3D extends Texture
{
	@Override
	default int texDims()
	{
		return 3;
	}
}
