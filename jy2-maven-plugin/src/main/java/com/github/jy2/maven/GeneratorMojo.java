package com.github.jy2.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.github.jy2.mapper.QdoxAnnotations;
import com.jyroscope.ros.types.RosMessageType;
import com.jyroscope.ros.types.RosTypeConverterFactory;
import com.jyroscope.ros.types.RosTypes;
import com.jyroscope.core.types.ConversionException;
import com.jyroscope.core.types.TypeConverterBuilder;
import com.jyroscope.util.Id;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

	@Parameter(property = "generate.sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
	private File sourceDirectory;

	@Parameter(property = "generate.messagesDirectory", defaultValue = "${project.basedir}/src/main/resources/msgs")
	private File messagesDirectory;

	@Parameter(property = "generate.messagesResourceDirectory", defaultValue = "msgs")
	private String messagesResourceDirectory;

	@Parameter(property = "generate.outputSourceDirectory", defaultValue = "${project.build.directory}/generated-sources")
	private File outputSourceDirectory;

	@Parameter(property = "generate.outputResourceDirectory", defaultValue = "${project.build.directory}/generated-resources/")
	private File outputResourceDirectory;

	@Parameter(property = "generate.initializerClass", defaultValue = "com.jyroscope.dynamic.DefaultConvertersInitializer")
	private String initializerClass;

	@Parameter(property = "generate.writeInitializerClass", defaultValue = "false")
	private boolean writeInitializerClass;
	
	@Parameter(property = "generate.writeResourceFile", defaultValue = "false")
	private boolean writeResourceFile;
	
	@Component
	MavenProject project;

	public void execute() throws MojoExecutionException {

		int idx = initializerClass.lastIndexOf('.');
		String packageName = initializerClass.substring(0, idx);
		String className = initializerClass.substring(idx + 1);

		RosTypes.addMsgSearchPath(messagesDirectory);
		RosTypes.addMsgResourceSearchPath(messagesResourceDirectory);

		String code = "";
		if (!packageName.trim().isEmpty()) {
			code += "package " + packageName + ";\n\n";
		}

		code += "import com.github.jy2.mapper.RosTypeConverters;\n\n" + "@com.jyroscope.annotations.Initializer\n"
				+ "public class " + className + " {\n\n	public static void initialize() {\n";

		// reset id counting
		Id.reset();
		RosTypeConverterFactory cf = new RosTypeConverterFactory();

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(sourceDirectory);
		for (JavaClass type : builder.getClasses()) {
			JavaAnnotation messageAnnotation = null;
			for (JavaAnnotation a : type.getAnnotations()) {
				if (a.getType().getFullyQualifiedName().equals(QdoxAnnotations.MESSAGE)) {
					messageAnnotation = a;
				}
			}
			if (messageAnnotation == null) {
				continue;
			}
			JavaAnnotation primitiveAnnotation = null;
			for (JavaAnnotation a : type.getAnnotations()) {
				if (a.getType().getFullyQualifiedName().equals(QdoxAnnotations.PRIMITIVE)) {
					primitiveAnnotation = a;
				}
			}

			// System.out.println(messageAnnotation.getNamedParameter("value"));

			String rosTypeName = messageAnnotation.getNamedParameter("value").toString();
			String rosTypeName2 = rosTypeName.substring(1, rosTypeName.length() - 1);

			String path = outputSourceDirectory + "/com/jyroscope/dynamic/";
			new File(path).mkdirs();

			// class->ros

			TypeConverterBuilder builder2;
			try {
				builder2 = cf.getWriteRosSource(type);
			} catch (ConversionException e) {
				throw new MojoExecutionException("Error while generating source file", e);
			}
			String name = builder2.getName();
			String source = builder2.getSource();

			String fileName = name;
			int pos = fileName.lastIndexOf('.');
			if (pos > 0) {
				fileName = fileName.substring(pos + 1);
			}
			fileName += ".java";

			try {
				writeFile(path + fileName, source);
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}

			String name1 = name;

			// ros->class

			try {
				builder2 = cf.getReadRosSource(type);
			} catch (ConversionException e) {
				throw new MojoExecutionException("Error while generating source file", e);
			}
			name = builder2.getName();
			source = builder2.getSource();

			fileName = name;
			pos = fileName.lastIndexOf('.');
			if (pos > 0) {
				fileName = fileName.substring(pos + 1);
			}
			fileName += ".java";

			try {
				writeFile(path + fileName, source);
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}

			RosMessageType rosType = RosTypes.getMessageType(rosTypeName2);
			String md5 = rosType.getHash();
			int size = rosType.getSize();
			String definition = StringEscapeUtils.escapeJava(rosType.getDefinition());

			code += "		RosTypeConverters.register(" + rosTypeName + ",\n" + "				"
					+ type.getFullyQualifiedName() + ".class,\n" + "				new " + name + "(),\n"
					+ "				new " + name1 + "(),\n" + "				\"" + md5 + "\",\n" + "				" + size
					+ ",\n" + "				\"" + definition + "\"" + ");\n";

			String primitiveType = null;
			String primitiveType2 = null;
			String rosObjectType = null;
			String className1 = null;
			String className2 = null;
			
			if (primitiveAnnotation != null) {

				primitiveType = primitiveAnnotation.getNamedParameter("value").toString();
				primitiveType2 = primitiveType.substring(0, primitiveType.lastIndexOf("."));
				rosObjectType = type.getFullyQualifiedName();
				
				// primitive converter 1
				className1 = "Convert_" + primitiveType2 + "_to_RosMessage_" + Id.generate();
	
				// primitive converter 2
				className2 = "Convert_RosMessage_to_" + primitiveType2 + "_" + Id.generate();
	
				// QDOX does not expand type from annotation to full class name
				// take the class name from field data
				String primitiveType3 = type.getFieldByName("data").getType().getCanonicalName();
				if(primitiveType3.contains(".")) {
					primitiveType = primitiveType3 + ".class";
					primitiveType2 = primitiveType3;
				} else {
					primitiveType = "java.lang." + primitiveType;
					primitiveType2 = "java.lang." + primitiveType2;
				}

				source = "package " + packageName + ";\n" +
						"\n" + 
						"public class " + className1 + "\n" +
						"		extends com.jyroscope.types.TypeConverter<" + primitiveType2 +", com.jyroscope.ros.RosMessage> {\n" + 
						"	" + name1 + " converter = new " + name1 + "();\n" + 
						"\n" + 
						"	@Override\n" + 
						"	public com.jyroscope.ros.RosMessage convert(" + primitiveType2 + " source) {\n" + 
						"		" + rosObjectType + " msg = new " + rosObjectType + "();\n" + 
						"		msg.data = source;\n" + 
						"		return converter.convert(msg);\n" + 
						"	}\n" + 
						"}";
	
				fileName = className1;
				pos = fileName.lastIndexOf('.');
				if (pos > 0) {
					fileName = fileName.substring(pos + 1);
				}
				fileName += ".java";

				try {
					writeFile(path + fileName, source);
				} catch (IOException e) {
					throw new MojoExecutionException("Error while writing to file", e);
				}

				source = "package " + packageName + ";\n" +
						"\n" + 
						"public class " + className2 + "\n" +
						"		extends com.jyroscope.types.TypeConverter<com.jyroscope.ros.RosMessage," + primitiveType2 + "> {\n" + 
						"	" + name + " converter = new " + name + "();\n" + 
						"\n" + 
						"	@Override\n" + 
						"	public " + primitiveType2 + " convert(com.jyroscope.ros.RosMessage source) {\n" + 
						"		return converter.convert(source).data;\n" + 
						"	}\n" + 
						"}";
	
				fileName = className2;
				pos = fileName.lastIndexOf('.');
				if (pos > 0) {
					fileName = fileName.substring(pos + 1);
				}
				fileName += ".java";

				try {
					writeFile(path + fileName, source);
				} catch (IOException e) {
					throw new MojoExecutionException("Error while writing to file", e);
				}

//				code += "		RosTypeConverters.addPrimitive(\n"
//						+ "				" + primitiveType + ",\n"
//						+ "				" + rosObjectType + ".class"
//						+ ");\n";
				code += "		RosTypeConverters.registerPrimitive(" + rosTypeName + ",\n" + "				"
						+ primitiveType + ",\n" + "				new com.jyroscope.dynamic." + className2 + "(),\n" +
						"				new com.jyroscope.dynamic."
						+ className1 + "());\n";

			}
			
			// single type initializer

			String singleInitializerClassName = type.getFullyQualifiedName().replace('.', '_');
			fileName = singleInitializerClassName + ".java";
			source = "package com.jyroscope.initializers;\n\n" +
					"import com.github.jy2.mapper.RosTypeConverters;\n\n" +
					"public class " + singleInitializerClassName + " {\n" + 
					"	public static void initialize() {\n";
			
			source += "		RosTypeConverters.register(" + rosTypeName + ",\n" + "				"
					+ type.getFullyQualifiedName() + ".class,\n" + "				new " + name + "(),\n"
					+ "				new " + name1 + "(),\n" + "				\"" + md5 + "\",\n" + "				" + size
					+ ",\n" + "				\"" + definition + "\"" + ");\n";
			
			if (primitiveAnnotation != null) {
				
				source += "		RosTypeConverters.registerPrimitive(" + rosTypeName + ",\n" + "				"
						+ primitiveType + ",\n" + "				new com.jyroscope.dynamic." + className2 + "(),\n"
						+ "				new com.jyroscope.dynamic."
						+ className1 + "());\n";
			}
			
			source += "	}\n" + 
			"}";
			
			String pathSingle = outputSourceDirectory + "/com/jyroscope/initializers/";
			new File(pathSingle).mkdirs();
			try {
				writeFile(pathSingle + fileName, source);
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}
			
			// primitive type initializer
			if (primitiveAnnotation != null) {
				String singleInitializerClassName2 = primitiveType2.replace('.', '_');
				fileName = singleInitializerClassName2 + ".java";
				
				source = "package com.jyroscope.initializers;\n\n" +
						"public class " + singleInitializerClassName2 + " {\n" + 
						"	public static void initialize() {\n";
				source += "		" + singleInitializerClassName + ".initialize();\n";
				
				source += "	}\n" + 
				"}";
				
				try {
					writeFile(pathSingle + fileName, source);
				} catch (IOException e) {
					throw new MojoExecutionException("Error while writing to file", e);
				}
			}
			
			// ros type name initializer
			String singleInitializerClassName3 = rosTypeName2.replace('/', '_');
			fileName = singleInitializerClassName3 + ".java";
			
			source = "package com.jyroscope.initializers2;\n\n" +
					"import com.jyroscope.initializers.*;\n\n" +
					"public class " + singleInitializerClassName3 + " {\n" + 
					"	public static void initialize() {\n";
			source += "		" + singleInitializerClassName + ".initialize();\n";
			
			source += "	}\n" + 
			"}";
			
			String pathSingle3 = outputSourceDirectory + "/com/jyroscope/initializers2/";
			new File(pathSingle3).mkdirs();
			try {
				writeFile(pathSingle3 + fileName, source);
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}
			
		}

		code += "	}\n" + "}\n";

		if(writeInitializerClass) {
			// write initializer file
			String path = outputSourceDirectory + "/" + initializerClass.replace(".", "/") + ".java";
			try {
				new File(path).getParentFile().mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				writeFile(path, code);
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}
		}
		
		if(writeResourceFile) {
			// write empty file with name of the class, so jyroscope constructor can scan it
			// and load the class with name of the file and treat it as initilizer
			File outputResourceDirectory2 = new File(outputResourceDirectory + "/jyroscope2");
			outputResourceDirectory2.mkdirs();
			try {
				writeFile(outputResourceDirectory2 + "/" + initializerClass, "");
			} catch (IOException e) {
				throw new MojoExecutionException("Error while writing to file", e);
			}
		}

		// add source folder
		project.addCompileSourceRoot(outputSourceDirectory.getAbsolutePath());

		// add resource folder
		Resource resource = new Resource();
		resource.setDirectory(outputResourceDirectory.toString());
		project.addResource(resource);
	}

	public static void writeFile(String path, String content) throws IOException {
		Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
	}
}
