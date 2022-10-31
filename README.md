# Java Gaming Lobbies Bot
<p align="center">
    <img src="https://media.discordapp.net/attachments/1036622997865381959/1036623048842944553/Gaming_Lobbies_Bot-logos_transparent.png?width=662&height=662" width="200">
</p>

[Invitation Link](https://discord.com/api/oauth2/authorize?client_id=1013486009612255263&permissions=8&scope=bot%20applications.commands)

***Gaming Lobbies Bot*** is a bot written in Java, based on the JDA framework, to facilitate the process of creating temporary gaming lobbies on Discord servers through a dedicated system.

Bot is currently in development, and is not yet ready for public use.

Mongodb is used to store data, and the bot will (most likely) be hosted on a VPS.

## Features
- Create a lobby (temporary voice channel)
- Join a lobby
- Leave a lobby
- Delete a lobby by leaving it when its host leaves
- List all lobbies on given server
- Receive DM notifications when a lobby is created (with an activity you are interested in)
- Create embeded announcements for lobbies

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

## How to run
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
- Automatically delete lobbies after a certain amount of time (if host won't join it in time) 
- Add more commands
- Web dashboard for your profile and server settings (with Discord OAuth2)
- Add more features to the bot

## How to contribute
- Fork the repository
- Create a new branch
- Make your changes
- Create a pull request
- Wait for the review

Alternatively, you can create an issue with a feature request or a bug report on the issues page or our Discord community server.

Server link: https://discord.gg/PfbF3x87js

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.txt) file for details
