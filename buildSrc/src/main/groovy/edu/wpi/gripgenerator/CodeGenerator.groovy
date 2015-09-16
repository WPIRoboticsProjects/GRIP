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
    def text
    def dest
    def removeExisting

    @TaskAction
    def runAction(){
        println "I'm running! Woot!!!"
        println text
        println dest

        //Get the target directory
        LinkedHashSet destSet = dest;
        String targetDirectoryString = destSet.getAt(0).toString()

        //Generate all of the output units
        Map<String, CompilationUnit> files = FileParser.testRead();


        File targetDirectory = new File(targetDirectoryString);

        if(removeExisting) targetDirectory.deleteDir();

        for(String fileName : files.keySet()){
            CompilationUnit fileUnit = files.get(fileName);
            File packageDir = new File(targetDirectory, "/" + fileUnit.package.getName().toString().replace('.', '/').replace(' ', '').replace(';', ''));
            if(packageDir.exists()){
                println "The file " + fileName + " already exists";
            } else {
                packageDir.mkdirs();
            }
            new File(packageDir, fileName + ".java").write(fileUnit.toString());
        }
    }

//    public generate( project, String packageName, String className ) {
//        // Where to write the classes
//        File targetDirectory = new File( project.build.directory + '/generated-sources/groovy' )
//
//        // The directory to write the source to
//        File packageDir = new File( targetDirectory, packageName.replace( '.', '/' ) )
//
//        // Now to create our enum
//        def out = []
//        out<<'package '+packageName+';\n'
//        out<<'public enum '+className+' {\n'
//
//        // We have four suits
//        def suit = [ 'D', 'S', 'H', 'C']
//
//        // Each suit has A, 2-9, T, J, Q & K
//        def rank = [ 'A', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K' ]
//
//        // For each suit, write an entry for each card
//        suit.eachWithIndex {
//            s, i -> def indent = i==0 ? '    ' : ',\n    '
//                rank.each {
//                    r -> out<< indent + s + r
//                        indent = ','
//                }
//        }
//
//        // Mark the end of the enum list
//        out<<';\n'
//
//        // Finish the enum class
//        out<<'}\n'
//
//        // Convert the array into a string
//        StringBuilder sb = new StringBuilder()
//        out.each { sb.append(it) }
//
//        // Now write the source, ensuring the directory exists first
//        packageDir.mkdirs()
//        new File( packageDir, className + ".java" ).write( sb.toString() );
//    }
}
