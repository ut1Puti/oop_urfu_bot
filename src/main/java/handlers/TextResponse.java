package handlers;

/**
 * Класс возможных ответов на сообщения пользователя
 * @author Кедровских Олег
 * @version 1.0
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
    public static final String NO_VERIFIED_GROUP = "Я не смог найти верефицированную группу с таким названием.";
    /** Поле сообщения об остановке бота  */
    public static final String STOP_INFO = "Остановлен.";
    /** Поле сообщения с просьбой перейти по ссылке */
    public static final String AUTH_GO_VIA_LINK = "Перейдите по ссылке, чтобы пройти аутентификацию:\n";
    /** Поле сообщения с просьбой пройти аутентификации через vk  */
    public static final String NOT_AUTHED_USER = "Сначала пройдите аутентификацию с помощью вк. Для этого используйте /start";
    /** Поле сообщения неизвестной команды */
    public static final String UNKNOWN_COMMAND = "Неизвестная команда. Используйте /help, чтобы увидеть доступные";
    /** Поле содерщащее адрес vk */
    public static final String VK_ADDRESS = "https://vk.com/";
}
