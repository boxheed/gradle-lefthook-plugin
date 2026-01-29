/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import spock.lang.Specification

public class LefthookPluginHelperSpec extends Specification {

    def project = Mock(Project)

    def "resolve empty config"() {
        expect:
        LefthookPluginHelper.resolve(project, [], config) == result 

        where:
        config | result
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
