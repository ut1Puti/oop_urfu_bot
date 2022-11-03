package socialnetworks.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.groups.GroupIsClosed;
import com.vk.api.sdk.objects.wall.WallpostFull;
import database.GroupsStorage;
import httpserver.server.HttpServer;
import socialnetworks.socialnetwork.AbstractSocialNetwork;
import socialnetworks.socialnetwork.groups.NoGroupException;
import socialnetworks.socialnetwork.oAuth.SocialNetworkAuthException;
import socialnetworks.vk.groups.VkGroups;
import socialnetworks.vk.oAuth.VkAuth;
import socialnetworks.vk.wall.VkPostsParser;
import socialnetworks.vk.wall.VkWall;
import socialnetworks.socialnetwork.groups.SubscribeStatus;
import socialnetworks.socialnetwork.SocialNetwork;
import socialnetworks.socialnetwork.SocialNetworkException;
import user.BotUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Класс обрабатывающий запросы пользователя к Vk API
 *
 * @author Кедровских Олег
 * @author Щеголев Андрей
 * @version 2.2
 * @see SocialNetwork
 */
public class Vk extends AbstractSocialNetwork<Group, WallpostFull, BotUser, ServiceActor, Actor> {
    /**
     * Поле клиента для соединения с vk api
     */
    private static final TransportClient transportClient = new HttpTransportClient();
    /**
     * Поле для взаимодействия с vk java sdk
     */
    private static final VkApiClient vkApiClient = new VkApiClient(transportClient);
    /**
     * Поле пользователя приложения в vk
     */
    private final ServiceActor vkApp;

    /**
     * Конструктор - создает экземпляр класса
     */
    public Vk() {
        super(
                new VkAuth(vkApiClient, HttpServer.getInstance(), "src/main/resources/anonsrc/vk_config.json"),
                new VkGroups(vkApiClient),
                new VkWall(vkApiClient)
        );
        vkApp = oAuth.createAppActor();
    }

    /**
     * Метод обертка возвращающий ссылку для аутентификации
     *
     * @return ссылку для аутентификации, если сервер недоступен, то эта ссылка {@code null}
     * @see VkAuth#getAuthUrl()
     */
    @Override
    public String getAuthUrl() {
        return oAuth.getAuthUrl();
    }

    /**
     * Метод для асинхронного создания пользователя
     *
     * @param userSystemId id пользователя в системе
     * @return {@code CompletableFuture<User>}, который выполняет логику создания пользователя,
     * посмотреть ее можно в метода {@link socialnetworks.vk.oAuth.VkAuth#createBotUser(String)}
     */
    public CompletableFuture<BotUser> createBotUserAsync(String userSystemId) {
        return CompletableFuture.supplyAsync(() -> oAuth.createBotUser(userSystemId));
    }

    /**
     * Метод получающий ссылку на группу в vk найденную по {@code userReceivedGroupName}
     *
     * @param userReceivedGroupName Название группы
     * @param userCallingMethod     пользователь вызвавший метод
     * @return возвращает ссылку на группу в vk
     * @throws NoGroupException           возникает если не нашлась группа по заданной подстроке
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     * @see VkGroups#searchGroup(String, BotUser)
     */
    @Override
    public String getGroupUrl(String userReceivedGroupName, BotUser userCallingMethod)
            throws NoGroupException, SocialNetworkException {
        return VkConstants.VK_ADDRESS + groups.searchGroup(userReceivedGroupName, userCallingMethod).getScreenName();
    }

    /**
     * Метод получающий id группы найденную по {@code userReceivedGroupName}
     *
     * @param userReceivedGroupName Название группы
     * @param userCallingMethod     пользователь вызвавший метод
     * @return возвращает id группы
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     * @throws NoGroupException           возникает если не нашлась группа по заданной подстроке
     * @see VkGroups#searchGroup(String, BotUser)
     */
    @Override
    public String getGroupId(String userReceivedGroupName, BotUser userCallingMethod)
            throws NoGroupException, SocialNetworkException {
        return String.valueOf(groups.searchGroup(userReceivedGroupName, userCallingMethod).getId());
    }

    /**
     * Метод для подписки пользователя(сохранение в базу данных id пользователя в телеграмме и группы)
     *
     * @param userReceivedGroupName строка по которой будет искаться группа, полученная от пользователя
     * @param userCallingMethod     пользователь вызвавший метод
     * @return статус подписки на группу,
     * {@link SubscribeStatus#SUBSCRIBED} - означает что пользователь успешно подписан,
     * {@link SubscribeStatus#ALREADY_SUBSCRIBED} - сообщает, что пользователь уже подписан на эту группу,
     * {@link SubscribeStatus#GROUP_IS_CLOSED} - сообщает, что невозможно подписаться, тк группа закрыта
     * @throws NoGroupException           возникает если не нашлась группа по заданной подстроке
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     * @see VkGroups#searchGroup(String, BotUser)
     * @see GroupsStorage#addInfoToGroup(String, String)
     */
    @Override
    public SubscribeStatus subscribeTo(GroupsStorage groupBase, String userReceivedGroupName, BotUser userCallingMethod)
            throws SocialNetworkException, NoGroupException {
        Group userFindGroup = groups.searchGroup(userReceivedGroupName, userCallingMethod);

        if (userFindGroup.getIsClosed() == GroupIsClosed.CLOSED) {
            return SubscribeStatus.GROUP_IS_CLOSED;
        }

        //TODO synchronize working with subscribers
        boolean isSubscribed = groupBase.addInfoToGroup(userFindGroup.getScreenName(), userCallingMethod.getTelegramId());
        return isSubscribed ? SubscribeStatus.SUBSCRIBED : SubscribeStatus.ALREADY_SUBSCRIBED;
    }

    /**
     * Метод отписывающий пользователя от группы
     *
     * @param groupBase             база данных
     * @param userReceivedGroupName название группы полученное от пользователя
     * @param userCallingMethod     пользователь вызвавший метод
     * @return {@code true} если пользователь был отписан, {@code false} если пользователь не был отписан
     * @throws NoGroupException           возникает если не нашлась группа по заданной подстроке
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     */
    @Override
    public boolean unsubscribeFrom(GroupsStorage groupBase, String userReceivedGroupName, BotUser userCallingMethod)
            throws NoGroupException, SocialNetworkException {
        Group userFindGroup = groups.searchGroup(userReceivedGroupName, userCallingMethod);

        if (userFindGroup.getIsClosed() == GroupIsClosed.CLOSED) {
            return false;
        }

        //TODO synchronize working with subscribers
        return groupBase.deleteInfoFromGroup(userFindGroup.getScreenName(), userCallingMethod.getTelegramId());
    }

    /**
     * Метод для получения последних {@code amountOfPosts} постов со стены по {@code userReceivedGroupName}
     *
     * @param amountOfPosts         - кол-во постов
     * @param userReceivedGroupName - имя группы
     * @param userCallingMethod     - пользователь вызвавший метод
     * @return возвращает последние amountOfPosts постов
     * @throws NoGroupException           возникает если не нашлась группа по заданной подстроке
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     * @throws IllegalArgumentException   возникает при передаче кол-ва постов большего, чем можно получить(max 100).
     *                                    Возникает при вызове пользователем не имеющем доступа к этому методу(пример из vk sdk GroupActor)
     * @see VkGroups#searchGroup(String, BotUser)
     * @see VkWall#getPostsStrings(String, int, Actor)
     */
    @Override
    public List<String> getLastPostsAsStrings(String userReceivedGroupName, int amountOfPosts, BotUser userCallingMethod)
            throws NoGroupException, SocialNetworkException {
        Group userFindGroup = groups.searchGroup(userReceivedGroupName, userCallingMethod);
        return wall.getPostsStrings(userFindGroup.getScreenName(), amountOfPosts, userCallingMethod);
    }

    /**
     * Метод для получения новых постов из группы в базе данных, а также обновляющий дату последнего поста
     *
     * @param groupsStorage   - база данных
     * @param groupScreenName - название группы в базе данных
     * @return список постов в группе в виде строк,
     * {@code Optional.empty()} возникает при ошибках обращения к vk api не связанных с самим api,
     * а также если не были найдены новые посты
     * @throws SocialNetworkException     возникает при ошибке обращения к vk api
     * @throws SocialNetworkAuthException возникает при ошибке аутентификации пользователя
     * @see VkWall#getPosts(String, int, Actor)
     * @see VkPostsParser#parsePosts(List)
     */
    @Override
    public Optional<List<String>> getNewPostsAsStrings(GroupsStorage groupsStorage, String groupScreenName)
            throws SocialNetworkException {
        final int amountOfPosts = 100;
        //TODO synchronize working with lastPostDate
        Optional<Long> optionalLastPostDate = groupsStorage.getGroupLastPostDate(groupScreenName);

        if (optionalLastPostDate.isEmpty()) {
            return Optional.empty();
        }

        long lastPostDate = optionalLastPostDate.get();
        long newLastPostDate = lastPostDate;
        List<WallpostFull> appFindPosts = new ArrayList<>();
        for (WallpostFull appFindPost : wall.getPosts(groupScreenName, amountOfPosts, vkApp)) {
            int appFindPostDate = appFindPost.getDate();

            if (appFindPostDate > lastPostDate) {
                appFindPosts.add(appFindPost);

                if (appFindPostDate > newLastPostDate) {
                    newLastPostDate = appFindPostDate;
                }

            }

        }
        groupsStorage.updateGroupLastPost(groupScreenName, newLastPostDate);
        List<String> vkParsedPosts = VkPostsParser.parsePosts(appFindPosts);
        return vkParsedPosts.isEmpty() ? Optional.empty() : Optional.of(vkParsedPosts);
    }
}