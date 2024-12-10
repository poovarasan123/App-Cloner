# Automated Android App Generation Script

This Python script automates the creation of Android WebView-based applications using a pre-defined base code template. It reads configuration details from an Excel file, customizes the app's package structure, updates resources, and optionally initializes a Git repository for version control.

---

## Features

- **Excel Integration**: Reads app configurations like name, package, WebView URL, AdMob keys, and more from an Excel file.
- **Package Renaming**: Automatically renames the package structure based on the provided package name.
- **Resource Management**: Updates `strings.xml`, `AndroidManifest.xml`, and other placeholders in the codebase.
- **Icon Generation**: Generates mipmap icons for different screen densities and integrates them into the project.
- **Git Integration**: Optionally initializes a Git repository and pushes the initial commit to a remote repository.
- **WebView Configuration**: Customizes the WebView URL directly in the `MainActivity`.

---

## Prerequisites

1. Python 3.x installed on your machine.
2. Required Python libraries installed:
   ```bash
   pip install pandas pillow
