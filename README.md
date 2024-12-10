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

---

## Installation

1.	Clone this repository or download the script.
2.	Ensure the following folder structure:
├── Basecode/
 │   └── [Your Android base code template]
├── codegres_projects.xlsx
├── icons/
 │   └── [App-specific icons organized by app name]
________________________________________

---

## Usage

1.	Update the paths in the script for:
o	base_code_path: Path to the base code directory.
o	excel_file_path: Path to the Excel configuration file.
o	output_directory: Path where the generated apps will be saved.

2.	Run the script:

python automate_apps.py

3.	Check the automation.log file for execution details.
________________________________________

---

## Excel File Format


App Name	Package Name	URL	adUnit	interstitialAdId	bannerAdId	isBannerAds	isInterstitialAds	isPortfolio	Repo URL
ExampleApp	com.example.app	https://example.com
ABC123	DEF456	GHI789	true	false	false	https://github.com/...
________________________________________

---

## Output

•	Each app is generated in its own directory under the output_directory.
•	Customizations include:
o	Renamed package structure.
o	Updated settings.gradle and strings.xml.
o	Replaced placeholders in code files.
o	Generated mipmap icons for launcher icons.
•	If a Git repository URL is provided, the app will be pushed to the specified repository.
________________________________________

---

## Logging

All operations are logged to automation.log. Errors, warnings, and successful steps are recorded.
________________________________________

---


## License
This project is licensed under the MIT License. See the LICENSE file for details.
________________________________________

---


## Contributing
Contributions are welcome! Feel free to fork the repository and submit pull requests.
________________________________________

---


## Contact
For queries or issues, reach out to the author at ksrpoovarasan634@gmail.com.

---


## How to Use the `README.md`
- Copy and paste the above content into a file named `README.md`.
- Replace placeholder paths, example configurations, and contact details with your project's actual details.
- Include the `README.md` in the root of your project's Git repository.

---


