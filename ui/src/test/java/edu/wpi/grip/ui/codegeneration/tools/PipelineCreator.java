package edu.wpi.grip.ui.codegeneration.tools;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.opencv.core.Core;
public class PipelineCreator {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static void compile(String fileName){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		try {
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(PipelineGenerator.codeDir));
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("FileManager could not set output location "+errorBase(fileName));
		}
		compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjects(PipelineGenerator.codeDir.toPath().resolve(fileName).toFile())).call();
		try {
			fileManager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Class load(String fileName){
		try {
			return Class.forName(fileName.replace(".java", ""));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to load class "+errorBase(fileName));
		}
		return null;
	}
	public static Class makeClass(String fileName){
		compile(fileName);
		Class claz = load(fileName);
		return claz;
	}
	public static void clean(String fileName){
		PipelineGenerator.codeDir.toPath().resolve(fileName).toFile().deleteOnExit();
		PipelineGenerator.codeDir.toPath().resolve(fileName.replace(".java", ".class")).toFile().deleteOnExit();
	}
	
	private static String errorBase(String fileName){
		return "for "+fileName;
	}
}
