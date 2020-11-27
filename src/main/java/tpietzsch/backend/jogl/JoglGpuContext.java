package tpietzsch.backend.jogl;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import tpietzsch.backend.GpuContext;
import tpietzsch.backend.SetUniforms;
import tpietzsch.backend.StagingBuffer;
import tpietzsch.backend.Texture;
import tpietzsch.backend.Texture3D;
import tpietzsch.shadergen.Shader;

import static com.jogamp.opengl.GL.GL_ACTIVE_TEXTURE;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_R32F;
import static com.jogamp.opengl.GL.GL_R8;
import static com.jogamp.opengl.GL.GL_REPEAT;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_BINDING_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL.GL_WRITE_ONLY;
import static com.jogamp.opengl.GL2ES2.GL_CLAMP_TO_BORDER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_3D;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_BINDING_3D;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_BORDER_COLOR;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_PIXEL_UNPACK_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_PIXEL_UNPACK_BUFFER_BINDING;
import static com.jogamp.opengl.GL2ES3.GL_RGBA8UI;
import static com.jogamp.opengl.GL2ES3.GL_RGBA_INTEGER;
import static com.jogamp.opengl.GL2GL3.GL_R16;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_1D;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_BINDING_1D;
import static tpietzsch.backend.Texture.Wrap.CLAMP_TO_BORDER_ZERO;

public class JoglGpuContext implements GpuContext
{
	@Override
	public void use( final Shader shader )
	{
		final ShaderProgram prog = getShaderProgram( shader );
		gl.glUseProgram( prog.program() );
	}

	@Override
	public SetUniforms getUniformSetter( final Shader shader )
	{
		final ShaderProgram prog = getShaderProgram( shader );
		return new JoglSetUniforms( gl, prog.program() );
	}

	@Override
	public int bindStagingBuffer( final StagingBuffer stagingBuffer )
	{
		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_PIXEL_UNPACK_BUFFER_BINDING, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, getPboId( stagingBuffer ) );

		return restoreId;
	}

	@Override
	public int bindStagingBufferId( final int id )
	{
		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_PIXEL_UNPACK_BUFFER_BINDING, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, id );

		return restoreId;
	}

	@Override
	public int bindTexture( final Texture texture )
	{
		final TexId texId = getTextureId( texture );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( texId.binding, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		gl.glBindTexture( texId.target, texId.id );

		return restoreId;
	}

	@Override
	public void bindTexture( final Texture texture, final int unit )
	{
		final TexId texId = getTextureId( texture );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_ACTIVE_TEXTURE, tmp, 0 );
		final int restoreActiveTextureId = tmp[ 0 ];

		gl.glActiveTexture( GL_TEXTURE0 + unit );

		gl.glGetIntegerv( texId.binding, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		gl.glBindTexture( texId.target, texId.id );
	}

	@Override
	public int bindTextureId( final int id, final int numTexDimensions )
	{
		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( targetBinding( numTexDimensions ), tmp, 0 );
		final int restoreId = tmp[ 0 ];

		gl.glBindTexture( target( numTexDimensions ), id );

		return restoreId;
	}

	@Override
	public Buffer map( final StagingBuffer stagingBuffer )
	{
		final int pboId = getPboId( stagingBuffer );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_PIXEL_UNPACK_BUFFER_BINDING, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		if ( restoreId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, pboId );

		gl.glBufferData( GL_PIXEL_UNPACK_BUFFER, stagingBuffer.getSizeInBytes(), null, GL_STREAM_DRAW );
		final ByteBuffer buffer = gl.glMapBuffer( GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY );

		if ( restoreId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, restoreId );

		return buffer;
	}

	@Override
	public void unmap( final StagingBuffer stagingBuffer )
	{
		final int pboId = getPboId( stagingBuffer );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_PIXEL_UNPACK_BUFFER_BINDING, tmp, 0 );
		final int restoreId = tmp[ 0 ];

		if ( restoreId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, pboId );

		gl.glUnmapBuffer( GL_PIXEL_UNPACK_BUFFER );

		if ( restoreId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, restoreId );
	}

	@Override
	public void delete( final Texture texture )
	{
		final TexId texId = textures.remove( texture );
		if ( texId != null )
			gl.glDeleteTextures( 1, new int[] { texId.id }, 0 );
	}

	@Override
	public void texSubImage3D( final StagingBuffer stagingBuffer, final Texture3D texture, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final long pixels_buffer_offset )
	{
		final int pboId = getPboId( stagingBuffer );
		final int textureId = getTextureId( texture ).id;

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_PIXEL_UNPACK_BUFFER_BINDING, tmp, 0 );
		final int restorePboId = tmp[ 0 ];

		gl.glGetIntegerv( GL_TEXTURE_BINDING_3D, tmp, 0 );
		final int restoreTextureId = tmp[ 0 ];

		if ( restorePboId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, pboId );

		if ( restoreTextureId != textureId )
			gl.glBindTexture( GL_TEXTURE_3D, textureId );

		gl.glTexSubImage3D( GL_TEXTURE_3D, 0, xoffset, yoffset, zoffset, width, height, depth, format( texture ), type( texture ), pixels_buffer_offset );

		if ( restorePboId != pboId )
			gl.glBindBuffer( GL_PIXEL_UNPACK_BUFFER, restorePboId );

		if ( restoreTextureId != textureId )
			gl.glBindTexture( GL_TEXTURE_3D, restoreTextureId );
	}

	@Override
	public void texSubImage3D( final Texture3D texture, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final Buffer pixels )
	{
		final int textureId = getTextureId( texture ).id;

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_TEXTURE_BINDING_3D, tmp, 0 );
		final int restoreTextureId = tmp[ 0 ];

		if ( restoreTextureId != textureId )
			gl.glBindTexture( GL_TEXTURE_3D, textureId );

		gl.glTexSubImage3D( GL_TEXTURE_3D, 0, xoffset, yoffset, zoffset, width, height, depth, format( texture ), type( texture ), pixels );

		if ( restoreTextureId != textureId )
			gl.glBindTexture( GL_TEXTURE_3D, restoreTextureId );
	}

	public static JoglGpuContext get( final GL3 gl )
	{
		return contexts.computeIfAbsent( gl, JoglGpuContext::new );
	}

	/*
	 * direct access to OpenGL objects
	 */

	public int getPboIdHack( StagingBuffer stagingBuffer )
	{
		return pbos.get( stagingBuffer );
	}

	public int getTextureIdHack( Texture texture )
	{
		return textures.get( texture ).id;
	}

	public int getProgramIdHack( Shader shader )
	{
		return shaders.get( shader ).program();
	}

	/*
	 * PRIVATE IMPLEMENTATION
	 */

	private static Map< GL3, JoglGpuContext > contexts = new HashMap<>();

	private final GL3 gl;

	private final Map< Shader, ShaderProgram > shaders = new WeakHashMap<>();

	private final Map< StagingBuffer, Integer > pbos = new WeakHashMap<>();

	private final Map< Texture, TexId > textures = new WeakHashMap<>();

	private JoglGpuContext( final GL3 gl )
	{
		this.gl = gl;
	}

	private ShaderProgram getShaderProgram( final Shader shader )
	{
		return shaders.computeIfAbsent( shader, s -> {
			final ShaderCode vs = new ShaderCode( GL_VERTEX_SHADER, 1, new CharSequence[][] { { s.getVertexShaderCode() } } );
			final ShaderCode fs = new ShaderCode( GL_FRAGMENT_SHADER, 1, new CharSequence[][] { { s.getFragmentShaderCode() } } );
			vs.defaultShaderCustomization( gl, true, false );
			fs.defaultShaderCustomization( gl, true, false );
			final ShaderProgram prog = new ShaderProgram();
			prog.add( vs );
			prog.add( fs );
			prog.link( gl, System.err );
			vs.destroy( gl );
			fs.destroy( gl );
			return prog;
		} );
	}

	private int getPboId( final StagingBuffer stagingBuffer )
	{
		return pbos.computeIfAbsent( stagingBuffer, o -> {
			final int[] tmp = new int[ 1 ];
			gl.glGenBuffers( 1, tmp, 0 );
			return tmp[ 0 ];
		} );
	}

	private static class TexId
	{
		int id;

		int target; // GL_TEXTURE_1D, GL_TEXTURE_2D, or GL_TEXTURE_3D

		int binding; // GL_TEXTURE_BINDING_1D, GL_TEXTURE_BINDING_2D, or GL_TEXTURE_BINDING_3D

		TexId( final int id, final int target, final int binding )
		{
			this.id = id;
			this.target = target;
			this.binding = binding;
		}
	}

	private TexId getTextureId( final Texture texture )
	{
		return textures.computeIfAbsent( texture, tex -> {
			final int[] tmp = new int[ 1 ];
			gl.glGenTextures( 1, tmp, 0 );
			final int id = tmp[ 0 ];

			final int target = target( tex );
			gl.glGetIntegerv( targetBinding( tex ), tmp, 0 );
			final int restoreId = tmp[ 0 ];

			gl.glBindTexture( target, id );
			gl.glTexStorage3D( target, 1, internalFormat( tex ), tex.texWidth(), tex.texHeight(), tex.texDepth() );
			gl.glTexParameteri( target, GL_TEXTURE_MIN_FILTER, minFilter( tex ) );
			gl.glTexParameteri( target, GL_TEXTURE_MAG_FILTER, magFilter( tex ) );
			gl.glTexParameteri( target, GL_TEXTURE_WRAP_S, wrap( tex ) );
			if ( tex.texDims() > 1 )
				gl.glTexParameteri( target, GL_TEXTURE_WRAP_T, wrap( tex ) );
			if ( tex.texDims() > 2 )
				gl.glTexParameteri( target, GL_TEXTURE_WRAP_R, wrap( tex ) );
			if ( tex.texWrap() == CLAMP_TO_BORDER_ZERO )
				gl.glTexParameterfv( target, GL_TEXTURE_BORDER_COLOR, new float[ 4 ], 1 );

			gl.glBindTexture( target, restoreId );

			return new TexId( id, target, targetBinding( texture ) );
		} );
	}

	public void registerTexture( final Texture texture, final int id )
	{
		textures.computeIfAbsent( texture, tex -> {
			final int[] tmp = new int[ 1 ];

			final int target = target( tex );
			gl.glGetIntegerv( targetBinding( tex ), tmp, 0 );
			final int restoreId = tmp[ 0 ];

			gl.glBindTexture( target, id );
			gl.glTexParameteri( target, GL_TEXTURE_MIN_FILTER, minFilter( tex ) );
			gl.glTexParameteri( target, GL_TEXTURE_MAG_FILTER, magFilter( tex ) );
			gl.glTexParameteri( target, GL_TEXTURE_WRAP_S, wrap( tex ) );
			if ( tex.texDims() > 1 )
				gl.glTexParameteri( target, GL_TEXTURE_WRAP_T, wrap( tex ) );
			if ( tex.texDims() > 2 )
				gl.glTexParameteri( target, GL_TEXTURE_WRAP_R, wrap( tex ) );

			gl.glBindTexture( target, restoreId );

			return new TexId( id, target, targetBinding( texture ) );
		} );
	}

	private static int target( Texture texture )
	{
		return target( texture.texDims() );
	}

	private static int target( final int numTexDimensions )
	{
		switch ( numTexDimensions )
		{
		case 1:
			return GL_TEXTURE_1D;
		case 2:
			return GL_TEXTURE_2D;
		case 3:
			return GL_TEXTURE_3D;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int targetBinding( Texture texture )
	{
		return targetBinding( texture.texDims() );
	}

	private static int targetBinding( final int numTexDimensions )
	{
		switch ( numTexDimensions )
		{
		case 1:
			return GL_TEXTURE_BINDING_1D;
		case 2:
			return GL_TEXTURE_BINDING_2D;
		case 3:
			return GL_TEXTURE_BINDING_3D;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int internalFormat( Texture texture )
	{
		switch ( texture.texInternalFormat() )
		{
		case R8:
			return GL_R8;
		case R16:
			return GL_R16;
		case RGBA8:
			return GL_RGBA8;
		case RGBA8UI:
			return GL_RGBA8UI;
		case R32F:
			return GL_R32F;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int format( Texture texture )
	{
		switch ( texture.texInternalFormat() )
		{
		case R8:
			return GL_RED;
		case R16:
			return GL_RED;
		case RGBA8:
			return GL_RGBA;
		case RGBA8UI:
			return GL_RGBA_INTEGER;
		case R32F:
			return GL_RED;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int type( Texture texture )
	{
		switch ( texture.texInternalFormat() )
		{
		case R8:
			return GL_UNSIGNED_BYTE;
		case R16:
			return GL_UNSIGNED_SHORT;
		case RGBA8:
			return GL_UNSIGNED_BYTE;
		case RGBA8UI:
			return GL_UNSIGNED_BYTE;
		case R32F:
			return GL_FLOAT;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int magFilter( Texture texture )
	{
		switch ( texture.texMagFilter() )
		{
		case NEAREST:
			return GL_NEAREST;
		case LINEAR:
			return GL_LINEAR;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int minFilter( Texture texture )
	{
		switch ( texture.texMinFilter() )
		{
		case NEAREST:
			return GL_NEAREST;
		case LINEAR:
			return GL_LINEAR;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static int wrap( Texture texture )
	{
		switch ( texture.texWrap() )
		{
		case CLAMP_TO_EDGE:
			return GL_CLAMP_TO_EDGE;
		case CLAMP_TO_BORDER_ZERO:
			return GL_CLAMP_TO_BORDER;
		case REPEAT:
			return GL_REPEAT;
		default:
			throw new IllegalArgumentException();
		}
	}
}
