import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class GuiTextEditor extends JFrame {
    private JTextArea textArea;
    private TextBuffer buffer;
    private Stack<Action> undoStack;
    private Stack<Action> redoStack;
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

    undoButton = new JButton("Undo");
    redoButton = new JButton("Redo");

    undoButton.addActionListener(e -> performUndo());
    redoButton.addActionListener(e -> performRedo());

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
                remove(fb, offset, length);
                insertString(fb, offset, text, attrs);
            }
        });
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            Action last = undoStack.pop();
            isProgrammaticChange = true;

            if (last.type.equals("insert")) {
                buffer.deleteAt(last.position);
                try {
                    textArea.getDocument().remove(last.position, 1);
                } catch (BadLocationException e) {}
            } else if (last.type.equals("delete")) {
                buffer.insertAt(last.position, last.character);
                try {
                    textArea.getDocument().insertString(last.position, String.valueOf(last.character), null);
                } catch (BadLocationException e) {}
            }

            redoStack.push(last);
            isProgrammaticChange = false;
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            Action next = redoStack.pop();
            isProgrammaticChange = true;

            if (next.type.equals("insert")) {
                buffer.insertAt(next.position, next.character);
                try {
                    textArea.getDocument().insertString(next.position, String.valueOf(next.character), null);
                } catch (BadLocationException e) {}
            } else if (next.type.equals("delete")) {
                buffer.deleteAt(next.position);
                try {
                    textArea.getDocument().remove(next.position, 1);
                } catch (BadLocationException e) {}
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