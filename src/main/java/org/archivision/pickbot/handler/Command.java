package org.archivision.pickbot.handler;

import lombok.Getter;

@Getter
public enum Command {
    START_ROUND("/start", "<назва_раунду> - Розпочати новий раунд голосування", false),
    ADD_PLACE("/add", "<назва_місця>, <назва_іншого_місця> - Додати місця до активного раунду", false),
    LIST_PLACES("/list", "Cписок місць у поточному раунді", false),
    VOTE("/vote", "<назва_місця_або_індекс> - Проголосувати за місце", false),
    END_ROUND("/end", "Завершити раунд голосування", false),
    ROUNDS("/rounds", "Показати всі раунди", false),
    ROUND("/round", "<індекс_раунду> - Показати інформацію про конкретний раунд", false),
    INFO("/info", "Детальна інформація про команди бота", false),
    REMOVE("/remove", "round <індекс_раунду_або_назва> - Видалити раунд \n/remove all rounds - Видалити всі раунди \n/remove place <індекс_раунду_або_назва> <індекс_місця_або_назва> - Видалити місце", true),
    RENAME("/rename", "round <індекс_раунду_або_назва> <нова_назва> - Перейменувати раунд \n/rename place <індекс_раунду_або_назва> <індекс_місця_або_назва> <нова назва> - Перейменувати місце", true),
    ADMIN("/admin", "Команди адміна", false);

    private final String name;
    private final String description;
    private final boolean isAdminCommand;

    Command(String name, String description, boolean isAdminCommand) {
        this.name = name;
        this.description = description;
        this.isAdminCommand = isAdminCommand;
    }
}
