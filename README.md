# Getting Started

This is the code to my bachelor's thesis, where I utilized the graph database Neo4j to gain knowledge about chess games. 
In this project, the matches from the World Rapid Championships 2024 in Uzbekistan will be analyzed.
### Reference Documentation
This project requires a library, that deals with chess. I opted for chesslib (https://github.com/bhlangonijr/chesslib),
to simulate the games. Additionally, I used the Spring Data Neo4j library to interact with the Neo4j database.

### Run the Application

The main file is `ChessDatabaseApplication.java`. Here the paths to the games and openings are set. The most important part
is the parseGames boolean which indicates whether the games should be parsed or not. If it is set to true, the games will be parsed and stored in the database or the openings.
I recommend to first import the openings and then the games.

### Analysis
The analysis is conducted via python scripts, which are located in the `analysis` folder. There is also the file, which was used for plotting the results.
