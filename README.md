# custom-league-client

You can support me and this project by buying a coffee.

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/hawolt)

## Bugs & Feature Requests

If you are experiences any troubles or would like a feature please open a new Issue and chose the correct template.

You can click [here](https://github.com/hawolt/custom-league-client/issues/new/choose) to open a new issue.

## Dependencies

- [riot-xmpp](https://github.com/hawolt/riot-xmpp)
- [league-of-legends-rtmp](https://github.com/hawolt/league-of-legends-rtmp)
- [league-client-api](https://github.com/hawolt/league-client-api)
- [league-of-legends-rms](https://github.com/hawolt/league-of-legends-rms)

## Discord

Since this code lacks documentation the best help you can get is my knowledge, proper questions can be asked in
this [discord](https://discord.gg/3wknX5gxaW) server, please note that I will not guide you to achieve something or
answer beginner level questions.

## Getting Started

### DISCLAIMER

Using this program may violate the game's terms of service and result in consequences such as access suspension (ban). 
The author of this program disclaims any responsibility for any penalties or actions taken against your 
League of Legends account as a result of using this program. You have been warned. If you are ready to proceed, 
please read the instructions below and use the program responsibly.

### Before proceeding

**Please Note:** The `exe` file included in this repository is purely experimental and may not work correctly or may not
work at all. It is provided for testing and development purposes only. For a stable and reliable experience, it is 
strongly recommended that you follow the instructions below to use the JAR file version of the program.

### 1. Download the JAR file

Go to [releases](https://github.com/hawolt/custom-league-client/releases) and download the JAR file of the program.

### 2. Verify Java 17 installation

Before you can run the program, make sure you have Java 17 installed on your computer. To check if it's already 
installed, open a terminal or command prompt and run the following command:
```sh
java -version
```
If you see output that mentions "17" (e.g., "openjdk version 17"), you're good to go. 
If not, you'll need to install Java 17. (the internet is full of tutorials on how to do this) 

### 3. Run the program
Once you have Java 17 installed and the JAR file downloaded, simply open the JAR file (e.g., `swift-rift-*version*`).

## How to setup the project using IntelliJ

1. Within Intellij select `File` -> `New` -> `Project from Version Control...`
2. Insert `git@github.com:hawolt/custom-league-client.git` for the `URL` field and hit `Clone`
3. IntelliJ should automatically detect the Maven framework, if this is not the case you can rightclick the
   custom-league-client folder in the Project hierarchy and select `Add Framework Support...` then select `Maven`
4. Make sure you are actually using a compatible Java version by selecting `File` -> `Project Structure`, navigate
   to `Project` within `Project Settings` and make sure both `SDK` and `Language level` have Java Version 17 or higher
   selected, hit `OK`
5. To run the Code navigate to `SwingUI/src/main/java/com/hawolt` and rightclick `LeagueClientUI`,
   select `Run LeagueClientUI.main()`

## Contributions

Pull requests are always appreciated, please note that static data sources will not get merged as the data is available
in the local game files, while they might not be present currently they will be at later stages of development, before
writing a larger chunk of code please communicate on Discord if it is needed.
