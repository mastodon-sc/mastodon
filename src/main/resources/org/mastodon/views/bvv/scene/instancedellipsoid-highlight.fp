in vec3 Normal;
in vec3 FragPos;
in vec3 ObjectColor;

out vec4 fragColor;

void main()
{
	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);

	fragColor = vec4(ObjectColor, 1) * pow(cos(1.571 * dot(viewDir, norm) + 2.5), 8);
}

