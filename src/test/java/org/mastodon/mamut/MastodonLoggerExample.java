package org.mastodon.mamut;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.logging.MastodonLogger;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;
import org.scijava.log.LogSource;

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
					final Context context = new Context();
					final WindowManager windowManager = new WindowManager( context );
					final MamutProject project = new MamutProjectIO().load( "samples/drosophila_crop.mastodon" );
					windowManager.projectManager.open( project );

					final MainWindow frame = new MainWindow( windowManager );
					frame.setVisible( true );

					// Make the logger visible.
					windowManager.toggleLog();

					// Send some messages.
					final MastodonLogger logger = windowManager.getAppModel().getLog();
					final LogSource source1 = logger.getLogSourceRoot().subSource( "the frame" );
					final LogSource source2 = logger.getLogSourceRoot().subSource( "another one" );
					logger.info( "Hey man! " );
					logger.info( "Check this! ", source1 );

					final Timer timer1 = new Timer();
					final TimerTask t1 = new TimerTask()
					{

						private final AtomicInteger ai = new AtomicInteger( 0 );

						@Override
						public void run()
						{
							logger.setProgress( ai.getAndIncrement() / 100., source1 );
							if ( ai.get() > 100 )
							{
								logger.error( "Oh no! I finished last!", source1 );
								timer1.cancel();
							}
						}
					};
					logger.setStatus( "Doing stuff", source1 );
					timer1.scheduleAtFixedRate( t1, 100, 50 );

					final Timer timer2 = new Timer();
					final TimerTask t2 = new TimerTask()
					{

						private final AtomicInteger ai = new AtomicInteger( 0 );

						@Override
						public void run()
						{
							logger.setProgress( ai.getAndIncrement() / 100., source2 );
							if ( ai.get() > 100 )
							{
								logger.info( "Other stuff done too.", source2 );
								timer2.cancel();
							}
						}
					};
					logger.setStatus( "Doing later but faster", source2 );
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
