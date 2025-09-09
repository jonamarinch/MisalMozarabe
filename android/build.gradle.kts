// Mantén este archivo mínimo para evitar conflictos de versiones.
// No declares aquí plugins ni repositorios: todo va en settings.gradle.kts.

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}