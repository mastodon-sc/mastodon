package tpietzschx.shadergen;

public class DefaultShader extends AbstractShader
{
	public DefaultShader( final String vpCode, final String fpCode )
	{
		super( vpCode, fpCode );
	}

	@Override
	protected String getUniqueName( final String key )
	{
		return key;
	}
}
