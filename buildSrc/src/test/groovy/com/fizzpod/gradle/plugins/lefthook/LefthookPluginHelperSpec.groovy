/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

public class LefthookPluginHelperSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "resolve empty config"() {
        expect:
        LefthookPluginHelper.resolve(temporaryFolder.getRoot(), [], config) == result 

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
