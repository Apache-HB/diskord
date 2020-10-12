package com.serebit.strife.scripting

import com.serebit.strife.BotBuilder
import java.io.File
import java.io.InputStream

private val BotBuilder.scriptingFeature get() = (features.getValue("Scripting") as ScriptingFeature)

private object DummyObject

private val codeSource = File(DummyObject::class.java.protectionDomain.codeSource.location.toURI())
private val classLoader = DummyObject::class.java.classLoader
private val classpath: File = codeSource.parentFile

private fun BotBuilder.requireFeature() = require("Scripting" in features) {
    "The Scripting feature must be installed before any scripts can be applied."
}

/** Reads the input [scriptText] and applies it as a Kotlin script. */
fun BotBuilder.applyScript(scriptText: String) {
    requireFeature()
    scriptingFeature.compileScript(scriptText)
}

/**
 * Reads a script from the [stream] specified. The stream must be resolvable to a valid Kotlin script. It is the
 * caller's responsibility to close the stream after this function finishes reading it.
 */
fun BotBuilder.applyScript(stream: InputStream) {
    requireFeature()
    scriptingFeature.compileScript(stream.readBytes().decodeToString())
}

/**
 * Reads a script from the [file] specified. The file must have a `.strife.kts` file extension, and must be a valid
 * Kotlin script.
 */
fun BotBuilder.applyScript(file: File) {
    requireFeature()
    if (!file.exists()) error("File at path ${file.path} does not exist.")
    scriptingFeature.compileScript(file.readText())
}

/**
 * Reads a script from the [path] specified, resolved relative to the location of the running class or jarfile. The file
 * must have a `.strife.kts` file extension, and must be a valid Kotlin script. This function should be used for reading
 * scripts that are stored at a known path outside of the jarfile.
 */
fun BotBuilder.applyScriptFromClasspath(path: String) = applyScript(classpath.resolve(path))

/**
 * Reads a script from the [path] specified, resolved as a JVM ClassLoader resource. The file must have a `.strife.kts`
 * file extension, and must be a valid Kotlin script. This function should be used for reading scripts that are bundled
 * inside jarfiles.
 */
fun BotBuilder.applyScriptFromResource(path: String) {
    requireFeature()
    classLoader.getResourceAsStream(path)?.use {
        scriptingFeature.compileScript(it.readBytes().decodeToString())
    } ?: error("Failed to read script from nonexistent resource at path $path.")
}
