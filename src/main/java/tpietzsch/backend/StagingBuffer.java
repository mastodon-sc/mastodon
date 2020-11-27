package tpietzsch.backend;

/**
 * Handle to a staging buffer for texture data. A staging buffer can be
 * {@link GpuContext#map(StagingBuffer) mapped} to obtain a
 * {@link java.nio.Buffer} that can be filled with data. When done writing data,
 * the staging buffer can be {@link GpuContext#unmap(StagingBuffer) unmapping}.
 * Texture blocks at a given offset within the filled staging buffer can be
 * uploaded with
 * {@link GpuContext#texSubImage3D(StagingBuffer, Texture3D, int, int, int, int, int, int, long)}
 */
public interface StagingBuffer
{
	int getSizeInBytes();
}
