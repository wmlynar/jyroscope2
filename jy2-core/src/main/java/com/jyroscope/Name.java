package com.jyroscope;

import java.util.*;

/**
 * A node in a hierarchical naming scheme
 * 
 * This class is not thread safe.
 * 
 * @param <P>
 */
public class Name<P> {
    
    public final static char DIVIDER = '/';
    public final static char RELATIVE = '~';

    private volatile P payload;
    private final String segment;
    private final Name<P> parent;
    private final Name<P> root;
    private final HashMap<String, Name<P>> children;
    private final PayloadFactory<P> factory; // This is only set for root nodes

    // Create a root
    public Name(PayloadFactory<P> factory) {
        this.segment = "";
        this.factory = factory;
        this.children = new HashMap<>();
        this.parent = null;
        this.root = this;
    }
    
    private Name(String segment, Name<P> parent, Name<P> root) {
        this.segment = segment;
        this.parent = parent;
        this.root = root;
        this.children = new HashMap<>();
        this.factory = null;
    }
    
    public boolean isRoot() {
        return this == root;
    }
    
    /**
     * 
     * @return parent or null if it is the root
     */
    public Name<P> getParent() {
        return parent;
    }
    
    public void deleteChild(Name<P> child) {
        children.remove(child.getSegmentName());
    }
    
    public Name<P> getChild(String segment) {
        return getChild(segment, true);
    }
    
    public Name<P> getChild(String segment, boolean allowCreate) {
        if (allowCreate && !children.containsKey(segment))
            children.put(segment, new Name<>(segment, this, root));
        return children.get(segment);
    }
    
    public String getSegmentName() {
        return segment;
    }
    
    public void removeChildren() {
        children.clear();
    }
    
    public P set(P payload) {
        P rval = this.payload;
        this.payload = payload;
        return rval;
    }
    
    public P set(P oldPayload, P newPayload) {
        if (this.payload == oldPayload) {
            this.payload = newPayload;
            return newPayload;
        } else
            return this.payload;
    }
    
    public boolean remove(P oldPayload) {
        if (this.payload == oldPayload) {
            this.payload = null;
            return true;
        } else
            return false;
    }
    
    public P peek() {
        return payload;
    }
    
    public P get() {
        P result = payload;
        if (result == null)
            result = set(null, root.factory.newInstance(this));
        return result;
    }

    /**
     * Returns a name relative to the current name (e.g., /a/b/c.parse(/d/e/f) == /a/b/c/d/e/f
     * 
     * @param path the path describing the relative search
     * @param allowCreate true if names can be created in the process
     * @return the child path (or null if allowCreate == false and there is no such name already existing)
     */
    public Name<P> parse(Name<?> path, boolean allowCreate) {
        if (path.isRoot())
            return this;
        
        Name<P> base = parse(path.getParent(), allowCreate);
        
        if (base == null)
            return null;
        
        return base.getChild(path.getSegmentName(), allowCreate);
    }
    
    public Name<P> parse(String base, String path) {
        return parse(base, path, true);
    }
    
    public Name<P> parse(String base, String path, boolean allowCreate) {
        if (path.length() == 0)
            return this;
        if (path.charAt(0) == RELATIVE) {
            Name<P> step = this.parse(base, allowCreate);
            if (step == null)
                return null;
            else
                return step.parse(path.substring(1));
        } else
            return parse(path, allowCreate);
    }
    
    public Name<P> parse(String path) {
        return parse(path, true);
    }
    
    public Name<P> parse(String path, boolean allowCreate) {
        Name<P> current = this;
        int position = 0;
        while (current != null && position < path.length()) {
            if (path.charAt(position) == DIVIDER) {
                current = root;
                position++;
            } else {
                int next = path.indexOf(DIVIDER, position + 1);
                if (next == -1)
                    return current.getChild(path.substring(position, path.length()), allowCreate);
                else {
                    current = current.getChild(path.substring(position, next), allowCreate);
                    position = next + 1;
                }
            }
        }
        return current;
    }
    
    public String toNameString() {
        return toString();
    }
    
    @Override
    public String toString() {
        if (parent == null)
            return String.valueOf(DIVIDER);
        else {
            StringBuffer sb = new StringBuffer();
            toString(sb);
            return sb.toString();
        }
    }
    
    private void toString(StringBuffer sb) {
        if (parent != null) {
            parent.toString(sb);
            sb.append(DIVIDER);
        }
        sb.append(segment);
    }
    
    public Iterable<Name<P>> children() {
        return children.values();
    }
    
    public Iterable<Name<P>> descendants() {
        return new Iterable<Name<P>>() {
            @Override
            public Iterator<Name<P>> iterator() {
                return new Iterator<Name<P>>() {

                    private Name<P> current;
                    private final Stack<Iterator<Name<P>>> iterators;
                    private Iterator<Name<P>> currentIterator;
                    
                    {
                        current = Name.this;
                        iterators = new Stack<>();
                        currentIterator = current.children.values().iterator();
                        iterators.push(null);
                    }
                    
                    private void findNext() {
                        while (currentIterator != null) {
                            if (currentIterator.hasNext()) {
                                current = currentIterator.next();
                                
                                if (current.children.size() > 0) {
                                    iterators.push(currentIterator);
                                    currentIterator = current.children.values().iterator();
                                }
                                break;
                                
                            } else {
                                currentIterator = iterators.pop();
                            }    
                        }
                    }
                    
                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }
        
                    @Override
                    public Name<P> next() {
                        Name<P> result = current;
                        current = null;
                        findNext();
                        return result;
                    }
        
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public Iterable<P> payloads() {
        return new Iterable<P>() {
            @Override
            public Iterator<P> iterator() {
                return new Iterator<P>() {
                    
                    private P next;
                    private Iterator<Name<P>> names;
                    
                    {
                        names = descendants().iterator();
                        while (names.hasNext() && next == null)
                            next = names.next().payload;
                    }
                    
                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }
        
                    @Override
                    public P next() {
                        P rval = next;
                        next = null;
                        while (names.hasNext() && next == null)
                            next = names.next().payload;
                        return rval;
                    }
        
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    
}
