package org.mastodon.revised.mamut;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
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
					final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
					windowManager.projectManager.open( project );

					final MastodonMainWindow frame = new MastodonMainWindow( windowManager );
					frame.setVisible( true );

					// Send some messages.
					final MastodonLogger logger = context.getService( DefaultMastodonLogger.class );
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

					final Model model = windowManager.getAppModel().getModel();
					final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
					new FeatureAndTagDialog( frame, model, computerService ).setVisible( true );
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}


}
