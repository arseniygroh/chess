# ♟️ Chess Computer Game (Java + JavaFX)

A full-featured chess computer game that combines classic gameplay mechanics, a modern graphical interface, and an artificial intelligence system. The program correctly handles all FIDE rules, including such complex elements as castling, en passant, and pawn promotion.

🔗 **Project repository:** [arseniygroh/chess](https://github.com/arseniygroh/chess)

## 🌟 Key Features

### 🤖 Artificial Intelligence and Game Modes
* **Play against the bot:** Choice of difficulty level (Easy, Medium, Hard) — from random moves to deep position analysis.
* **Minimax Algorithm:** The smart computer opponent uses the minimax algorithm with Alpha-Beta pruning to calculate the move tree.
* **Local PvP:** A game for two people on a single computer with automatic board rotation.

### 🌐 Online Mode
* **Lobby and Challenges:** A real-time list of online players and the ability to send game requests (Challenge).
* **Global Rating (ELO):** An ELO rating system, win/loss statistics (Win Rate), and a global leaderboard.
* **Player Profiles:** Login/Register authorization, personalized player cards, and the ability to set custom avatars and profile descriptions.

### 🎮 Game Interface (UI/UX)
* **Controls:** Support for both `Click-to-Move` and `Drag-and-Drop` systems. Visual highlighting of available moves.
* **Time Control:** Choice of time limit per game (5, 10, 15, 30 minutes) with automatic game stoppage.
* **Real-time Analytics:** A "graveyard" of pieces (displaying captured pieces), material advantage calculation, and move history tracking in notation.
* **Review Mode (Review Game):** After a game ends, you can step through all moves backward/forward for decision analysis.

## 🛠 Technology Stack
* **Language:** Java
* **GUI:** JavaFX
* **Networking:** Java Sockets (Client-server architecture with multithreaded connection handling)
* **Data Storage:** File system for the account database and statistics

## 👥 Development Team

The project was implemented by 1st-year students of the Faculty of Informatics at NaUKMA (Software Engineering specialty):

* **Illia Kabysh (Interface Designer):** Responsible for the visual style, development of the difficulty menu, the completed-game review interface (ReplayOverlay), and the music manager.
* **Andrii Zaiats (Algorithm and Network Developer):** Implemented the server architecture, network interaction, and the AI opponent (MinimaxBot, RandomBot, Evaluator) with deep search.
* **Arsenii Hrokh (Rules Developer):** Responsible for the mathematical accuracy of the game model, move legality, implementation of complex rules (castling, en passant), and checkmate/stalemate states.
* **Oleksandr Zaionchkivskyi (Game Loop Developer):** Created the game dynamics, time control (timers), and integrated the board and statistics panel into a unified `GameView`.
