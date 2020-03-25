package com.jyroscope;

public interface Link<T> {

    public Class<? extends T> getType();
    public void handle(T message);
    public void setLatched(boolean isLatched);
    
}
