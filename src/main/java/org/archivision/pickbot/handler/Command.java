package org.archivision.pickbot.handler;

import lombok.Getter;

@Getter
public enum Command {
    START_ROUND("/start"),
    ADD_PLACE("/add"),
    LIST_PLACES("/list"),
    VOTE("/vote"),
    END_ROUND("/end"),
    ROUNDS("/rounds"),
    ROUND("/round"),
    INFO("/info"),
    CLEAR("/clear"),
    RENAME("/rename"),
    ADMIN("/admin");

    private final String name;

    Command(String name) {
        this.name = name;
    }
}
