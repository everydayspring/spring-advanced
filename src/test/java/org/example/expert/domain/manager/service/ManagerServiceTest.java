package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Nested
    class SaveManagerTest {
        @Test // 테스트코드 샘플
        void todo가_정상적으로_등록된다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            assertEquals(managerUser.getEmail(), response.getUser().getEmail());
        }

        @Test
        void todo의_user가_null인_경우_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(authUser, todoId, managerSaveRequest)
            );

            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        void todo등록중_AuthUser와_TodoUser가_일치하지_않아_에러발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            User user = new User("b@b.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 2L);

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", user);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

            // then
            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        void todo등록중_일정_작성자를_담당자로_등록하려해_에러발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            User user = new User("b@b.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", user);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));



            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

            // then
            assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
        }
    }

    @Nested
    class GetManagersTest {
        @Test // 테스트코드 샘플
        public void manager_목록_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("user1@example.com", "password", UserRole.USER);
            Todo todo = new Todo("Title", "Contents", "Sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager mockManager = new Manager(todo.getUser(), todo);
            List<Manager> managerList = List.of(mockManager);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

            // when
            List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

            // then
            assertEquals(1, managerResponses.size());
            assertEquals(mockManager.getId(), managerResponses.get(0).getId());
            assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
        }

        @Test
        public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
            // given
            long todoId = 1L;
            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
            assertEquals("Todo not found", exception.getMessage());
        }
    }

    @Nested
    class DeleteManagerTest {
        @Test
        void 담당자_삭제_정상동작() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User("user1@example.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            User newUser = new User("user2@example.com", "password", UserRole.USER);

            Todo todo = new Todo("Title", "Contents", "Sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager manager = new Manager(newUser, todo);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

            // when
            managerService.deleteManager(userId, todoId, managerId);

            // then
            verify(managerRepository, times(1)).delete(any(Manager.class));
        }

        @Test
        void 담당자_삭제중_유효하지_않은_userId_에러발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->managerService.deleteManager(userId, todoId, managerId));

            // then
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        void 담당자_삭제중_유효하지_않은_todoId_에러발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User("user1@example.com", "password", UserRole.USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->managerService.deleteManager(userId, todoId, managerId));

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        void 담당자_삭제중_todo에_등록된_User가_없는_에러발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User("user1@example.com", "password", UserRole.USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            Todo todo = new Todo("Title", "Contents", "Sunny", null);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->managerService.deleteManager(userId, todoId, managerId));

            // then
            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        void 담당자_삭제중_todo에_등록된_User가_아닌_에러발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User("user1@example.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            User newUser = new User("user1@example.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 2L);

            Todo todo = new Todo("Title", "Contents", "Sunny", newUser);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->managerService.deleteManager(userId, todoId, managerId));

            // then
            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        void 담당자_삭제중_todo에_등록된_담당자가_없는_에러발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User("user1@example.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            Todo todo = new Todo("Title", "Contents", "Sunny", user);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            given(managerRepository.findById(anyLong())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->managerService.deleteManager(userId, todoId, managerId));

            // then
            assertEquals("Manager not found", exception.getMessage());
        }
    }
}
