import os
import shutil
import pandas as pd
import re
import logging
import os
from PIL import Image
from PIL.Image import Resampling
import os
import shutil
import re
import xml.etree.ElementTree as ET

# Configure logging
logging.basicConfig(
    filename='automation.log',
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    filemode='w'  # Overwrites the log file each time the script runs
)

def initialize_git_repository(project_path, repo_url):
    try:
        logging.info(f"Initializing Git repository for: {project_path}")
        
        commands = [
            ["git", "init"],
            ["git", "add", "."],
            ["git", "commit", "-m", "project initial commit"],
            ["git", "branch", "-M", "main"],
            ["git", "remote", "add", "origin", repo_url],
            ["git", "push", "-u", "origin", "main"]
        ]

        for cmd in commands:
            logging.info(f"Running command: {' '.join(cmd)}")
            subprocess.run(cmd, cwd=project_path, check=True)

        logging.info(f"Successfully set up Git repository and pushed to: {repo_url}")
        print(f"Successfully set up Git repository and pushed to: {repo_url}")
    except subprocess.CalledProcessError as e:
        logging.error(f"Git command failed: {e}")
        print(f"Git command failed: {e}")
    except Exception as e:
        print(f"Error setting up Git repository for {project_path}: {e}")
        logging.error(f"Error setting up Git repository for {project_path}: {e}")

def read_excel(file_path):
    try:
        logging.info(f"Reading Excel file: {file_path}")
        return pd.read_excel(file_path)
    except Exception as e:
        logging.error(f"Error reading Excel file {file_path}: {e}")
        return None

def update_settings_gradle(file_path, app_name):
    """
    Update the `settings.gradle` file with the app name and include the ':app' module.
    """
    try:
        logging.info(f"Updating settings.gradle for app: {app_name}")

        # Create or overwrite the settings.gradle file with the desired content
        with open(file_path, 'w') as file:
            file.write(f"rootProject.name='{app_name}'\n")
            file.write("include ':app'\n")

        logging.info(f"Updated settings.gradle successfully for {app_name}")

    except Exception as e:
        logging.error(f"Error updating settings.gradle for {app_name}: {e}")


def copy_base_code(base_path, target_path):
    try:
        if os.path.exists(target_path):
            logging.warning(f"Existing directory found. Deleting: {target_path}")
            shutil.rmtree(target_path)  # Remove if exists
        shutil.copytree(base_path, target_path)
        logging.info(f"Copied base code to {target_path}")
    except Exception as e:
        logging.error(f"Error copying base code to {target_path}: {e}")
        raise


def rename_package_structure(project_path, old_package_dir, package_name):
    # Convert package name to folder structure using os.path.join
    old_package_dir = os.path.join(project_path, "app", "src", "main", "java", "com", "codegres", "appname")
    new_package_dir = os.path.join(project_path, "app", "src", "main", "java", *package_name.split("."))

    # Log the paths to verify if they are correct
    logging.debug(f"Old package directory: {old_package_dir}")
    logging.debug(f"New package directory: {new_package_dir}")

    # Check if old directory exists
    if not os.path.exists(old_package_dir):
        logging.error(f"Old package directory does not exist: {old_package_dir}")
        return False

    # Check if the new package directory already exists
    if os.path.exists(new_package_dir):
        logging.warning(f"New package directory already exists: {new_package_dir}. Skipping rename.")
        return False

    try:
        # Create necessary intermediate directories if they don't exist
        os.makedirs(os.path.dirname(new_package_dir), exist_ok=True)

        # Rename the old directory to the new one
        shutil.move(old_package_dir, new_package_dir)
        logging.info(f"Renamed directory: {old_package_dir} -> {new_package_dir}")

    except Exception as e:
        logging.error(f"Error renaming directory {old_package_dir} to {new_package_dir}: {e}")
        return False

    return True


def replace_in_files(folder_or_file_path, replacements):
    if os.path.isfile(folder_or_file_path):
        file_paths = [folder_or_file_path]
    else:
        file_paths = [
            os.path.join(root, file)
            for root, _, files in os.walk(folder_or_file_path)
            for file in files
        ]

    for file_path in file_paths:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            updated = False
            for placeholder, value in replacements.items():
                if re.search(placeholder, content):
                    content = re.sub(placeholder, value, content)
                    updated = True
            if updated:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                logging.info(f"Updated file: {file_path}")
        except Exception as e:
            logging.error(f"Error processing file {file_path}: {e}")

def update_webview_url(file_path, url_placeholder, actual_url):
    """
    Update the WebView URL in the MainActivity class file.
    """
    try:
        logging.info(f"Updating WebView URL in {file_path}")

        # Read the file content
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()

        # Replace the placeholder URL with the actual URL
        updated_content = content.replace(url_placeholder, actual_url)

        # Write the updated content back to the file
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(updated_content)

        logging.info(f"Updated WebView URL successfully in {file_path}")

    except Exception as e:
        logging.error(f"Error updating WebView URL in {file_path}: {e}")


def replace_in_xml(file_path, replacements):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        # Loop through all string and bool elements in XML
        for element in root.iter():
            if element.tag == 'string' and element.attrib.get('name') in replacements:
                logging.debug(f"Replacing {element.attrib['name']} with {replacements[element.attrib['name']]}") 
                element.text = replacements[element.attrib['name']]
            elif element.tag == 'bool' and element.attrib.get('name') in replacements:
                logging.debug(f"Replacing {element.attrib['name']} with {replacements[element.attrib['name']]}") 
                # Convert Excel-style booleans to valid XML booleans
                excel_value = replacements[element.attrib['name']].strip().upper()  # Read as string
                if excel_value == "TRUE":
                    element.text = "true"
                elif excel_value == "FALSE":
                    element.text = "false"
                else:
                    logging.warning(f"Unexpected value for {element.attrib['name']}: {excel_value}")
                    element.text = "false"  # Default to false for invalid values

        # Save changes back to the file
        tree.write(file_path, encoding='utf-8', xml_declaration=True)
        logging.info(f"Updated XML file: {file_path}")
    except Exception as e:
        logging.error(f"Error processing XML file {file_path}: {e}")


def replace_in_manifest(file_path, replacements):
    
    try:
        # Read the file content
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        updated = False
        for placeholder, value in replacements.items():
            if re.search(placeholder, content):
                logging.debug(f"Replacing {placeholder} with {value} in {file_path}")
                content = re.sub(placeholder, value, content)
                updated = True

        # Write back to the file if changes were made
        if updated:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            logging.info(f"Updated AndroidManifest.xml: {file_path}")
        else:
            logging.info(f"No changes made to AndroidManifest.xml: {file_path}")

    except Exception as e:
        logging.error(f"Error processing AndroidManifest.xml file {file_path}: {e}")


def generate_mipmap_icons(image_path, output_dir):
    # Check if the image file exists
    if not os.path.exists(image_path):
        print(f"Error: Image file '{image_path}' not found!")
        return

    # Android mipmap dimensions (based on standard launcher icon sizes)
    mipmap_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    # Create output directories if they don't exist
    for folder in mipmap_sizes.keys():
        logging.info(f"Creating folder: {folder}")
        folder_path = os.path.join(output_dir, folder)
        os.makedirs(folder_path, exist_ok=True)

    # Open the input image
    with Image.open(image_path) as img:
        for folder, size in mipmap_sizes.items():
            # Resize the image to the desired size using LANCZOS filter
            resized_img = img.resize((size, size), Resampling.LANCZOS)
            output_file = os.path.join(output_dir, folder, "ic_launcher.png")
            resized_img.save(output_file, format="PNG")
            print(f"Generated: {output_file}")

    print("Mipmap icons generated successfully!")


def copy_images_to_drawable(icons_directory, project_path, app_name):
    # Define the source directory containing the images
    source_directory = os.path.join(icons_directory, app_name)
    
    # Define the destination drawable folder in the Android project
    drawable_directory = os.path.join(project_path, "app", "src", "main", "res", "drawable")

    # Check if the source directory exists
    if not os.path.exists(source_directory):
        print(f"Error: Source directory '{source_directory}' not found!")
        return

    # Create the drawable directory if it doesn't exist
    os.makedirs(drawable_directory, exist_ok=True)

    # Iterate through all files in the source directory
    for filename in os.listdir(source_directory):
        if filename.lower().endswith(('.png', '.jpg', '.jpeg', '.gif', '.bmp')):  # Check for image files
            source_file = os.path.join(source_directory, filename)
            destination_file = os.path.join(drawable_directory, filename)

            # Copy the image file to the drawable folder
            shutil.copy2(source_file, destination_file)
            print(f"Copied: {filename} to {drawable_directory}")

    print("All images copied successfully!")



def automate_apps(base_path, excel_path, output_dir):
    data = read_excel(excel_path)
    if data is None:
        logging.critical("Failed to read Excel file. Exiting.")
        return

    for _, row in data.iterrows():
        try:

            app_name = row['App Name']
            status = row['Status'].strip().lower()
            if status == 'completed':
                logging.info(f"Skipping app creation for {app_name} as the status is 'completed'.")
                continue
                
            package_name = row['Package Name']
            webview_url = row['URL']
            admob_key_ad_unit = row['adUnit']
            admob_key_interstitial = row['interstitialAdId']
            admob_key_banner = row['bannerAdId']
            is_banner_ads = str(row['isBannerAds']).lower()
            is_interstitial_ads = str(row['isInterstitialAds']).lower()
            is_portfolio = str(row['isPortfolio']).lower()
            repo_url = row.get('Repo URL', '').strip()

            # Validate required fields
            if not app_name or not package_name:
                logging.error(f"Missing required data for app: {app_name or 'Unknown'}")
                continue

            # Initialize project path
            project_path = os.path.join(output_dir, app_name)

            # Log start of app creation
            logging.info(f"Starting app creation for: {app_name} ({package_name})")

            # Step 1: Copy base code
            copy_base_code(base_path, project_path)

            # Step 2: Update settings.gradle
            settings_gradle_file = os.path.join(project_path, "settings.gradle")
            update_settings_gradle(settings_gradle_file, app_name)

            # Step 3: Rename package directory
            old_package_dir = os.path.join(
                project_path, "app", "src", "main", "java", "com", "codegres", "appname"
            )
            if not rename_package_structure(project_path, old_package_dir, package_name):
                logging.error(f"Failed to rename package structure for {app_name}")
                continue

            # Step 4: Replace placeholders in all files
            replacements = {
                r'PLACEHOLDER_APP_NAME': app_name,
                r'com\.codegres\.appname': package_name,
                r'PLACEHOLDER_WEBVIEW_URL': webview_url,
                r'PLACEHOLDER_ADMOB_KEY': admob_key_ad_unit,
                r'PLACEHOLDER_ADMOB_KEY': admob_key_interstitial,
                r'PLACEHOLDER_BANNER_AD_ID': admob_key_banner,
                r'PLACEHOLDER_IS_BANNER_ADS': is_banner_ads,
                r'PLACEHOLDER_IS_INTERSTITIAL_ADS': is_interstitial_ads,
                r'PLACEHOLDER_IS_PORTFOLIO': is_portfolio,
            }
            replace_in_files(project_path, replacements)

            # Step 5: Update `strings.xml`
            strings_file = os.path.join(
                project_path, "app", "src", "main", "res", "values", "strings.xml"
            )

            xml_replacements = {
                'app_name': app_name,
                'title': app_name,
                'adUnit': admob_key_ad_unit,
                'interstitialAdId': admob_key_interstitial,
                'bannerAdId': admob_key_banner,
                'isBannerAds': is_banner_ads,
                'isInterstitialAds': is_interstitial_ads,
                'isPortfolio': is_portfolio,
            }

            url_placeholder = "https://google.com"
            main_activity_file = os.path.join(project_path, "app", "src", "main", "java", *package_name.split("."), "MainActivity.java")
            update_webview_url(main_activity_file, url_placeholder, webview_url)


            replace_in_xml(strings_file, xml_replacements)
            logging.info(f"Updated strings.xml for app: {app_name}")

            # Step 6: Update `AndroidManifest.xml`
            manifest_file = os.path.join(project_path, "app", "src", "main", "AndroidManifest.xml")

            manifest_replacements = {
                r'ca-app-pub-\d+~\d+': admob_key_ad_unit  # Replace with the value from Excel
            }
            replace_in_manifest(manifest_file, manifest_replacements)
            logging.info(f"Updated AndroidManifest.xml for app: {app_name}")

            # image_path = os.path.join(project_path, "app", "src", "main", "res", "drawable", "logo.png")
            # output_dir = os.path.join(project_path, "app", "src", "main", "res")

            # logging.info(f"image_path: {image_path}")
            # logging.info(f"output_dir: {output_dir}")


            icons_directory = "H:\\TechoTackle\\Codegres\\icons\\"
            image_file = os.path.join(icons_directory, app_name, "logo.png")
            output_directory = os.path.join(project_path, "app", "src", "main", "res")
            generate_mipmap_icons(image_file, output_directory)

            copy_images_to_drawable(icons_directory, project_path, app_name)

            if repo_url:
                initialize_git_repository(project_path, repo_url)

            logging.info(f"Successfully created app: {app_name}")

        except Exception as e:
            logging.error(f"Error creating app {app_name}: {e}")



# Inputs
base_code_path = "H:\\TechoTackle\\Codegres\\Basecode"
excel_file_path = r"H:\\TechoTackle\\Codegres\\codegres_projects.xlsx"
output_directory = "H:\\TechoTackle\\Codegres\\"

# image_path = r"H:\\TechoTackle\\Codegres\\Basecode\\app\\src\\main\\res\\drawable\\logo.png"
# output_dir = r"H:\\TechoTackle\\Codegres\\Basecode\\app\\src\\main\\res"


automate_apps(base_code_path, excel_file_path, output_directory)
