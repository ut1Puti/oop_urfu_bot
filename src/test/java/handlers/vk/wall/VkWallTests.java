package handlers.vk.wall;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import database.GroupsStorage;
import handlers.vk.oAuth.VkAuth;
import httpserver.server.HttpServer;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Класс для тестирования класса для взаимодействия со стеной vk через vk api
 *
 * @author Кедровских Олег
 * @version 2.0
 */
public class VkWallTests {
    /**
     * Поле транспортного клиента
     */
    private final TransportClient transportClient = new HttpTransportClient();
    /**
     * Поле класс позволяющего работать с vk api
     */
    private final VkApiClient vk = new VkApiClient(transportClient);
    /**
     * Поле класса взаимодействующего со стеной vk через vk api
     */
    private final VkWall vkWall = new VkWall(vk);

    /**
     * Метод обработку случая при котором кол-во запрашиваемых постов в getLastPosts больше чем можно получить из vk api
     *
     * @throws ApiException    - возникает при ошибке обращения к vk api со стороны vk
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    @Test
    public void testAmountOfPostsMoreThanOneHundredInGetLastPostsMethod() throws ClientException, ApiException {
        int amountOfPostsMoreThanOneHundred = 101;
        String groupScreenName = "some not really interesting in this test name";
        Actor actor = new Actor() {
            @Override
            public String getAccessToken() {
                return null;
            }

            @Override
            public Integer getId() {
                return null;
            }
        };
        String expectedExceptionMessage = "Кол-во запрашиваемых постов превышает кол-во доступных к получению";
        try {
            vkWall.getPosts(groupScreenName, amountOfPostsMoreThanOneHundred, actor);
        } catch (IllegalArgumentException e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
            return;
        }
        throw new RuntimeException("Тест не пройден, тк не было получено и обработано исключение");
    }

    /**
     * Метод обработку случая при котором кол-во запрашиваемых постов в getPosts больше чем можно получить из vk api
     *
     * @throws ApiException    - возникает при ошибке обращения к vk api со стороны vk
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    @Test
    public void testAmountOfPostsMoreThanOneHundredInGetPostsMethod() throws ClientException, ApiException {
        int amountOfPostsMoreThanOneHundred = 101;
        String groupScreenName = "some not really interesting in this test name";
        Actor actor = new Actor() {
            @Override
            public String getAccessToken() {
                return null;
            }

            @Override
            public Integer getId() {
                return null;
            }
        };
        String expectedExceptionMessage = "Кол-во запрашиваемых постов превышает кол-во доступных к получению";
        try {
            vkWall.getPosts(groupScreenName, amountOfPostsMoreThanOneHundred, actor);
        } catch (IllegalArgumentException e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
            return;
        }
        throw new RuntimeException("Тест не пройден, тк не было получено и обработано исключение");
    }

    /**
     * Метод проверяющий обработку обращения к getLastPosts пользователя не имеющего к этому методу доступу
     *
     * @throws ApiException    - возникает при ошибке обращения к vk api со стороны vk
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    @Test
    public void testIllegalActorInGetPostsMethod() throws ClientException, ApiException {
        int amountOfPostsLessOrEqualsOneHundred = 100;
        String groupScreenName = "some not really interesting in this test name";
        Actor actor = new Actor() {
            @Override
            public String getAccessToken() {
                return null;
            }

            @Override
            public Integer getId() {
                return null;
            }
        };
        String expectedExceptionMessage = "Этот пользователь не имеет доступа к этому методу";
        try {
            vkWall.getPosts(groupScreenName, amountOfPostsLessOrEqualsOneHundred, actor);
        } catch (IllegalArgumentException e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
            return;
        }
        throw new RuntimeException("Тест не пройден, тк не было получено и обработано исключение");
    }

    /**
     * Метод тестирующий попытку получить уведомления из группы, которой нет в вк
     *
     * @throws ApiException    - возникает при ошибке обращения к vk api со стороны vk
     * @throws ClientException - возникает при ошибке обращения к vk api со стороны клиента
     */
    @Test
    public void testNoGroupInDataBase() throws ClientException, ApiException {
        HttpServer httpServer = HttpServer.getInstance();
        VkAuth vkAuth = new VkAuth(vk, httpServer, "src/test/resources/anonsrc/vk_config.json");
        ServiceActor appUser = vkAuth.createAppActor();
        GroupsStorage groupBase = GroupsStorage.getInstance();
        Optional<List<String>> noPosts = vkWall.getNewPostsStrings(groupBase, "some unknown group", appUser);
        assertEquals(Optional.empty(), noPosts);
    }
}
