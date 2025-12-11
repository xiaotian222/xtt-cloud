package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.task.TodoTask;
import xtt.cloud.oa.document.domain.mapper.flow.DoneTaskMapper;
import xtt.cloud.oa.document.domain.mapper.flow.TodoTaskMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskService 单元测试
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService 单元测试")
class TaskServiceTest {

    @Mock
    private TodoTaskMapper todoTaskMapper;

    @Mock
    private DoneTaskMapper doneTaskMapper;

    @InjectMocks
    private TaskService taskService;

    private FlowInstance flowInstance;
    private FlowNodeInstance nodeInstance;
    private Document document;
    private TodoTask todoTask;
    private DoneTask doneTask;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        flowInstance = new FlowInstance();
        flowInstance.setId(1L);
        flowInstance.setDocumentId(100L);

        nodeInstance = new FlowNodeInstance();
        nodeInstance.setId(1L);
        nodeInstance.setFlowInstanceId(1L);
        nodeInstance.setApproverId(1000L);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);

        document = new Document();
        document.setId(100L);
        document.setTitle("测试公文");
        document.setContent("测试内容");
        document.setUrgencyLevel(0); // 普通紧急程度

        todoTask = new TodoTask();
        todoTask.setId(1L);
        todoTask.setDocumentId(100L);
        todoTask.setFlowInstanceId(1L);
        todoTask.setNodeInstanceId(1L);
        todoTask.setAssigneeId(1000L);
        todoTask.setTitle("测试公文");
        todoTask.setTaskType(TodoTask.TASK_TYPE_USER);
        todoTask.setStatus(TodoTask.STATUS_PENDING);

        doneTask = new DoneTask();
        doneTask.setId(1L);
        doneTask.setDocumentId(100L);
        doneTask.setFlowInstanceId(1L);
        doneTask.setNodeInstanceId(1L);
        doneTask.setHandlerId(1000L);
        doneTask.setAction(DoneTask.ACTION_APPROVE);
        doneTask.setTaskType(DoneTask.TASK_TYPE_USER);
    }

    @Test
    @DisplayName("创建待办任务 - 成功")
    void testCreateTodoTask_Success() {
        // Given
        Long approverId = 1000L;
        when(todoTaskMapper.insert(any(TodoTask.class))).thenAnswer(invocation -> {
            TodoTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        // When
        taskService.createTodoTask(nodeInstance, approverId, flowInstance, document);

        // Then
        ArgumentCaptor<TodoTask> captor = ArgumentCaptor.forClass(TodoTask.class);
        verify(todoTaskMapper, times(1)).insert(captor.capture());
        TodoTask created = captor.getValue();
        assertEquals(flowInstance.getDocumentId(), created.getDocumentId());
        assertEquals(flowInstance.getId(), created.getFlowInstanceId());
        assertEquals(nodeInstance.getId(), created.getNodeInstanceId());
        assertEquals(approverId, created.getAssigneeId());
        assertEquals(TodoTask.TASK_TYPE_USER, created.getTaskType());
        assertEquals(TodoTask.STATUS_PENDING, created.getStatus());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    @DisplayName("创建已办任务 - 成功")
    void testCreateDoneTask_Success() {
        // Given
        String action = DoneTask.ACTION_APPROVE;
        String comments = "同意";
        when(doneTaskMapper.insert(any(DoneTask.class))).thenAnswer(invocation -> {
            DoneTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        // When
        taskService.createDoneTask(nodeInstance, action, comments, flowInstance, document);

        // Then
        ArgumentCaptor<DoneTask> captor = ArgumentCaptor.forClass(DoneTask.class);
        verify(doneTaskMapper, times(1)).insert(captor.capture());
        DoneTask created = captor.getValue();
        assertEquals(flowInstance.getDocumentId(), created.getDocumentId());
        assertEquals(flowInstance.getId(), created.getFlowInstanceId());
        assertEquals(nodeInstance.getId(), created.getNodeInstanceId());
        assertEquals(nodeInstance.getApproverId(), created.getHandlerId());
        assertEquals(action, created.getAction());
        assertEquals(comments, created.getComments());
        assertEquals(DoneTask.TASK_TYPE_USER, created.getTaskType());
        assertNotNull(created.getHandledAt());
    }

    @Test
    @DisplayName("标记待办任务为已处理 - 成功")
    void testMarkAsHandled_Success() {
        // Given
        Long nodeInstanceId = 1L;
        Long approverId = 1000L;
        when(todoTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(todoTask));
        when(todoTaskMapper.updateById(any(TodoTask.class))).thenReturn(1);

        // When
        taskService.markAsHandled(nodeInstanceId, approverId);

        // Then
        ArgumentCaptor<TodoTask> captor = ArgumentCaptor.forClass(TodoTask.class);
        verify(todoTaskMapper, times(1)).updateById(captor.capture());
        assertEquals(TodoTask.STATUS_HANDLED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getHandledAt());
    }

    @Test
    @DisplayName("根据审批人获取待办任务列表 - 成功")
    void testGetTodoTasksByAssignee_Success() {
        // Given
        Long assigneeId = 1000L;
        int pageNum = 1;
        int pageSize = 10;
        Page<TodoTask> pageObj = new Page<>(pageNum, pageSize);
        pageObj.setRecords(Arrays.asList(todoTask));
        pageObj.setTotal(1);
        when(todoTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageObj);

        // When
        IPage<TodoTask> result = taskService.getTodoTasksByAssignee(assigneeId, pageNum, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(todoTaskMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据流程实例获取待办任务列表 - 成功")
    void testGetTodoTasksByFlowInstance_Success() {
        // Given
        Long flowInstanceId = 1L;
        when(todoTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(todoTask));

        // When
        List<TodoTask> result = taskService.getTodoTasksByFlowInstance(flowInstanceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(todoTask.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("根据处理人获取已办任务列表 - 成功")
    void testGetDoneTasksByHandler_Success() {
        // Given
        Long handlerId = 1000L;
        int pageNum = 1;
        int pageSize = 10;
        Page<DoneTask> pageObj = new Page<>(pageNum, pageSize);
        pageObj.setRecords(Arrays.asList(doneTask));
        pageObj.setTotal(1);
        when(doneTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageObj);

        // When
        IPage<DoneTask> result = taskService.getDoneTasksByHandler(handlerId, pageNum, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("根据任务类型查询待办任务 - 成功")
    void testGetTodoTasksByType_Success() {
        // Given
        Integer taskType = TodoTask.TASK_TYPE_JAVA;
        Long assigneeId = 1000L;
        int pageNum = 1;
        int pageSize = 10;
        TodoTask javaTask = new TodoTask();
        javaTask.setId(2L);
        javaTask.setTaskType(TodoTask.TASK_TYPE_JAVA);
        Page<TodoTask> pageObj = new Page<>(pageNum, pageSize);
        pageObj.setRecords(Arrays.asList(javaTask));
        pageObj.setTotal(1);
        when(todoTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageObj);

        // When
        IPage<TodoTask> result = taskService.getTodoTasksByType(taskType, assigneeId, pageNum, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(TodoTask.TASK_TYPE_JAVA, result.getRecords().get(0).getTaskType());
    }
}

