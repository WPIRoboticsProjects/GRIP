import NativePlatforms.WIN32
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Generates a dependency on a JavaFX artifact. The artifact name is prepended with `"javafx-"`, so `name` should be
 * something like `"base"` instead of `"javafx-base"` or `"graphics"` vs `"javafx-graphics"`.  This generates a
 * platform-specific dependency, so this should only be used as a `compileOnly` dependency _except_ in the app project,
 * which wants the dependencies as `compile` or `runtime` dependencies so they can be included in the fatjar
 * distribution.
 */
internal fun DependencyHandler.javafx(name: String, platform: NativePlatforms, version: String = "11") =
    when (platform) {
        WIN32 -> add(platform, "edu.wpi.first.openjfx:javafx-$name:$version:${javaFxClassifier(platform)}")
        else -> add(platform, "org.openjfx:javafx-$name:$version:${javaFxClassifier(platform)}")
    }

/**
 * Generates dependencies for all platform-specific configurations on a JavaFX artifact. The artifact name is
 * prepended with `"javafx-"`, so `name` should be something like `"base"` instead of `"javafx-base"` or `"graphics"`
 * vs `"javafx-graphics"`.
 */
fun DependencyHandler.javafx(name: String) {
    forEachPlatform {
        javafx(name = name, platform = it)
    }
}
