# Gradle Lefthook Plugin

The Gradle Lefthook Plugin is a tool that integrates Lefthook, a powerful Git hooks manager, into your Gradle projects. This plugin simplifies the process of managing Git hooks by allowing you to configure and run Lefthook directly from your Gradle build scripts.

## Features

- Seamless integration of Lefthook with Gradle projects.
- Easy configuration of Git hooks using Gradle tasks.
- Supports all Lefthook features, including parallel execution and custom scripts.
- Automatically installs Lefthook if not already present.

## Requirements

- Java 8 or higher
- Gradle 5.0 or higher
- Lefthook installed on your system (optional, as the plugin can install it for you)

## Installation

To apply the Gradle Lefthook Plugin to your project, add the following to your `build.gradle` file:

```groovy
plugins {
    id 'com.example.lefthook' version '0.1.0'
}
```

## Usage
Once the plugin is applied, you can configure Lefthook in your build.gradle file:
```groovy
lefthook {
  options {
    autoinstall = false // sets autoinstall - defaut is true
  }
  config { // lefthook file as groovy maps. This will be converted to yaml and applied to the .efthho.yml file
    [
      "assert_lefthook_installed": true,
      "pre-commit": [
        "follow": true,
        "commands": [
          "test": [
            "glob": "*.{groovy,java}",
            "run": "./gradlew test"
          ]
        ]
      ],
      "commit-msg": [
        "follow": true,
        "scripts": [
          {install "https://raw.githubusercontent.com/joaobsjunior/sh-conventional-commits/16dbdae09db941718750db62d4fdbd737da7784e/commit-msg"}: [
            "runner": "bash"
          ]
        ]
      ]
    ]
  }
```


## Configuration
The plugin allows you to customize the Lefthook setup through the lefthook extension in your build.gradle:

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes. Ensure that your code adheres to the project's coding standards and includes appropriate tests.

## License
This project is licensed under the Apache 2 License. See the LICENSE file for details.

## Acknowledgments
[Lefthook](https://github.com/evilmartians/lefthook) - The Git hooks manager that this plugin integrates with.

This README provides a comprehensive overview of the project, including installation instructions, usage examples, and contribution guidelines. Adjust the content as necessary to fit the specific details and features of your plugin.