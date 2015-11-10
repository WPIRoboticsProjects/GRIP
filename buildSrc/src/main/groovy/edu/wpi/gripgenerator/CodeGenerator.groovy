// This code provides the "codegen" task, provided by this build item,
// to generate a class named by the user of the build item.

// Create a class to contain our targets.  From inside our class,
// properties in the script's binding are not available.  By doing our
// work inside a class, we are protected against a category of easy
// coding errors.  It doesn't matter if the class name collides with
// other classes defined in other rules.

import com.github.javaparser.ast.CompilationUnit
import edu.wpi.gripgenerator.FileParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CodeGenerator extends DefaultTask {
    /**
     * The destination of the source code.
     */
    def dest
    /**
     * Whether or not the contents of the destination directory should be removed prior to the
     * generated code being added.
     */
    def removeExisting

    @TaskAction
    def runAction() {
        //Get the target directory
        LinkedHashSet destSet = dest;
        String targetDirectoryString = destSet.getAt(0).toString()

        //Generate all of the output units
        Map<String, CompilationUnit> files = FileParser.generateAllSourceCode();


        File targetDirectory = new File(targetDirectoryString);

        if (removeExisting) targetDirectory.deleteDir();

        for (String fileName : files.keySet()) {
            CompilationUnit fileUnit = files.get(fileName);
            File packageDir = new File(targetDirectory, "/" + fileUnit.package.getName().toString().replace('.', '/').replace(' ', '').replace(';', ''));
            if (packageDir.exists()) {
                //println "The file " + fileName + " already exists";
            } else {
                packageDir.mkdirs();
            }
            new File(packageDir, fileName + ".java").write(fileUnit.toString());
        }
    }

}
