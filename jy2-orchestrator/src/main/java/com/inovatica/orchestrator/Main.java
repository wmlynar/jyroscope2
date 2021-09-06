package com.inovatica.orchestrator;

import com.github.jy2.di.JyroscopeDi;

public class Main {
    public static void main(String[] args) throws Exception {
		JyroscopeDi di = new JyroscopeDi("orchestrator", args);
        di.create(Orchestrator.class);
        di.start();
    }
}
