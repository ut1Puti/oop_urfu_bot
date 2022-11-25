package dj.arbuz.database.hibernate.group;

import dj.arbuz.database.GroupBase;
import dj.arbuz.database.hibernate.user.UserDto;
import dj.arbuz.database.hibernate.user.UserService;
import dj.arbuz.user.BotUser;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupService implements GroupBase {
    private final UserService userService = new UserService();

    private final GroupRepository groupRepository = new GroupRepository();

    /**
     * Метод добавления информации о подписчиках группы
     *
     * @param groupScreenName короткое имя группы
     * @param subscriberId      пользователь подписавшийся на группу
     * @return {@code true} - если пользователь был добавлен в базу данных подписчиков группы,
     * {@code false} - если пользователь не был добавлен в базу данных
     */
    @Override
    public boolean addSubscriber(String groupScreenName, String subscriberId) {
        GroupDto dbSavedGroup = groupRepository.findByScreenName(groupScreenName);
        BotUser subscriber = userService.getUser(subscriberId);
        UserDto subscriberDto = UserDto.builder()
                .telegramId(subscriber.getTelegramId())
                .vkId(Long.valueOf(subscriber.getId()))
                .accessToken(subscriber.getAccessToken())
                .build();

        if (dbSavedGroup == null) {
            dbSavedGroup = GroupDto.builder()
                    .groupName(groupScreenName)
                    .dateLastPost(Instant.now().getEpochSecond())
                    .subscribedUsers(List.of(subscriberDto))
                    .build();
            return groupRepository.save(dbSavedGroup) == dbSavedGroup;
        } else {

            if (dbSavedGroup.getSubscribedUsers().contains(subscriberDto)) {
                return false;
            }

            dbSavedGroup.addNewSubscriber(subscriberDto);
            return groupRepository.update(dbSavedGroup) == dbSavedGroup;
        }
    }

    /**
     * Метод удаления пользователь из подписчиков группы
     *
     * @param groupScreenName короткое имя группы
     * @param subscriberId      пользователь подписавшийся на группу
     * @return {@code true} - если пользователь был удален, {@code false} - если пользователь не был удален
     */
    @Override
    public boolean deleteSubscriber(String groupScreenName, String subscriberId) {
        GroupDto dbSavedGroup = groupRepository.findByScreenName(groupScreenName);

        if (dbSavedGroup == null) {
            return false;
        }

        BotUser subscriber = userService.getUser(subscriberId);
        UserDto subscriberDto = UserDto.builder()
                .telegramId(subscriber.getTelegramId())
                .vkId(Long.valueOf(subscriber.getId()))
                .accessToken(subscriber.getAccessToken())
                .build();

        if (dbSavedGroup.getSubscribedUsers().remove(subscriberDto)) {
            groupRepository.update(dbSavedGroup);
            return true;
        }

        return false;
    }

    /**
     * Метод получающий множество все групп из базы данных
     *
     * @return множество групп из базы данных
     */
    @Override
    public Set<String> getGroups() {
        return groupRepository.findAll().stream()
                .filter(Objects::nonNull)
                .map(GroupDto::getGroupName)
                .collect(Collectors.toSet());
    }

    /**
     * Метод получающий список всех подписчиков группы
     *
     * @param groupScreenName короткое имя группы
     * @return список всех подписчиков группы
     */
    @Override
    public List<String> getSubscribedToGroupUsersId(String groupScreenName) {
        return groupRepository.findByScreenName(groupScreenName).getSubscribedUsers()
                .stream()
                .filter(Objects::nonNull)
                .map(UserDto::getTelegramId)
                .toList();
    }

    /**
     * Метод получающий множество все групп, на которые подписан пользователь
     *
     * @param subscriberId id пользователя
     * @return множество групп на которые подписан пользователь
     */
    @Override
    public Set<String> getUserSubscribedGroups(String subscriberId) {
         return groupRepository.findBySubscriberId(subscriberId)
                 .stream()
                 .filter(Objects::nonNull)
                 .map(GroupDto::getGroupName)
                 .collect(Collectors.toSet());
    }

    /**
     * Метод получающий дату последнего поста группы
     *
     * @param groupScreenName короткое имя группы
     * @return {@code Optional.empty} если групп нет в базе данных,
     * {@code Optional.of(groupLastPost)} если есть группа в базе данных
     */
    @Override
    public Optional<Long> getGroupLastPostDate(String groupScreenName) {
        GroupDto dbSavedGroup = groupRepository.findByScreenName(groupScreenName);

        if (dbSavedGroup == null) {
            return Optional.empty();
        }

        return Optional.of(dbSavedGroup.getDateLastPost());
    }

    /**
     * Метод обновляющий дату последнего поста
     *
     * @param groupScreenName короткое имя группы
     * @param newLastPostDate новая дата последнего поста
     */
    @Override
    public void updateGroupLastPost(String groupScreenName, long newLastPostDate) {
        GroupDto dbSavedGroup = groupRepository.findByScreenName(groupScreenName);

        if (dbSavedGroup == null) {
            return;
        }

        dbSavedGroup.setDateLastPost(newLastPostDate);
        groupRepository.update(dbSavedGroup);
    }

    /**
     * Метод проверяющий есть ли группа в базе данных
     *
     * @param groupScreenName короткое имя группы
     * @return {@code true} - если группа есть в базе данных, {@code false} - если группы нет в базе данных
     */
    @Override
    public boolean containsGroup(String groupScreenName) {
        return groupRepository.findByScreenName(groupScreenName) != null;
    }
}