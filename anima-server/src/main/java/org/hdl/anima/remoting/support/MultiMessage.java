package org.hdl.anima.remoting.support;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * MultiMessage
 * @author qiuhd
 */
public final class MultiMessage implements Iterable<Object> {

    public static MultiMessage createFromCollection(Collection<Object> collection) {
        MultiMessage result = new MultiMessage();
        result.addMessages(collection);
        return result;
    }

    public static MultiMessage createFromArray(Object... args) {
        return createFromCollection(Arrays.asList(args));
    }

    public static MultiMessage create() {
        return new MultiMessage();
    }

    private final List<Object> messages = new ArrayList<Object>();

    private MultiMessage() {}

    public void addMessage(Object msg) {
        messages.add(msg);
    }

    public void addMessages(Collection<Object> collection) {
        messages.addAll(collection);
    }

    public Collection<Object> getMessages() {
        return Collections.unmodifiableCollection(messages);
    }

    public int size() {
        return messages.size();
    }

    public Object get(int index) {
        return messages.get(index);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Collection<Object> removeMessages() {
        Collection<Object> result = Collections.unmodifiableCollection(messages);
        messages.clear();
        return result;
    }

    public Iterator<Object> iterator() {
        return messages.iterator();
    }
}