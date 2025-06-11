import java.util.ArrayList;

public class Stack<T> {
    private ArrayList<T> stack;

    public Stack() {
        stack = new ArrayList<>();
    }

    public void push(T item) {
        stack.add(item);
    }

    public T pop() {
        if (isEmpty()) return null;
        return stack.remove(stack.size() - 1);
    }

    public T peek() {
        if (isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void clear() {
        stack.clear();
    }
}
