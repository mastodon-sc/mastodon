package tpietzschx.backend.jogl;

import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import tpietzschx.backend.SetUniforms;

public class JoglSetUniforms implements SetUniforms
{
	private final GL2ES2 gl;

	private final int program;

	JoglSetUniforms( final GL2ES2 gl, final int program )
	{
		this.gl = gl;
		this.program = program;
	}

	@Override
	public boolean shouldSet( final boolean modified )
	{
		return modified;
	}

	@Override
	public void setUniform1i( final String name, final int v0 )
	{
		gl.glProgramUniform1i( program, location( name ), v0 );
	}

	@Override
	public void setUniform2i( final String name, final int v0, final int v1 )
	{
		gl.glProgramUniform2i( program, location( name ), v0, v1 );
	}

	@Override
	public void setUniform3i( final String name, final int v0, final int v1, final int v2 )
	{
		gl.glProgramUniform3i( program, location( name ), v0, v1, v2 );
	}

	@Override
	public void setUniform4i( final String name, final int v0, final int v1, final int v2, final int v3 )
	{
		gl.glProgramUniform4i( program, location( name ), v0, v1, v2, v3 );
	}

	@Override
	public void setUniform1f( final String name, final float v0 )
	{
		gl.glProgramUniform1f( program, location( name ), v0 );
	}

	@Override
	public void setUniform2f( final String name, final float v0, final float v1 )
	{
		gl.glProgramUniform2f( program, location( name ), v0, v1 );
	}

	@Override
	public void setUniform3f( final String name, final float v0, final float v1, final float v2 )
	{
		gl.glProgramUniform3f( program, location( name ), v0, v1, v2 );
	}

	@Override
	public void setUniform4f( final String name, final float v0, final float v1, final float v2, final float v3 )
	{
		gl.glProgramUniform4f( program, location( name ), v0, v1, v2, v3 );
	}

	@Override
	public void setUniform1fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform1fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform2fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform2fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform3fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform3fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniformMatrix4f( final String name, final boolean transpose, final FloatBuffer value )
	{
		gl.glProgramUniformMatrix4fv( program, location( name ), 1, transpose, value );
	}

	@Override
	public void setUniformMatrix3f( final String name, final boolean transpose, final FloatBuffer value )
	{
		gl.glProgramUniformMatrix3fv( program, location( name ), 1, transpose, value );
	}

	private int location( final String name )
	{
		return gl.glGetUniformLocation( program, name );
	}
}
