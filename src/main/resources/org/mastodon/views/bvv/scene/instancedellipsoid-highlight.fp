in vec3 Normal;
in vec3 FragPos;
in vec3 ObjectColor;
in float fr;

out vec4 fragColor;

void main()
{
	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);

//	fragColor = vec4(ObjectColor, 1) * pow(cos(1.571 * dot(viewDir, norm) - 0.4), 8);
//	fragColor = vec4(ObjectColor, 1) * pow(1 - dot(viewDir, norm), 0.5);
	float pi2 = 1.571;
	float maxa = asin( 1 - 1 / fr);
	float angle = acos(dot(viewDir, norm)) + (pi2-maxa);
//	float angle = min(maxa, acos(dot(viewDir, norm))) + (pi2-maxa);
	fragColor = vec4(ObjectColor, 1) * pow(sin(angle), 20);
}
