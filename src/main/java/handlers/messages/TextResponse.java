package handlers.messages;

/**
 * Класс возможных ответов на сообщения пользователя
 *
 * @author Кедровских Олег
 * @author Щеголев Андрей
 * @version 1.4
 */
public class TextResponse {
    /** Поле сообщения о боте и его возможностях */
    public static final String HELP_INFO = """
            Привет, я бот помогающий взаимодействовать с музыкальной индустрией.
            Я могу найти страницу исполнителя, выдав в качестве результата id или ссылку на его группу в вк.
            Чтоб запустить бота используйте /start и следуйте дальнейшим указаниям по аутентификации с помощью вк.
            Команда "/relogin" нужна для повторной аутентификации.
            Чтобы получить ссылку на подтвержденного исполнителя используйте команду 
            "/link имя артиста или его псевдоним".
            Чтобы получить id на подтвержденного исполнителя используйте команду
            "/id имя артиста или его псевдоним".
            чтобы остановить бота используйте 
            "/stop".
            Вы можете вызвать это сообщение еще раз использовав 
            "/help".""";
    /** Поле сообщения об ошибке при аутентификации */
    public static final String AUTH_ERROR = "Ошибка при аутентификации. Повторите позже.";
    /** Поле сооббщения о необходимости обновить токен */
    public static final String UPDATE_TOKEN = "Продлите токен с помощью команды /relogin.";
    /** Поле сообщения об отсутствии подтвержденной группы */
    public static final String NO_GROUP = "Я не смог найти группу с таким названием.";
    /** Поле сообщения об остановке бота  */
    public static final String STOP_INFO = "Остановлен.";
    /** Поле сообщения с просьбой перейти по ссылке */
    public static final String AUTH_GO_VIA_LINK = "Перейдите по ссылке, чтобы пройти аутентификацию:\n";
    /** Поле сообщения с просьбой пройти аутентификации через vk  */
    public static final String NOT_AUTHED_USER = "Сначала пройдите аутентификацию с помощью вк. Для этого используйте /start";
    /** Поле сообщения неизвестной команды */
    public static final String UNKNOWN_COMMAND = "Неизвестная команда. Используйте /help, чтобы увидеть доступные";
    /** Поле сообщения подписки на группу*/
    public static final String SUBSCRIBE = "Вы успешно подписаны на группу!";
    /** Поле сообщения о том, что пользователь уже подписан */
    public static final String ALREADY_SUBSCRIBER = "Вы уже подписаны на эту группу";
    /** Поле сообщения о том, что в группе отсутствуют посты */
    public static final String NO_POSTS_IN_GROUP = "В этой группе нет постов";
    /**  */
    public static final String VK_API_ERROR = """
            Возникла ошибка при обращении к вк.
            Попробуйте еще раз, если ошибка возникнет еще раз, попробуйте позже.""";
}
