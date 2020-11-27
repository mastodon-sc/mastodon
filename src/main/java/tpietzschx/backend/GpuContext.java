package tpietzschx.backend;

import java.nio.Buffer;
import tpietzschx.shadergen.Shader;

public interface GpuContext
{
	// build and cache and bind
	void use( Shader shader );

	// from cached shader thingie and gl context, build SetUniforms
	SetUniforms getUniformSetter( Shader shader );

	/**
	 * @param stagingBuffer staging buffer to bind
	 * @return id of previously bound staging buffer
	 */
	int bindStagingBuffer( StagingBuffer stagingBuffer );

	/**
	 * @param id staging buffer id to bind
	 * @return id of previously bound staging buffer
	 */
	int bindStagingBufferId( int id );

	/**
	 * @param texture texture to bind
	 * @return id of previously bound texture
	 */
	int bindTexture( Texture texture );

	/**
	 * @param texture texture to bind
	 * @param unit texture unit to bind to
	 */
	void bindTexture( Texture texture, int unit );

	/**
	 * @param id texture id to bind
	 * @param numTexDimensions texture target: 1, 2, or 3
	 * @return id of previously bound texture
	 */
	int bindTextureId( int id, int numTexDimensions );

	// map staging buffer, initialize and cache if necessary
	// previous staging buffer binding is restored when done
	Buffer map( StagingBuffer stagingBuffer );

	// unmap a (mapped) staging buffer
	// previous staging buffer binding is restored when done
	void unmap( StagingBuffer stagingBuffer );

	// delete a texture (if it has already been allocated)
	// used to redefine a texture that has changed size
	void delete( Texture texture );

	// upload texture block from staging buffer
	// previous staging buffer binding is restored when done
	void texSubImage3D( StagingBuffer stagingBuffer, Texture3D texture, int xoffset, int yoffset, int zoffset, int width, int height, int depth, long pixels_buffer_offset );

	// upload texture block from Buffer
	void texSubImage3D( Texture3D texture, int xoffset, int yoffset, int zoffset, int width, int height, int depth, Buffer pixels );
}
