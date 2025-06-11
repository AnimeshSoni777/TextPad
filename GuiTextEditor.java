import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class GuiTextEditor extends JFrame {
    private JTextArea textArea;
    private TextBuffer buffer;
    private Stack<Action> undoStack;
    private Stack<Action> redoStack;
    private JButton saveButton;
    private JButton openButton;
    private JButton undoButton;
    private JButton redoButton;
    private boolean isProgrammaticChange = false;

    public GuiTextEditor() {
    setTitle("Simple GUI Text Editor");
    setSize(700, 500);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    buffer = new TextBuffer();
    undoStack = new Stack<>();
    redoStack = new Stack<>();

    textArea = new JTextArea();
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 27));
    JScrollPane scrollPane = new JScrollPane(textArea);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    saveButton = new JButton("Save");
    openButton = new JButton("Open");
    undoButton = new JButton("Undo");
    redoButton = new JButton("Redo");

    saveButton.addActionListener(e -> performSave());
    openButton.addActionListener(e -> performOpen());
    undoButton.addActionListener(e -> performUndo());
    redoButton.addActionListener(e -> performRedo());

    topPanel.add(saveButton);
    topPanel.add(openButton);
    topPanel.add(undoButton);
    topPanel.add(redoButton);

    add(topPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    setupKeyBindings();
    setupDocumentFilter();
}

    private void setupKeyBindings() {
        InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textArea.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save");
        am.put("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                performSave();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        am.put("Undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                performUndo();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        am.put("Redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                performRedo();
            }
        });
    }

    private void setupDocumentFilter() {
        AbstractDocument doc = (AbstractDocument) textArea.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
                if (isProgrammaticChange) {
                    super.insertString(fb, offset, str, attr);
                    return;
                }

                for (int i = 0; i < str.length(); i++) {
                    buffer.insertAt(offset + i, str.charAt(i));
                    undoStack.push(new Action("insert", offset + i, str.charAt(i)));
                }
                redoStack.clear();

                super.insertString(fb, offset, str, attr);
            }

            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (isProgrammaticChange) {
                    super.remove(fb, offset, length);
                    return;
                }

                for (int i = 0; i < length; i++) {
                    char deleted = buffer.deleteAt(offset);
                    undoStack.push(new Action("delete", offset, deleted));
                }
                redoStack.clear();

                super.remove(fb, offset, length);
            }

            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (isProgrammaticChange) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                StringBuilder deletedText = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    deletedText.append(buffer.deleteAt(offset));
                }

                for (int i = 0; i < text.length(); i++) {
                    buffer.insertAt(offset + i, text.charAt(i));
                }

                undoStack.push(new Action("replace", offset, deletedText.toString() + "|" + text));
                redoStack.clear();

                super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    private void  performSave(){
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);

        if(option==JFileChooser.APPROVE_OPTION){
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))){
                writer.write(textArea.getText());
                JOptionPane.showMessageDialog(this, "File saved Succesfully!!!");
            }catch(IOException e){
                JOptionPane.showMessageDialog(this, "! Error Saving File: "+e.getMessage());
            }
        }

    }

    private void performOpen() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
    
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
    
                isProgrammaticChange = true; 
                textArea.setText(sb.toString());
                buffer.setContent(sb.toString()); 
                undoStack.clear();
                redoStack.clear();
                isProgrammaticChange = false;
    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    //made functions to make code clear/readable
    private void bufferInsert(int position, String text) {
        for (int i = 0; i < text.length(); i++) {
            buffer.insertAt(position + i, text.charAt(i));
        }
    }

    private void bufferDelete(int position, int length) {
        for (int i = 0; i < length; i++) {
            buffer.deleteAt(position);
        }
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            Action last = undoStack.pop();
            isProgrammaticChange = true;

            try {
                switch (last.type) {
                    case "insert":
                        bufferDelete(last.position, last.text.length());
                        textArea.getDocument().remove(last.position, last.text.length());
                        break;

                    case "delete":
                        bufferInsert(last.position, last.text);
                        textArea.getDocument().insertString(last.position, last.text, null);
                        break;

                    case "replace":
                        String[] partsUndo = last.text.split("\\|", 2);
                        String oldText = partsUndo[0];
                        String newText = partsUndo[1];

                        bufferDelete(last.position, newText.length());
                        bufferInsert(last.position, oldText);

                        textArea.getDocument().remove(last.position, newText.length());
                        textArea.getDocument().insertString(last.position, oldText, null);
                        break;
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            redoStack.push(last);
            isProgrammaticChange = false;
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            Action next = redoStack.pop();
            isProgrammaticChange = true;

            try {
                switch (next.type) {
                    case "insert":
                        bufferInsert(next.position, next.text);
                        textArea.getDocument().insertString(next.position, next.text, null);
                        break;

                    case "delete":
                        bufferDelete(next.position, next.text.length());
                        textArea.getDocument().remove(next.position, next.text.length());
                        break;

                    case "replace":
                        String[] partsRedo = next.text.split("\\|", 2);
                        String oldText = partsRedo[0];
                        String newText = partsRedo[1];

                        bufferDelete(next.position, oldText.length());
                        bufferInsert(next.position, newText);

                        textArea.getDocument().remove(next.position, oldText.length());
                        textArea.getDocument().insertString(next.position, newText, null);
                        break;
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            undoStack.push(next);
            isProgrammaticChange = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GuiTextEditor().setVisible(true);
        });
    }
}
