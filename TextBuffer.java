public class TextBuffer {
    private class Node {
        char ch;
        Node next;

        Node(char ch) {
            this.ch = ch;
            this.next = null;
        }
    }
    private Node head;  
    private int length; 

    public TextBuffer() {
        head = null;
        length = 0;
    }

    public void insertAt(int pos, char ch) {
        if (pos < 0 || pos > length) return;

        Node newNode = new Node(ch);

        if (pos == 0) {
            newNode.next = head;
            head = newNode;
        } else {
            Node temp = head;
            for (int i = 0; i < pos - 1; i++) {
                temp = temp.next;
            }
            newNode.next = temp.next;
            temp.next = newNode;
        }

        length++;
    }

    public char deleteAt(int pos) {
        if (pos < 0 || pos >= length || head == null) return '\0';

        char deletedChar;

        if (pos == 0) {
            deletedChar = head.ch;
            head = head.next;
        } else {
            Node temp = head;
            for (int i = 0; i < pos - 1; i++) {
                temp = temp.next;
            }
            deletedChar = temp.next.ch;
            temp.next = temp.next.next;
        }

        length--;
        return deletedChar;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        Node temp = head;
        while (temp != null) {
            sb.append(temp.ch);
            temp = temp.next;
        }
        return sb.toString();
    }

    public int length() {
        return length;
    }
}
