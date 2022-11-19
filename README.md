# Java Gaming Lobbies Bot
<p align="center">
    <img src="https://media.discordapp.net/attachments/1036622997865381959/1036623048842944553/Gaming_Lobbies_Bot-logos_transparent.png?width=662&height=662" width="200">
</p>

***Gaming Lobbies Bot*** is a bot written in Java, based on the JDA framework, to facilitate the process of creating temporary gaming lobbies on Discord servers through a dedicated system.

Bot is currently in development, and is not yet ready for public use (**you can test him at the moment by joining one of the few selected servers where the bot is tested or by cloning and launching this repo by yourself).

Mongodb is used to store data, and the bot is currently hosted on a home server.

## Features
- Create a lobby (temporary voice channel)
- Join a lobby by clicking on an appropriate button
- Leave a lobby by clicking on an appropriate button
- Delete a lobby by either clicking on an appropriate button or by leaving it empty for a certain amount of time
- List all lobbies on given server
- Receive DM notifications when a lobby is created (with an activity you are interested in)
- Create embedded announcements for lobbies

## Commands
- `/ping` - to check whether the bot is online
- `/help` - to get a list of all commands
- `/register-to-bot` - to register yourself to the bot
- `/create-lobby` - to create a lobby
- `/register-server` - to register the server to the bot
- `/unregister-server` - to unregister the server from the bot
- `/unregister-from-bot` - to unregister yourself from the bot
- `/change-server-settings` - to change the server settings
- `/show-server-settings` - to show the server bot settings
- `/add-activity` - to add an activity to your followed activities list
- `/remove-activity` - to remove an activity from your followed activities list
- `/get-activities` - to show all activities you are following

## How to run

### Docker

 1. Clone repo
 2. Copy `.example.env` to `.env` and update credentials
 3. Run `docker compose build`
 4. Run `docker compose pull`
 5. Run `docker compose up -d`

*To update bot, pull changes from git repo, then follow points 3, 4 and 5.*

### Bare metal
- Clone the repository
- Create a file named `.env` in the root directory of the project
- Add the following lines to the `.env` file:
```
BOT_TOKEN=[token of your bot]
BOT_PREFIX=[prefix of your bot not slash commands]
DEBUG_MODE=[check if you want to run the bot in debug mode]
```
- Run docker commands in `MongoDocker.txt` file, to start the mongoDB database container
- Run the `Main.java` file
- Enjoy!

## How to create lobby
- Register the server to the bot (if you're the server admin) with `/register-server` command
- Register yourself to the bot with `/register-to-bot` command
- Create a lobby with `/create-lobby` command
- Lobby will be deleted when the host leaves it

## Planned features
- Fix minor and major bugs
- Add more commands
- Web dashboard for your profile and server settings (with Discord OAuth2 for user authentication)
- Add more features to the bot (suggestions are welcome)

## How to contribute
- Fork the repository
- Create a new branch
- Make your changes
- Create a pull request
- Wait for the review

Alternatively, you can create an issue with a feature request or a bug report on the issues page or our Discord community server.

Server link: https://discord.gg/PfbF3x87js

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
