/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import nebula.test.ProjectSpec

public class LefthookPluginHelperSpec extends ProjectSpec {

    def "resolve empty config"() {
        expect:
        LefthookPluginHelper.resolve(project, [], config) == result 

        where:
        config | result
        null   | [:]
        [:]    | [:]
        ["abc":true]  | ["abc":true]
        ["abc":"def"] | ["abc":"def"]
        ["abc":["xyz":"hjk"]] | ["abc":["xyz":"hjk"]]
        [{"apple"}:"custard"] | ["apple":"custard"]
        ["apple":{"custard"}] | ["apple":"custard"]
        ["abc":[{"xyz"}:"hjk"]] | ["abc":["xyz":"hjk"]]
        ["abc":["xyz":{"hjk"}]] | ["abc":["xyz":"hjk"]]
        ["abc":["xyz":{["hjk":"hyt"]}]] | ["abc":["xyz":["hjk":"hyt"]]]
        ["abc":["xyz":{["hjk":"hyt"]}]] | ["abc":["xyz":["hjk":"hyt"]]]
        ["abc":["xyz":{["hjk":{"hyt"}]}]] | ["abc":["xyz":["hjk":"hyt"]]]
        ["abc":["xyz":{[{"hjk"}:{"hyt"}]}]] | ["abc":["xyz":["hjk":"hyt"]]]

    }


}
