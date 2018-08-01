package org.mastodon.revised.bvv.scene;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.shadergen.DefaultShader;
import tpietzsch.shadergen.Shader;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

public class InstancedEllipsoid
{
	private final int subdivisions;

	private final Shader prog;

	private int ivbo;

	private int vao;

	private int numElements;

	public InstancedEllipsoid( final int subdivisions )
	{
		this.subdivisions = subdivisions;
		final Segment ex1vp = new SegmentTemplate(InstancedEllipsoid.class, "instancedellipsoid.vp" ).instantiate();
		final Segment ex1fp = new SegmentTemplate(InstancedEllipsoid.class, "instancedellipsoid.fp" ).instantiate();
		prog = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );
	}

	private boolean initialized;

	private void init( GL3 gl )
	{
		initialized = true;

		final UnitSphere sphere = new UnitSphere( subdivisions );
		final float[] vertices = sphere.getVertices();
		final int[] indices = sphere.getIndices();
		numElements = sphere.getNumElements();

		final int[] tmp = new int[ 3 ];
		gl.glGenBuffers( 3, tmp, 0 );
		final int vbo = tmp[ 0 ];
		final int ebo = tmp[ 1 ];
		ivbo = tmp[ 2 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, vbo );
		gl.glBufferData( GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, ebo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		// ..:: VERTEX ARRAY OBJECT ::..

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL_ARRAY_BUFFER, vbo );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindBuffer( GL_ARRAY_BUFFER, ivbo );
		for ( int i = 0; i < 7; ++i )
		{
			gl.glVertexAttribPointer( 1 + i, 3, GL_FLOAT, false, 21 * Float.BYTES, i * 3 * Float.BYTES );
			gl.glEnableVertexAttribArray( 1 + i );
			gl.glVertexAttribDivisor( 1 + i, 1 );
		}
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, ebo );
		gl.glBindVertexArray( 0 );
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4f vm, FloatBuffer data )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );

		final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();
		prog.getUniformMatrix4f( "pvm" ).set( pvm );
		prog.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
		prog.setUniforms( context );
		prog.use( context );

		gl.glBindVertexArray( vao );
		gl.glBindBuffer(GL_ARRAY_BUFFER, ivbo );
		final int size = data.capacity();
		gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
		gl.glDrawElementsInstanced( GL_TRIANGLES, numElements, GL_UNSIGNED_INT, 0, size / 21 );
		gl.glBindVertexArray( 0 );
	}
}
