package net.trackmate.revised.trackscheme.context;

import java.util.ArrayList;

import javax.swing.JFrame;

public class ContextChooserPlayground
{
	public static void main( final String[] args )
	{
		final ArrayList< ContextProvider< Integer > > providers = new ArrayList<>();
		for ( int i = 0; i < 3; ++i )
		{
			final String name = "Provider " + ( i + 1 );
			providers.add( new ContextProvider< Integer >()
			{
				@Override
				public String getContextProviderName()
				{
					return name;
				}

				@Override
				public void addContextListener( final ContextListener< Integer > listener )
				{
					System.out.println( name + " : addContextListener " + listener );
				}

				@Override
				public void removeContextListener( final ContextListener< Integer > listener )
				{
					System.out.println( name + " : removeContextListener " + listener );
				}
			} );
		}

		final ContextListener< Integer > contextListener = new ContextListener< Integer >()
		{
			@Override
			public void contextChanged( final Context< Integer > context )
			{
				System.out.println( "contextChanged" );
			}
		};

		final ContextChooser< Integer > contextChooser = new ContextChooser<>( contextListener );
		contextChooser.updateContextProviders( providers );
		final ContextChooserPanel< Integer > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		contextChooser.addUpdateListener( contextChooserPanel );

		final JFrame frame = new JFrame( "choose context" );
		frame.add( contextChooserPanel );
		frame.pack();
		frame.setVisible( true );
	}
}
