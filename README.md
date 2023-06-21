# Chess game Back-end

This is the back-end repository for a chess game application built with Kotlin and Spring.

## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Instalation](#Instalation)
* [Usage](*Usage)

## General info
The back-end is responsible for handling game logic, managing game state, validating moves, and facilitating communication between clients.
The Chess game back-end includes the following features:

Authentication: Users can register an account, log in, and authenticate their requests to the server.

Game Management: The back-end handles the creation of new games, joining existing games, and managing the state of ongoing games.

Move Validation: The server validates the legality of moves requested by clients, ensuring they comply with the rules of chess.

Game History: The back-end keeps track of the moves made during each game, allowing players to review the game history and analyze their strategies.

## Technologies
Project is created with:
* Kotlin
* Spring

## Installation
To install and run the Chess game back-end locally, follow these steps:

Clone the repository:

bash
Copy code
git clone https://github.com/your-username/chess-game-backend.git
Navigate to the project directory:

bash
Copy code
cd chess-game-backend
Build the project using Gradle:

bash
Copy code
./gradlew build
Run the application:

bash
Copy code
./gradlew bootRun
The back-end server should now be running on http://localhost:8080.
## Usage
Once the back-end server is running, the front-end client can interact with it to play the chess game.

You can integrate this back-end with the Chess game front-end repository to have a fully functional chess game application.

