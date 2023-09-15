package org.mastodon.mamut;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.app.logging.MastodonLogger;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

public class MastodonLoggerExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		EventQueue.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Context context = new Context();
					final ProjectModel projectModel = ProjectLoader.open( "/home/arzt/devel/mastodon/mastodon/src/test/resources/org/mastodon/mamut/examples/tiny/tiny-project.mastodon", context );
					final MainWindow frame = new MainWindow( projectModel );
					frame.setVisible( true );

					// Send some messages.
					projectModel.getWindowManager().toggleLog();
					final MastodonLogger logger = projectModel.logger();
					final MastodonLogger loggerSource1 = logger.subLogger( "the frame" );
					final MastodonLogger loggerSource2 = logger.subLogger( "another one" );
					logger.info( "Hey man! " );
					loggerSource1.info( "Check this! " );

					final Timer timer1 = new Timer();
					final TimerTask t1 = new TimerTask()
					{

						private final AtomicInteger ai = new AtomicInteger( 0 );

						@Override
						public void run()
						{
							loggerSource1.setProgress( ai.getAndIncrement() / 100. );
							if ( ai.get() > 100 )
							{
								loggerSource1.error( "Oh no! I finished last!" );
								timer1.cancel();
							}
						}
					};
					loggerSource1.setStatus( "Doing stuff" );
					timer1.scheduleAtFixedRate( t1, 100, 50 );

					final Timer timer2 = new Timer();
					final TimerTask t2 = new TimerTask()
					{

						private final AtomicInteger ai = new AtomicInteger( 0 );

						@Override
						public void run()
						{
							loggerSource2.setProgress( ai.getAndIncrement() / 100. );
							if ( ai.get() > 100 )
							{
								loggerSource2.info( "Other stuff done too." );
								timer2.cancel();
							}
						}
					};
					loggerSource2.setStatus( "Doing later but faster" );
					timer2.scheduleAtFixedRate( t2, 1000, 20 );
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}
}
