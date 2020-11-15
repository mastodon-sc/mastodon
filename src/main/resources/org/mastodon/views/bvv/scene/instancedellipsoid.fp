in vec3 Normal;
in vec3 FragPos;
in vec3 ObjectColor;

out vec4 fragColor;

/*
const vec3 lightColor1 = vec3(1.0, 1.0, 1.0);
const vec3 lightDir1 = normalize(vec3(-0.5, -1, -1));

const vec3 lightColor2 = vec3(1.0, 1.0, 1.0);
const vec3 lightDir2 = normalize(vec3(1, 1, 1));

const vec3 ambient = vec3(0.1, 0.1, 0.1);

const float specularStrength = 1.0;
*/

const vec3 lightColor1 = vec3(0.9, 0.9, 1);
const vec3 lightDir1 = normalize(vec3(0, -0.2, -1));

const vec3 lightColor2 = vec3(1, 0.7, 0.7);
const vec3 lightDir2 = normalize(vec3(1, 1, 0.5));

const vec3 ambient = vec3(0.1, 0.1, 0.1);

const float specularStrength = 1;


void main()
{
	vec3 norm = normalize(Normal);

	float diff1 = max(dot(norm, lightDir1), 0.0);
	float diff2 = max(dot(norm, lightDir2), 0.0);
	vec3 diffuse = diff1 * lightColor1 + diff2 * lightColor2;

	vec3 viewDir = normalize(-FragPos);
	vec3 reflectDir = reflect(-lightDir2, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
	vec3 specular = specularStrength * spec * lightColor2;

	fragColor = vec4((ambient + diffuse + specular) * ObjectColor, 1);
	//	fragColor = vec4(-norm, 1);
}

