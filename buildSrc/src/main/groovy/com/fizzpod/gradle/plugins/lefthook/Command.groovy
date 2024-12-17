/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project

public class Command {

    static def execute = Loggy.wrap({ Map<?,?> x ->
        x = x + Command.run(x.command)
        return x
    })

    static def run = Loggy.wrap({ String command ->
        Loggy.debug("command: {}", command)
        def soutBuilder = new StringBuilder(), serrBuilder = new StringBuilder()
        def proc = command.execute()
        proc.waitForProcessOutput(soutBuilder, serrBuilder)
        proc.waitFor()
        def exitValue = proc.exitValue()
        def sout = soutBuilder.toString()? soutBuilder.toString(): ""
        def serr = serrBuilder.toString()? serrBuilder.toString(): ""
        
        Loggy.debug("stdout: {}", sout.toString())
        Loggy.debug("stderr: {}", serr.toString())
        Loggy.debug("exit: {}", exitValue)
        
        return [exit: exitValue, sout: sout, serr: serr]
    })

}
