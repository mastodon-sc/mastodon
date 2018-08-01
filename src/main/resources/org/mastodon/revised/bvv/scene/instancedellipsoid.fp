in vec3 normal;

out vec4 fragColor;

void main()
{
	vec3 lightDir = normalize( vec3( -1, 0, 0 ) );
	vec3 lightColor = vec3( 1, 1, 0.5 );
	vec3 ambient = vec3( 0, 0, 0.5 );

	float diff = max( dot( normal, lightDir ), 0.0 );
	vec3 diffuse = diff * lightColor;

    fragColor = vec4( diffuse + ambient, 1 );
//    fragColor = vec4( diffuse, 1 );
//    fragColor = vec4( normal, 1 );
}
