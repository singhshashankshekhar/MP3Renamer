Simple MP3 Renamer
This is a lightweight, command-line Java utility that renames an MP3 file based on the song title found in its ID3v1 tag.

Features
No Dependencies: Runs using only standard Java libraries.

Safe Renaming: Sanitizes the extracted title to remove characters that are invalid in filenames.

Simple to Use: Just provide the path to an MP3 file.

How It Works
The program reads the last 128 bytes of the specified MP3 file, which is where the metadata for the ID3v1 standard is stored. It checks for the "TAG" identifier, extracts the 30-byte title field, and then renames the file to match.

Limitations
This tool is designed for simplicity and only supports the ID3v1 tag format. It will not be able to read the more modern ID3v2 tags, which are stored differently. If you run it on a file that only has an ID3v2 tag (or no tag at all), it will report that no title was found.

How to Use
Prerequisites
Java Development Kit (JDK) 11 or higher installed.

Steps
Save the Code

Save the code as MP3Renamer.java.

Compile the Program

Open a terminal or command prompt and navigate to the folder where you saved the file.

Run the Java compiler:

javac MP3Renamer.java

Run the Renamer

Execute the program from the terminal, passing the path to your MP3 file as an argument. Make sure to enclose the path in quotes if it contains spaces.

java MP3Renamer "path/to/your song.mp3"

Example
Before:

You have a file named track01.mp3.

Its ID3v1 title tag is "Bohemian Rhapsody".

Command:

java MP3Renamer "C:\Music\track01.mp3"

Output:

Found Title: "Bohemian Rhapsody"
Successfully renamed 'track01.mp3' to 'Bohemian Rhapsody.mp3'

After:

The file will now be named Bohemian Rhapsody.mp3.
