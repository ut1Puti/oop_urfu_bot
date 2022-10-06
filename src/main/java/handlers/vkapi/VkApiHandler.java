package handlers.vkapi;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import database.Storage;
import httpserver.HttpServer;
import user.CreateUser;
import user.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Класс обрабатывающий запросы пользователя к Vk API
 *
 * @author Кедровских Олег
 * @author Щеголев Андрей
 * @version 1.4
 */
public class VkApiHandler implements CreateUser {
    /** Поле транспортного клиента */
    private final TransportClient transportClient = new HttpTransportClient();
    /** Поле класс позволяющего работать с Vk SDK Java */
    private final VkApiClient vk = new VkApiClient(transportClient);
    /** Поле сервера получающего токены пользователя и переправляющего пользователей на tg бота */
    private HttpServer httpServer = null;
    /** Поле хранилища данных о группах и пользователях */
    private Storage dataBase = null;
    /** Поле конфигурации vk приложения */
    private final VkAppConfiguration appConfiguration;
    /** Поле класса для взаимодействия с группами через vk api */
    private final VkApiGroups groups;

    /**
     * Конструктор по пути до файла с конфигурацией приложения
     *
     * @param configPath - путь до файла с конфигурацией
     */
    public VkApiHandler(String configPath) {
        appConfiguration = new VkAppConfiguration(configPath);
        groups = new VkApiGroups(vk);
    }

    /**
     * Метод возвращающий ссылку для аутентификации
     *
     * @return ссылку для аутентификации, если сервер недоступен, то это null
     */
    public String getAuthURL() {
        try {
            httpServer = HttpServer.getInstance();
        } catch (IOException e){
            return null;
        }
        return appConfiguration.AUTH_URL;
    }

    /**
     * Метод интерфейса CreateUser создающий пользователя
     * Создается с помощью Vk Java SDK, получая код с сервера
     *
     * @return интерфейс для создания пользователя
     */
    @Override
    public User createUser() {
        String httpRequestGetParameters;
        try {
            httpServer = HttpServer.getInstance();
            httpRequestGetParameters = httpServer.getHttpRequestGetParametrs();
        } catch (IOException e) {
            return null;
        }

        if (httpRequestGetParameters == null){
            return null;
        }

        String authCode = getAuthCodeFromHttpRequest(httpRequestGetParameters);
        try {
            UserAuthResponse authResponse = vk.oAuth()
                    .userAuthorizationCodeFlow(
                            appConfiguration.APP_ID,
                            appConfiguration.CLIENT_SECRET,
                            appConfiguration.REDIRECT_URL,
                            authCode)
                    .execute();
            return new User(authResponse.getUserId(), authResponse.getAccessToken());
        } catch (ApiException | ClientException e) {
            return null;
        }
    }

    /**
     * Метод получающий ссылку на группу в вк
     *
     * @param groupName   - Название группы
     * @param callingUser - пользователя
     * @return возвращает ссылку на группу в вк
     * @throws ApiException - возникает при ошибке обращения к vk api со стороны vk
     * @throws NoGroupException - возникает если не нашлась группа по заданной подстроке
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    public String getGroupURL(String groupName, User callingUser) throws NoGroupException, ClientException, ApiException {
        return VkApiConsts.VK_ADDRESS + groups.searchGroup(groupName, callingUser).getScreenName();
    }

    /**
     * Метод получающий id группы
     *
     * @param groupName   - Название группы
     * @param callingUser - пользователя
     * @return возвращает id группы
     * @throws ApiException - возникает при ошибке обращения к vk api со стороны vk
     * @throws NoGroupException - возникает если не нашлась группа по заданной подстроке
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    public String getGroupId(String groupName, User callingUser) throws NoGroupException, ClientException, ApiException {
        return String.valueOf(groups.searchGroup(groupName, callingUser).getId());
    }

    /**
     * Метод для подписки пользователя(сохранение в базу данных айди и группы)
     *
     * @param groupName   - Название группы
     * @param callingUser - пользователя
     * @return возвращает true - если пользователь только что подписался
     *                    false - если пользователь уже был подписан
     * @throws ApiException - возникает при ошибке обращения к vk api со стороны vk
     * @throws NoGroupException - возникает если не нашлась группа по заданной подстроке
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    public boolean subscribeTo(String groupName, User callingUser) throws ApiException, NoGroupException, ClientException {
        Group userFindGroup = groups.searchGroup(groupName, callingUser);

        if (dataBase == null) {
            dataBase = Storage.getInstance();
        }

        return dataBase.addInfoToGroup(userFindGroup.getScreenName(), callingUser);
    }

    /**
     * Метод получает последние посты из сообщества
     *
     * @param amountOfPosts - кол-во постов
     * @param groupName   - Название группы
     * @param callingUser - пользователя
     * @return текст указанного кол-ва постов, а также изображения и ссылки, если они есть в посте
     * @throws ApiException - возникает при ошибке обращения к vk api со стороны vk
     * @throws NoGroupException - возникает если не нашлась группа по заданной подстроке
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    public Optional<List<String>> getLastPosts(int amountOfPosts, String groupName, User callingUser) throws ApiException, NoGroupException, ClientException {
        Group userFindGroup = groups.searchGroup(groupName, callingUser);
        List<WallpostFull> userFindGroupPosts = vk.wall().get(callingUser)
                .domain(userFindGroup.getScreenName())
                .offset(VkApiConsts.DEFAULT_OFFSET).count(amountOfPosts)
                .execute().getItems();

        int postsCounter = 1;
        List<String> groupFindPosts = new ArrayList<>();
        for (WallpostFull userFindGroupPost : userFindGroupPosts) {
            List<WallpostAttachment> userFindGroupPostAttachments = userFindGroupPost.getAttachments();
            StringBuilder userFindPostTextBuilder = new StringBuilder(userFindGroupPost.getText());
            boolean isNoAttachmentsInPost = false;
            while (userFindGroupPostAttachments == null) {
                List<Wallpost> userFindGroupPostCopy = userFindGroupPost.getCopyHistory();

                if (userFindGroupPostCopy == null) {
                    isNoAttachmentsInPost = true;
                    break;
                }

                userFindGroupPostAttachments = userFindGroupPostCopy.get(VkApiConsts.FIRST_ELEMENT_INDEX)
                                               .getAttachments();
                userFindPostTextBuilder.append("\n").append(userFindGroupPostCopy.get(VkApiConsts.FIRST_ELEMENT_INDEX).getText());
            }

            String postText = "Пост " + postsCounter++ + ") " + userFindPostTextBuilder + "\n";

            if (isNoAttachmentsInPost) {
                groupFindPosts.add(postText);
                continue;
            }

            addAttachmentsToPost(userFindGroup, userFindGroupPostAttachments, userFindPostTextBuilder);
            groupFindPosts.add(postText);
        }
        return groupFindPosts.isEmpty() ? Optional.empty() : Optional.of(groupFindPosts);
    }

    /**
     * Метод добавляющий к посту ссылки на прикрепленные элементы
     * или сообщающий об их наличии, если добавить их невозможно
     *
     * @param userFindGroupPostAttachments - доп. материалы прикрепленные к посту
     * @param postsText - текст постов
     */
    private void addAttachmentsToPost(Group userFindGroup, List<WallpostAttachment> userFindGroupPostAttachments,
                                      StringBuilder postsText) {
        boolean impossibleToLoadAttachment = false;
        for (WallpostAttachment userFindGroupPostAttachment : userFindGroupPostAttachments) {
            String userFindGroupPostAttachmentTypeString = userFindGroupPostAttachment.getType().toString();
            switch (userFindGroupPostAttachmentTypeString) {
                case "photo" -> {
                    postsText.append(userFindGroupPostAttachment
                                    .getPhoto().getSizes()
                                    .get(VkApiConsts.FIRST_ELEMENT_INDEX)
                                    .getUrl())
                             .append(" ");
                }
                case "link" -> {
                    postsText.append(userFindGroupPostAttachment.getLink().getUrl()).append(" ");
                }
                case "audio", "video" -> {
                    impossibleToLoadAttachment = true;
                }
            }
        }

        if (impossibleToLoadAttachment) {
            postsText.append("\nЕсть файлы, недоступные для отображения на сторонних ресурсах.\n")
                     .append("Если хотите посмотреть их, перейдите по ссылке: ")
                     .append(VkApiConsts.VK_ADDRESS)
                     .append(userFindGroup.getScreenName());
        }

    }

    /**
     * Метод, который получает code из get параметров GET запроса на сервер
     *
     * @param httpRequestGetParameters - get параметры отправленные на сервер
     * @return code
     */
    private String getAuthCodeFromHttpRequest(String httpRequestGetParameters) {
        StringBuilder authCodeBuilder = new StringBuilder();
        for (int i = httpRequestGetParameters.lastIndexOf("code=") + "code=".length(); i < httpRequestGetParameters.length(); i++){

            if (httpRequestGetParameters.charAt(i) == '&') {
                break;
            }

            authCodeBuilder.append(httpRequestGetParameters.charAt(i));
        }
        return authCodeBuilder.toString();
    }
}
