package socialnetworks.vk.wall;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.junit.jupiter.api.Test;
import socialnetworks.socialnetwork.SocialNetworkException;
import user.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
     * Метод для тестирования обработки некорректного числа получаемых пользователем отпрашенных строк
     *
     * @throws SocialNetworkException возникает при ошибках обращения к vk api
     */
    @Test
    public void testAmountOfPostsMoreThanHundred() throws SocialNetworkException {
        int amountOfPostsMoreThanOneHundred = 101;
        String groupScreenName = "some not really interesting in this test name";
        User user = new User(100, "accessToken", "telegramId");
        assertEquals(
                Optional.empty(), vkWall.getPostsStrings(groupScreenName, amountOfPostsMoreThanOneHundred, user)
        );
    }

    /**
     * Метод обработку случая при котором кол-во запрашиваемых постов в getPosts больше чем можно получить из vk api
     *
     * @throws SocialNetworkException возникает при ошибках обращения к vk api
     */
    @Test
    public void testAmountOfPostsMoreThanOneHundredInGetPostsMethod() throws SocialNetworkException {
        int amountOfPostsMoreThanOneHundred = 101;
        String groupScreenName = "some not really interesting in this test name";
        User user = new User(100, "accessToken", "telegramId");
        String expectedExceptionMessage = "Кол-во запрашиваемых постов превышает кол-во доступных к получению";
        try {
            vkWall.getPosts(groupScreenName, amountOfPostsMoreThanOneHundred, user);
        } catch (IllegalArgumentException e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
            return;
        }
        throw new RuntimeException("Тест не пройден, тк не было получено и обработано исключение");
    }

    /**
     * Метод для тестирования метода при передаче корректного пользователя
     *
     * @throws SocialNetworkException возникает при ошибках обращения к vk api
     */
    @Test
    public void testAmountOfPostsLessOeEqualsOneHundredInGetPostsMethod() throws SocialNetworkException {
        int amountOfPostsMoreThanOneHundred = 100;
        String groupScreenName = "some not really interesting in this test name";
        String exrectedException = "Этот пользователь не имеет доступа к этому методу";
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
        try {
            vkWall.getPosts(groupScreenName, amountOfPostsMoreThanOneHundred, actor);
        } catch (IllegalArgumentException e) {
            assertEquals(exrectedException, e.getMessage());
            return;
        }
        fail();
    }
}
