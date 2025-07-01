<<<<<<< HEAD
# Tic Tac Toe Ultimate 🎮

A modern, interactive Tic Tac Toe game built in Java with Swing, featuring smooth animations, AI gameplay, and a beautiful user interface.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0-green?style=for-the-badge)

## ✨ Features

### 🎯 Game Modes
- **Player vs Player**: Classic 2-player mode
- **Player vs AI**: Challenge an intelligent AI opponent
- **Smart AI**: Minimax algorithm with alpha-beta pruning for optimal gameplay

### 🎨 Modern UI/UX
- **Clean Design**: Modern card-based interface with beautiful color palette
- **Smooth Animations**: 60+ FPS animations with easing effects
- **Interactive Elements**: Hover effects, button animations, and visual feedback
- **Responsive Layout**: Well-organized layout with proper spacing and typography

### 🌟 Visual Effects
- **Button Animations**: Scale and color transitions on moves
- **Winning Celebrations**: Confetti effects and rainbow text animations
- **AI Thinking Indicator**: Visual feedback when AI is calculating moves
- **Game Transitions**: Smooth board reset and game state changes

### 📊 Game Features
- **Score Tracking**: Persistent score counting for all game modes
- **Winning Line Highlight**: Animated highlighting of winning combinations
- **Draw Detection**: Proper handling of tie games
- **Game State Management**: Clean state transitions and move validation

## 🚀 Quick Start

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Any Java IDE or command line compiler

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/Nitesh-Kumar-Das/TICTAC-TOE.git
   cd TICTAC-TOE
   ```

2. **Compile the project**
   ```bash
   javac *.java
   ```

3. **Run the game**
   ```bash
   java TicTacToeUI
   ```

### Alternative: Direct Download
Download the source files and compile them in your preferred Java environment.

## 🏗️ Project Structure

```
TICTAC-TOE/
├── TicTacToeUI.java    # Main UI class with animations and event handling
├── GameLogic.java      # Core game logic, win detection, and state management
├── AIBot.java          # AI implementation with minimax algorithm
└── README.md           # Project documentation
```

## 🎮 How to Play

### Basic Gameplay
1. **Start**: Launch the game to see the main interface
2. **Choose Mode**: Toggle between "Player vs Player" and "Player vs AI"
3. **Make Moves**: Click on empty squares to place your mark (X or O)
4. **Win Condition**: Get three marks in a row (horizontal, vertical, or diagonal)
5. **New Game**: Click "New Game" to start fresh or "Reset Score" to clear statistics

### Game Controls
- **Left Click**: Place your mark on an empty square
- **New Game Button**: Start a fresh game
- **Mode Button**: Switch between 2-player and AI modes
- **Reset Score Button**: Clear all game statistics

## 🤖 AI Features

The AI opponent uses advanced algorithms to provide challenging gameplay:

### Intelligence Levels
- **Strategic Play**: Uses minimax algorithm for optimal move selection
- **Immediate Threats**: Prioritizes winning moves and blocking opponent wins
- **Opening Strategy**: Smart first moves (center or corners)
- **Endgame Optimization**: Prefers quicker wins and delays losses

### Technical Implementation
- **Minimax Algorithm**: Complete game tree evaluation
- **Alpha-Beta Pruning**: Optimized search for better performance
- **Position Evaluation**: Strategic assessment of board positions
- **Move Validation**: Ensures all moves are legal and optimal

## 🎨 Design Highlights

### Color Palette
- **Primary**: Dark slate gray (#2D3436)
- **Secondary**: Cool gray (#636E72)
- **Accent**: Turquoise (#00B894)
- **Success**: Mint green (#55EFC4)
- **Warning**: Orange (#FFBE42)
- **Danger**: Coral (#FF7675)

### Typography
- **Title**: Segoe UI Bold, 28px
- **Game Buttons**: Segoe UI Bold, 36px
- **UI Elements**: Segoe UI Regular, 14px
- **Status**: Segoe UI Bold, 18px

### Animations
- **Timing**: 60+ FPS for smooth performance
- **Easing**: Custom easing functions for natural motion
- **Effects**: Scale, glow, sparkle, and confetti animations
- **Performance**: Optimized rendering with targeted repaints

## 🔧 Technical Details

### Architecture
- **Separation of Concerns**: UI, game logic, and AI in separate classes
- **Event-Driven**: Swing event handling for user interactions
- **State Management**: Clean game state transitions and validation
- **Performance**: Optimized animations and rendering

### Key Components

#### TicTacToeUI.java
- Main application window and UI components
- Animation system and visual effects
- Event handling and user interactions
- Swing-based modern interface

#### GameLogic.java
- Core game rules and validation
- Win/draw detection algorithms
- Score tracking and game state
- Move simulation for AI

#### AIBot.java
- Minimax algorithm implementation
- Alpha-beta pruning optimization
- Strategic move evaluation
- Difficulty scaling capability

## 🎯 Game Rules

### Standard Tic Tac Toe Rules
1. **Grid**: 3×3 playing field
2. **Players**: Two players alternate turns (X and O)
3. **Objective**: Get three marks in a row, column, or diagonal
4. **First Move**: X always goes first
5. **Draw**: Game ends in a tie if the board fills without a winner

### Scoring System
- **Win**: +1 point for the winning player
- **Draw**: +1 draw count for both players
- **Loss**: No points awarded
- **Persistent**: Scores maintained across games until reset

## 🚀 Future Enhancements

### Planned Features
- [ ] Multiple difficulty levels for AI
- [ ] Custom player names and avatars
- [ ] Sound effects and music
- [ ] Game statistics and analytics
- [ ] Network multiplayer support
- [ ] Tournament mode
- [ ] Themes and customization options

### Technical Improvements
- [ ] Save/load game states
- [ ] Replay system
- [ ] Performance profiling
- [ ] Cross-platform packaging
- [ ] Mobile-responsive design

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Contribution Guidelines
- Follow existing code style and formatting
- Add comments for complex logic
- Test all changes thoroughly
- Update documentation as needed

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Nitesh Kumar Das**
- GitHub: [@Nitesh-Kumar-Das](https://github.com/Nitesh-Kumar-Das)
- Repository: [TICTAC-TOE](https://github.com/Nitesh-Kumar-Das/TICTAC-TOE)

## 🙏 Acknowledgments

- Java Swing documentation and community
- Minimax algorithm resources and tutorials
- Color palette inspiration from modern UI design
- Animation techniques from web development best practices

## 📊 Stats

![GitHub repo size](https://img.shields.io/github/repo-size/Nitesh-Kumar-Das/TICTAC-TOE)
![GitHub code size](https://img.shields.io/github/languages/code-size/Nitesh-Kumar-Das/TICTAC-TOE)
![Lines of code](https://img.shields.io/tokei/lines/github/Nitesh-Kumar-Das/TICTAC-TOE)

---

**Enjoy playing Tic Tac Toe Ultimate!** 🎉

*Built with ❤️ using Java and Swing*
