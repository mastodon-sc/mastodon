package tpietzschx.backend;

import java.nio.FloatBuffer;

public interface SetUniforms
{
	boolean shouldSet( boolean modified );

	void setUniform1i( final String name, final int v0 );

	void setUniform2i( final String name, final int v0, final int v1 );

	void setUniform3i( final String name, final int v0, final int v1, final int v2 );

	void setUniform4i( final String name, final int v0, final int v1, final int v2, final int v3 );

	void setUniform1f( final String name, final float v0 );

	void setUniform2f( final String name, final float v0, final float v1 );

	void setUniform3f( final String name, final float v0, final float v1, final float v2 );

	void setUniform4f( final String name, final float v0, final float v1, final float v2, final float v3 );

	void setUniform1fv( final String name, final int count, final float[] value );

	void setUniform2fv( final String name, final int count, final float[] value );

	void setUniform3fv( final String name, final int count, final float[] value );

	// transpose==true: data is in row-major order
	void setUniformMatrix3f( final String name, final boolean transpose, final FloatBuffer value );

	// transpose==true: data is in row-major order
	void setUniformMatrix4f( final String name, final boolean transpose, final FloatBuffer value );
}
