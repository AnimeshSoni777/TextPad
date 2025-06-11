# 📝 TextPad (GUI Text Editor)

A lightweight and minimal text editor built with Java Swing that supports **Undo/Redo**, **Open**, and **Save** functionality, along with a custom **TextBuffer** implementation for text manipulation.

---

## Features

- ✅ Rich-text editing area
- 🔁 Undo and Redo operations (keyboard + button)
- 📂 Open text files from local storage
- 💾 Save text content to a `.txt` file
- 🧠 Custom `TextBuffer` and `Stack` implementation (no Java Collections used internally)
- 🪄 Handles text changes via a `DocumentFilter` to track operations
- 🎨 Monospaced, developer-friendly UI design

---

## 📸 Screenshots

![Screenshot 2025-06-11 233836](https://github.com/user-attachments/assets/ad72d9fa-9a29-44db-b57d-f53396e0bf4c)


---

## 📁 Project Structure

<pre> text 📦 TextEditor 
├── Action.java # Represents a text editing action (insert/delete) 
├── Stack.java # Custom stack used for Undo/Redo 
├── TextBuffer.java # Custom linked-list-based text storage 
├── GuiTextEditor.java # Main application window and UI logic 
└── README.md # You're reading it! </pre>


---

## How It Works

- **Undo/Redo**:
  - Tracks each edit (`insert` or `delete`) as an `Action`
  - Stores actions in `undoStack` / `redoStack`
  - Custom logic to reverse or reapply actions with full accuracy

- **Open**:
  - Reads `.txt` files and populates the `JTextArea` and `TextBuffer`

- **Save**:
  - Exports current content from `JTextArea` to a user-selected file

---

## Technologies Used

- Java 8+
- Swing (JFrame, JTextArea, JFileChooser, DocumentFilter)
- Core Java (OOP, custom data structures)

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/gui-text-editor-java.git
   cd gui-text-editor-java
   
2. Compile and run:
   ```bash
   javac *.java
   java GuiTextEditor
   
3. Or open the project in your favorite Java IDE (like IntelliJ or Eclipse) and run GuiTextEditor.java.
