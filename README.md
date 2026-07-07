# Gradle Lefthook Plugin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The Gradle Lefthook Plugin integrates [Lefthook](https://github.com/evilmartians/lefthook), a fast and powerful Git hooks manager, directly into your Gradle projects. It automates downloading, configuring, and installing Lefthook without requiring local installations on developer machines.

---

## ✨ Features

- **Zero Local Dependency**: Automatically downloads the correct, versioned Lefthook binary for the running OS and CPU architecture.
- **Gradle-managed Configuration**: Define your Lefthook hooks directly in your `build.gradle` using Groovy maps.
- **Dynamic Script Auto-Download**: Download hook scripts from HTTP/HTTPS URLs or extract them from the classpath automatically during installation.
- **Offline / Multi-Platform Cache**: Download binaries for all supported platforms (Linux, macOS, Windows) at once for committed repositories or CI/CD pipelines.

---

## 📋 Requirements

- **Java**: 11 or higher
- **Gradle**: 5.0 or higher
- **Git**: A git repository initialized in the project directory

---

## 🚀 Installation

Apply the plugin to your `build.gradle`:

```groovy
plugins {
    id 'com.fizzpod.lefthook' version '0.3.0'
}
```

---

## ⚙️ Configuration

Configure the plugin using the `lefthook` extension block in your `build.gradle`:

```groovy
lefthook {
    // Enable or disable automatic installation of Git hooks
    autoInstall = false // Default is false

    // Name of the Gradle task that triggers automatic hook installation when autoInstall is true
    autoTaskName = "assemble" // Default is "assemble"

    // Version of Lefthook to download (or "latest" to resolve the latest tag from GitHub)
    version = "latest" // Default is "latest"

    // Custom configuration commands to run/export in the .lefthookrc shell wrapper
    rc = "export FOO=bar" // Default is ""

    // Directory where the Lefthook binaries, version info, and .lefthookrc are stored
    location = file(".lefthook") // Default is ".lefthook"

    // The GitHub repository to fetch Lefthook binaries from
    repository = "evilmartians/lefthook" // Default is "evilmartians/lefthook"

    // Optional GitHub personal access token to authenticate API calls (prevents rate limiting in CI)
    // Default is resolved from the GITHUB_TOKEN or LEFTHOOK_GITHUB_TOKEN environment variables.
    githubToken = "" 


    // Lefthook hook configuration. Maps directly to the generated lefthook.yml
    config = [
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
                // Automatically download a helper commit-msg script
                { install "https://raw.githubusercontent.com/joaobsjunior/sh-conventional-commits/16dbdae09db941718750db62d4fdbd737da7784e/commit-msg" }: [
                    "runner": "bash"
                ]
            ]
        ]
    ]
}
```

### 📦 Dynamic Script Downloader

The `config` block allows you to download or extract scripts dynamically when Git hooks are installed. By using a closure key:
* `{ install "https://example.com/script.sh" }` - Downloads the script via HTTP/HTTPS.
* `{ install "classpath:/scripts/my-script.sh" }` - Extracts the script from your Gradle build classpath.

The downloaded/extracted scripts are stored under the respective hook's directory (e.g. `.lefthook/commit-msg/`) and automatically marked as executable.

---

## 🏃 Tasks

The plugin registers the following tasks under the **Lefthook** group:

|           Task           |                                                             Description                                                              |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `lefthookResolveVersion` | Queries GitHub to resolve the concrete version for `"latest"` (cached with a 24-hour TTL in `version.txt`).                          |
| `lefthookDownload`       | Downloads and installs the versioned Lefthook binary matching the current OS and CPU architecture.                                   |
| `lefthookDownloadAll`    | Downloads and caches Lefthook binaries for all supported platforms (Linux x86_64/ARM64, macOS x86_64/ARM64, Windows x86_64).         |
| `lefthookRc`             | Generates the `.lefthookrc` environment shell wrapper (which points to the managed binary path and injects the `rc` string).         |
| `lefthookLocal`          | Generates the project's root `lefthook-local.yml` file pointing to `.lefthookrc`.                                                    |
| `lefthookYml`            | Converts the configured `config` map into YAML and writes it to the root `lefthook.yml`.                                             |
| `lefthookInstall`        | Runs the managed Lefthook binary to install hooks into `.git/hooks/`. (Depends on `lefthookRc`, `lefthookLocal`, and `lefthookYml`). |
| `lefthookVersion`        | Runs `lefthook version` and prints output to console.                                                                                |
| `lefthookHelp`           | Runs `lefthook help` and prints output to console.                                                                                   |

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes. Ensure that your code adheres to the project's coding standards and includes appropriate tests.

---

## 📄 License

This project is licensed under the Apache 2.0 License. See the [LICENSE](file:///workspace/LICENSE) file for details.

---

## 💡 Acknowledgments

- [Lefthook](https://github.com/evilmartians/lefthook) - The ultra-fast Git hooks manager that powers this plugin.

