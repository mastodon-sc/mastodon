in vec3 Normal;
in vec3 FragPos;
in vec3 ObjectColor;
in float fr;

out vec4 fragColor;


const vec3 lightColor1 = vec3(1);
const vec3 lightDir1 = normalize(vec3(0, -0.2, -1));

const vec3 lightColor2 = vec3(1);
const vec3 lightDir2 = normalize(vec3(1, 1, 0.5));

const float specularStrength = 1;

vec4 specular(vec3 norm, vec3 viewDir, vec3 lightDir, vec3 lightColor, float shininess, float specularStrength)
{
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
	vec4 specular = specularStrength * spec * vec4(lightColor, 1);
	return specular;
}


void main()
{
//	vec3 ObjectColor = vec3(0.5,1,0.5);
	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);

//	fragColor = vec4(ObjectColor, 1) * pow(cos(1.571 * dot(viewDir, norm) - 0.4), 8);
//	fragColor = vec4(ObjectColor, 1) * pow(1 - dot(viewDir, norm), 0.5);
	float pi2 = 1.571;
	float maxa = asin( 1 - 1 / fr);
	float angle = acos(dot(viewDir, norm)) + (pi2-maxa);
//	float angle = min(maxa, acos(dot(viewDir, norm))) + (pi2-maxa);
	fragColor = vec4(ObjectColor, 1) * pow(sin(angle), 20);

	vec4 l1 = specular( norm, viewDir, lightDir1, lightColor1, 64, 1 );
	vec4 l2 = specular( norm, viewDir, lightDir2, lightColor2, 64, 1 );
	fragColor = vec4(ObjectColor, 1) * pow(sin(angle), 20)
			  + (vec4(ObjectColor, 1) + vec4(0)) * (l1 + l2);
}
