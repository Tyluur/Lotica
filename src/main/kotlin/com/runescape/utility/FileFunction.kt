package rs.dusk.utility.func.com.runescape.utility

import io.github.classgraph.ClassGraph

/**
 * This class provides functions for file operations on the JVM level
 */
class FileFunction {

    companion object {

        val result = ClassGraph().enableClassInfo()

        inline fun <reified T> getChildClassesOf(): MutableList<T> {
            val kClass = T::class
            val name = kClass.qualifiedName
            val result2 = result.blacklistClasses(name).scan()
            val classes = mutableListOf<T>()
            result2.use { result ->
                val subclasses = result.getSubclasses(name)
                subclasses.forEach {
                    val clazz = result.loadClass(it.name, true).newInstance() as T
                    classes.add(clazz)
                }
            }
            return classes
        }

    }
}