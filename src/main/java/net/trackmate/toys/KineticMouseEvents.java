package net.trackmate.toys;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class KineticMouseEvents extends MouseAdapter
{
	private final double dt = 1; // s

	private double zoomSpeed = 0;

	private double zoom = 1;

	private final double lambda = 0.1; // s-1

	private final double minV = 0.1;

	private final double alpha = 0.02;

	private final Timer zoomFriction = new Timer( 10, new ActionListener()
	{

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double dv = -lambda * zoomSpeed * dt;
			zoomSpeed += dv;

			zoom = Math.max( 0.1, zoom + zoomSpeed * dt * alpha );

			frame.repaint();
			if ( Math.abs( zoomSpeed ) < minV )
			{
				zoomFriction.stop();
			}
		}
	} );

	private final Timer positionFriction = new Timer( 10, new ActionListener()
	{

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double dvx = -lambda * vx * dt;
			final double dvy = -lambda * vy * dt;

			vx += dvx;
			vy += dvy;

			x += vx * dt;
			y += vy * dt;

			if ( y < 0 )
			{
				y = 0;
				vy = -vy;
			}
			if ( y + h > height )
			{
				y = height - h;
				vy = -vy;
			}
			if ( x < 0 )
			{
				x = 0;
				vx = -vx;
			}
			if ( x + w > width )
			{
				x = width - w;
				vx = -vx;
			}

//			System.out.println( "vx = " + vx + ", vy = " + vy );// DEBUG

			frame.repaint();
			if ( ( vx * vx + vy * vy ) < minV * minV )
			{
//				System.out.println( "XY Stop" );// DEBUG
				positionFriction.stop();
			}
		}
	} );

	private final TestCanvas cv = new TestCanvas();

	private final JFrame frame;

	private int oX;

	private int oY;

	private int eX;

	private int eY;

	private int x;

	private int y;

	private int w;

	private int h;

	private final int width;

	private final int height;

	private int vx;

	private int vy;

	public KineticMouseEvents( final JFrame frame )
	{
		this.frame = frame;
		cv.addMouseWheelListener( this );
		cv.addMouseListener( this );
		cv.addMouseMotionListener( this );
		width = 600;
		height = 600;
		x = ( int ) ( 0.25 * width );
		y = ( int ) ( 0.25 * height );
	}

	public TestCanvas getCanvas()
	{
		return cv;
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		final int wr = e.getWheelRotation();
		zoomSpeed += dt * wr;
		zoomFriction.restart();
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		oX = e.getX();
		oY = e.getY();
		eX = oX;
		eY = oY;
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		eX = e.getX();
		eY = e.getY();
		vx = ( eX - oX );
		vy = ( eY - oY );
		x += vx;
		y += vy;
		oX = eX;
		oY = eY;
		frame.repaint();
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		positionFriction.restart();
	}

	private class TestCanvas extends JPanel
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void paint( final Graphics g )
		{
			w = ( int ) ( width / 2d * zoom );
			h = ( int ) ( height / 2d * zoom );
			g.drawRect( x, y, w, h );
		}
	}

	public static void main( final String[] args )
	{
		final JFrame frame = new JFrame( "MouseWheel toy" );

		frame.add( new KineticMouseEvents( frame ).getCanvas() );

		frame.setSize( 600, 600 );
		frame.setResizable( false );
		frame.setVisible( true );
	}
}
