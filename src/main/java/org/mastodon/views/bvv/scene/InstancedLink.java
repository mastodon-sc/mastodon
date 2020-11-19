package org.mastodon.views.bvv.scene;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.mastodon.views.bvv.scene.HotLoading.ShaderHotLoader;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.shadergen.DefaultShader;
import tpietzsch.shadergen.Shader;
import tpietzsch.shadergen.Uniform1f;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

/**
 * Draw instanced cylinders.
 * TODO
 * <p>
 * {@code InstancedCylinder} sets up vertex buffer and element buffer objects to to draw a unit cylinder.
 * Then {@link #draw} is called with a {@code Cylinders} collection to draw a set of cylindrical frustums as instances of the transformed unit cylinder.
 */
public class InstancedLink
{
	private final int cylinderSubdivisions;
	private final int sphereSubdivisions;

	private final ShaderHotLoader hotloader;
	private Shader cylinderProg;
	private Shader sphereProg;
	private Shader cylinderHighlightProg;
	private Shader sphereHighlightProg;
	private Shader sphereTargetHighlightProg;

	private int cylinderVbo;
	private int cylinderEbo;
	private int cylinderNumElements;

	private int sphereVbo;
	private int sphereEbo;
	private int sphereNumElements;

	private final ReusableResources< Cylinders, InstanceArray > instanceArrays;

	public InstancedLink()
	{
		this( 36, 3, 20 );
	}

	public InstancedLink( final int cylinderSubdivisions, final int sphereSubdivisions, final int numReusableInstanceArrays  )
	{
		this.cylinderSubdivisions = cylinderSubdivisions;
		this.sphereSubdivisions = sphereSubdivisions;
		hotloader = new ShaderHotLoader()
				.watch( InstancedLink.class, "instancedcylinder.vp" )
				.watch( InstancedLink.class, "instancedsphere.vp" )
				.watch( InstancedLink.class, "instancedellipsoid.fp" )
				.watch( InstancedLink.class, "instancedcylinder-highlight.vp" )
				.watch( InstancedSpot.class, "instancedsphere-highlight.vp" )
				.watch( InstancedSpot.class, "instancedsphere-target-highlight.vp" )
				.watch( InstancedSpot.class, "instancedellipsoid-highlight.fp" );
		hotloadShader();
		instanceArrays = new ReusableResources<>( numReusableInstanceArrays, InstanceArray::new );
	}

	private void hotloadShader()
	{
		if ( hotloader.isModified() || cylinderProg == null )
		{
			final Segment ex1vp = new SegmentTemplate( InstancedLink.class, "instancedcylinder.vp" ).instantiate();
			final Segment ex1fp = new SegmentTemplate( InstancedLink.class, "instancedellipsoid.fp" ).instantiate();
			cylinderProg = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );

			final Segment ex2vp = new SegmentTemplate( InstancedLink.class, "instancedsphere.vp" ).instantiate();
			final Segment ex2fp = new SegmentTemplate( InstancedLink.class, "instancedellipsoid.fp" ).instantiate();
			sphereProg = new DefaultShader( ex2vp.getCode(), ex2fp.getCode() );

			final Segment ex3vp = new SegmentTemplate( InstancedLink.class, "instancedcylinder-highlight.vp" ).instantiate();
			final Segment ex3fp = new SegmentTemplate( InstancedLink.class, "instancedellipsoid-highlight.fp" ).instantiate();
			cylinderHighlightProg = new DefaultShader( ex3vp.getCode(), ex3fp.getCode() );

			final Segment ex4vp = new SegmentTemplate( InstancedSpot.class, "instancedsphere-highlight.vp" ).instantiate();
			final Segment ex4fp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid-highlight.fp" ).instantiate();
			sphereHighlightProg = new DefaultShader( ex4vp.getCode(), ex4fp.getCode() );

			final Segment ex5vp = new SegmentTemplate( InstancedSpot.class, "instancedsphere-target-highlight.vp" ).instantiate();
			final Segment ex5fp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid-highlight.fp" ).instantiate();
			sphereTargetHighlightProg = new DefaultShader( ex5vp.getCode(), ex5fp.getCode() );
		}
	}

	private boolean initialized;

	private void init( GL3 gl )
	{
		initialized = true;

		final UnitCylinder cylinder = new UnitCylinder( cylinderSubdivisions );
		final float[] cylinderVertices = cylinder.getVertices();
		final int[] cylinderIndices = cylinder.getIndices();
		cylinderNumElements = cylinder.getNumElements();

		final int[] tmp = new int[ 2 ];
		gl.glGenBuffers( 2, tmp, 0 );
		cylinderVbo = tmp[ 0 ];
		cylinderEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, cylinderVertices.length * Float.BYTES, FloatBuffer.wrap( cylinderVertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, cylinderIndices.length * Integer.BYTES, IntBuffer.wrap( cylinderIndices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		final UnitSphere sphere = new UnitSphere( sphereSubdivisions );
		final float[] sphereVertices = sphere.getVertices();
		final int[] sphereIndices = sphere.getIndices();
		sphereNumElements = sphere.getNumElements();

		gl.glGenBuffers( 2, tmp, 0 );
		sphereVbo = tmp[ 0 ];
		sphereEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, sphereVertices.length * Float.BYTES, FloatBuffer.wrap( sphereVertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, sphereIndices.length * Integer.BYTES, IntBuffer.wrap( sphereIndices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
	}

	class InstanceArray extends ReusableResource< Cylinders >
	{
		private int instanceCount;
		private int vboShape;
		private int vboColor;
		private int cylinderVao;
		private int sphereVao;
		private int sphereTargetVao;

		private boolean initialized;

		private void init( GL3 gl )
		{
			initialized = true;

			final int[] tmp = new int[ 3 ];
			gl.glGenBuffers( 2, tmp, 0 );
			vboShape = tmp[ 0 ];
			vboColor = tmp[ 1 ];
			gl.glGenVertexArrays( 3, tmp, 0 );
			cylinderVao = tmp[ 0 ];
			sphereVao = tmp[ 1 ];
			sphereTargetVao = tmp[ 2 ];

			gl.glBindVertexArray( cylinderVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
			for ( int i = 0; i < 7; ++i )
			{
				gl.glVertexAttribPointer( 1 + i, 3, GL_FLOAT, false, 21 * Float.BYTES, i * 3 * Float.BYTES );
				gl.glEnableVertexAttribArray( 1 + i );
				gl.glVertexAttribDivisor( 1 + i, 1 );
			}
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
			gl.glVertexAttribPointer( 8, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 8 );
			gl.glVertexAttribDivisor( 8, 1 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
			gl.glBindVertexArray( 0 );

			gl.glBindVertexArray( sphereVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
			gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 21 * Float.BYTES, 18 * Float.BYTES );
			gl.glEnableVertexAttribArray( 1 );
			gl.glVertexAttribDivisor( 1, 1 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
			gl.glVertexAttribPointer( 2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 2 );
			gl.glVertexAttribDivisor( 2, 1 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
			gl.glBindVertexArray( 0 );

			gl.glBindVertexArray( sphereTargetVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
			for ( int i = 0; i < 3; ++i )
			{
				gl.glVertexAttribPointer( 1 + i, 3, GL_FLOAT, false, 21 * Float.BYTES, i * 3 * Float.BYTES );
				gl.glEnableVertexAttribArray( 1 + i );
				gl.glVertexAttribDivisor( 1 + i, 1 );
			}
			gl.glVertexAttribPointer( 4, 3, GL_FLOAT, false, 21 * Float.BYTES, 18 * Float.BYTES );
			gl.glEnableVertexAttribArray( 4 );
			gl.glVertexAttribDivisor( 4, 1 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
			gl.glVertexAttribPointer( 5, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 5 );
			gl.glVertexAttribDivisor( 5, 1 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
			gl.glBindVertexArray( 0 );
		}

		private void update( GL3 gl )
		{
			boolean updateShape = needsUpdate.getAndSet( false );
			boolean updateColor = updateShape;

			final Cylinders cylinders = this.key();
			updateShape |= cylinders.shapes.getAndClearModified();
			updateColor |= cylinders.colors.getAndClearModified();

			if ( updateShape )
			{
				final FloatBuffer data = cylinders.shapes.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
				instanceCount = cylinders.size();
			}

			if ( updateColor )
			{
				final FloatBuffer data = cylinders.colors.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
			}
		}

		private void draw( GL3 gl, JoglGpuContext context, boolean onlyHighlights )
		{
			if ( !initialized )
				init( gl );

			update( gl );

			if ( onlyHighlights )
				cylinderHighlightProg.use( context );
			else
				cylinderProg.use( context );
			gl.glBindVertexArray( cylinderVao );
			gl.glDrawElementsInstanced( GL_TRIANGLES, cylinderNumElements, GL_UNSIGNED_INT, 0, instanceCount );
			gl.glBindVertexArray( 0 );

			if ( onlyHighlights )
				sphereHighlightProg.use( context );
			else
				sphereProg.use( context );
			gl.glBindVertexArray( sphereVao );
			gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
			gl.glBindVertexArray( 0 );

			if ( onlyHighlights )
			{
				sphereTargetHighlightProg.use( context );
				gl.glBindVertexArray( sphereTargetVao );
				gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
				gl.glBindVertexArray( 0 );
			}
		}
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, final Cylinders cylinders,
			final int highlightIndex,
			final float r0, final float r1,
			final boolean onlyHighlights )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );
		hotloadShader();

		final Matrix3f itvm = vm.invert( new Matrix4f() ).transpose().get3x3( new Matrix3f() );

		if ( onlyHighlights )
		{
			cylinderHighlightProg.getUniformMatrix4f( "pvm" ).set( pvm );
			cylinderHighlightProg.getUniformMatrix4f( "vm" ).set( vm );
			cylinderHighlightProg.getUniformMatrix3f( "itvm" ).set( itvm );
			cylinderHighlightProg.getUniform2f( "radii" ).set( r0, r1 );
			highlight_stuff( pvm, vm, itvm,
					cylinderHighlightProg.getUniform1f( "highlight_f" ),
					cylinderHighlightProg.getUniform1f( "highlight_k" ) );
			cylinderHighlightProg.setUniforms( context );

			sphereHighlightProg.getUniformMatrix4f( "pvm" ).set( pvm );
			sphereHighlightProg.getUniformMatrix4f( "vm" ).set( vm );
			sphereHighlightProg.getUniformMatrix3f( "itvm" ).set( itvm );
			sphereHighlightProg.getUniform1f( "radius" ).set( r0 );
			highlight_stuff( pvm, vm, itvm,
					sphereHighlightProg.getUniform1f( "highlight_f" ),
					sphereHighlightProg.getUniform1f( "highlight_k" ) );
			sphereHighlightProg.setUniforms( context );

			sphereTargetHighlightProg.getUniformMatrix4f( "pvm" ).set( pvm );
			sphereTargetHighlightProg.getUniformMatrix4f( "vm" ).set( vm );
			sphereTargetHighlightProg.getUniformMatrix3f( "itvm" ).set( itvm );
			sphereTargetHighlightProg.getUniform1f( "radius" ).set( r1 );
			highlight_stuff( pvm, vm, itvm,
					sphereTargetHighlightProg.getUniform1f( "highlight_f" ),
					sphereTargetHighlightProg.getUniform1f( "highlight_k" ) );
			sphereTargetHighlightProg.setUniforms( context );
		}
		else
		{
			cylinderProg.getUniformMatrix4f( "pvm" ).set( pvm );
			cylinderProg.getUniformMatrix4f( "vm" ).set( vm );
			cylinderProg.getUniformMatrix3f( "itvm" ).set( itvm );
			cylinderProg.getUniform1i( "highlight" ).set( highlightIndex );
			cylinderProg.getUniform2f( "radii" ).set( r0, r1 );
			highlight_stuff( pvm, vm, itvm,
					cylinderProg.getUniform1f( "highlight_f" ),
					cylinderProg.getUniform1f( "highlight_k" ) );
			cylinderProg.setUniforms( context );

			sphereProg.getUniformMatrix4f( "pvm" ).set( pvm );
			sphereProg.getUniformMatrix4f( "vm" ).set( vm );
			sphereProg.getUniformMatrix3f( "itvm" ).set( itvm );
			sphereProg.getUniform1i( "highlight" ).set( highlightIndex );
			sphereProg.getUniform1f( "radius" ).set( r0 );
			highlight_stuff( pvm, vm, itvm,
					sphereProg.getUniform1f( "highlight_f" ),
					sphereProg.getUniform1f( "highlight_k" ) );
			sphereProg.setUniforms( context );
		}

		instanceArrays.get( cylinders ).draw( gl, context, onlyHighlights );
	}

	public static void highlight_stuff( Matrix4fc pvm, Matrix4fc vm, Matrix3fc itvm, final Uniform1f highlight_f, final Uniform1f highlight_k )
	{
		final int viewportWidth = 800; // TODO!

		final Matrix4f ipvm = pvm.invert( new Matrix4f() );
		final float dx = ( float ) ( 2.0 / viewportWidth );

		final Vector4f adx = vm.transform( ipvm.transform( new Vector4f( dx, 0, -1, 1 ) ) );
		final Vector4f cdx = vm.transform( ipvm.transform( new Vector4f( dx, 0,  1, 1 ) ) );
		adx.div( adx.w() );
		cdx.div( cdx.w() );

		final float sNear = adx.x;
		final float sFar = cdx.x;
		final float ac = cdx.z - adx.z;
		final float f = ( sFar - sNear ) / ac;
		final float k = sNear - f * adx.z;

		final float s = ( float )Math.sqrt( itvm.m00() * itvm.m00() + itvm.m01() * itvm.m01() + itvm.m02() * itvm.m02() );

		highlight_f.set( s * f );
		highlight_k.set( s * k );
	}

}
