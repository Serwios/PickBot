package org.archivision.pickbot.handler;

import lombok.Getter;

@Getter
public enum Command {
    START_ROUND("/start", "<назва_раунду> <години> - Розпочати новий раунд голосування з (опціонально) таймаутом у годинах", false, false),
    ADD_PLACE("/add", "<назва_місця>, <назва_іншого_місця> - Додати місця до активного раунду", false, false),
    LIST_PLACES("/list", "Cписок місць у поточному раунді", false, false),
    VOTE("/vote", "<назва_місця_або_індекс> - Проголосувати за місце", false, false),
    END_ROUND("/end", "Завершити раунд голосування", false, false),
    ROUNDS("/rounds", "Показати всі раунди", false, false),
    ROUND("/round", "<індекс_раунду> - Показати інформацію про конкретний раунд", false, false),
    INFO("/info", "Детальна інформація про команди бота", false, false),
    REMOVE("/remove", "round <індекс_раунду_або_назва> - Видалити раунд \n/remove all rounds - Видалити всі раунди \n/remove place <індекс_раунду_або_назва> <індекс_місця_або_назва> - Видалити місце", true, false),
    RENAME("/rename", "round <індекс_раунду_або_назва> <нова_назва> - Перейменувати раунд \n/rename place <індекс_раунду_або_назва> <індекс_місця_або_назва> <нова назва> - Перейменувати місце", true, false),
    ADMIN("/admin", "Команди адміна", false, false),
    ACTIVITY("/activity", "<х> - Кількість користувачів за останні х днів", false, true),
    NOTIFY("/notify", "<повідомлення> - Сповістити всіх користувачів", false, true),
    DISCARD("/discard", "Відмінити голос", false, false);

    private final String name;
    private final String description;
    private final boolean isAdminCommand;
    private final boolean isDeveloperCommand;

    Command(String name, String description, boolean isAdminCommand, boolean isDeveloperCommand) {
        this.name = name;
        this.description = description;
        this.isAdminCommand = isAdminCommand;
        this.isDeveloperCommand = isDeveloperCommand;
    }
}
