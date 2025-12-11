package xtt.cloud.oa.document.application.flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.application.flow.core.DocumentService;
import xtt.cloud.oa.document.application.flow.core.FlowApprovalService;
import xtt.cloud.oa.document.application.flow.core.FlowEngineService;
import xtt.cloud.oa.document.application.flow.core.FreeFlowEngineService;
import xtt.cloud.oa.document.application.flow.core.TaskService;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.Handling;
import xtt.cloud.oa.document.domain.mapper.flow.ExternalSignReceiptMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;
import xtt.cloud.oa.document.domain.mapper.flow.HandlingMapper;
import xtt.cloud.oa.document.domain.mapper.gw.IssuanceInfoMapper;
import xtt.cloud.oa.document.domain.mapper.gw.ReceiptInfoMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FlowService 单元测试
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlowService 单元测试")
class FlowServiceTest {

    @Mock
    private FlowEngineService flowEngineService;

    @Mock
    private FlowApprovalService flowApprovalService;

    @Mock
    private FreeFlowEngineService freeFlowEngineService;

    @Mock
    private TaskService taskService;

    @Mock
    private DocumentService documentService;

    @Mock
    private FlowInstanceMapper flowInstanceMapper;

    @Mock
    private FlowNodeMapper flowNodeMapper;

    @Mock
    private FlowNodeInstanceMapper flowNodeInstanceMapper;

    @Mock
    private IssuanceInfoMapper issuanceInfoMapper;

    @Mock
    private ReceiptInfoMapper receiptInfoMapper;

    @Mock
    private ExternalSignReceiptMapper externalSignReceiptMapper;

    @Mock
    private HandlingMapper handlingMapper;

    @InjectMocks
    private FlowService flowService;

    private FlowInstance flowInstance;
    private Document document;
    private FlowNodeInstance nodeInstance;
    private FlowNode flowNode;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        flowInstance = new FlowInstance();
        flowInstance.setId(1L);
        flowInstance.setDocumentId(100L);
        flowInstance.setFlowDefId(10L);
        flowInstance.setStatus(FlowInstance.STATUS_PROCESSING);
        flowInstance.setFlowMode(FlowInstance.FLOW_MODE_FIXED);
        flowInstance.setCreatedAt(LocalDateTime.now());
        flowInstance.setUpdatedAt(LocalDateTime.now());

        document = new Document();
        document.setId(100L);
        document.setTitle("测试公文");
        document.setContent("测试内容");
        document.setStatus(Document.STATUS_REVIEWING);

        nodeInstance = new FlowNodeInstance();
        nodeInstance.setId(1L);
        nodeInstance.setFlowInstanceId(1L);
        nodeInstance.setNodeId(1L);
        nodeInstance.setApproverId(1000L);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);

        flowNode = new FlowNode();
        flowNode.setId(1L);
        flowNode.setNodeName("测试节点");
        flowNode.setAllowFreeFlow(1);
    }

    @Test
    @DisplayName("创建流程实例 - 成功")
    void testCreateFlowInstance_Success() {
        // Given
        when(flowInstanceMapper.insert(any(FlowInstance.class))).thenAnswer(invocation -> {
            FlowInstance instance = invocation.getArgument(0);
            instance.setId(1L);
            return 1;
        });

        // When
        FlowInstance result = flowService.createFlowInstance(flowInstance);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(flowInstanceMapper, times(1)).insert(any(FlowInstance.class));
    }

    @Test
    @DisplayName("创建流程实例 - 失败")
    void testCreateFlowInstance_Failure() {
        // Given
        when(flowInstanceMapper.insert(any(FlowInstance.class)))
                .thenThrow(new RuntimeException("数据库错误"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowService.createFlowInstance(flowInstance);
        });
    }

    @Test
    @DisplayName("启动流程 - 成功")
    void testStartFlow_Success() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        when(flowEngineService.startFlow(documentId, flowDefId)).thenReturn(flowInstance);

        // When
        FlowInstance result = flowService.startFlow(documentId, flowDefId);

        // Then
        assertNotNull(result);
        assertEquals(flowInstance.getId(), result.getId());
        verify(flowEngineService, times(1)).startFlow(documentId, flowDefId);
    }

    @Test
    @DisplayName("启动流程 - 业务异常")
    void testStartFlow_BusinessException() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        when(flowEngineService.startFlow(documentId, flowDefId))
                .thenThrow(new BusinessException("流程定义不存在"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowService.startFlow(documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("审批同意 - 成功")
    void testApprove_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "同意";
        Long approverId = 1000L;

        // When
        flowService.approve(nodeInstanceId, comments, approverId);

        // Then
        verify(flowApprovalService, times(1)).approve(nodeInstanceId, comments, approverId);
    }

    @Test
    @DisplayName("审批拒绝 - 成功")
    void testReject_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "拒绝";
        Long approverId = 1000L;

        // When
        flowService.reject(nodeInstanceId, comments, approverId);

        // Then
        verify(flowApprovalService, times(1)).reject(nodeInstanceId, comments, approverId);
    }

    @Test
    @DisplayName("审批转发 - 成功")
    void testForward_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "转发";
        Long approverId = 1000L;

        // When
        flowService.forward(nodeInstanceId, comments, approverId);

        // Then
        verify(flowApprovalService, times(1)).forward(nodeInstanceId, comments, approverId);
    }

    @Test
    @DisplayName("审批退回 - 成功")
    void testReturnBack_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "退回";
        Long approverId = 1000L;

        // When
        flowService.returnBack(nodeInstanceId, comments, approverId);

        // Then
        verify(flowApprovalService, times(1)).returnBack(nodeInstanceId, comments, approverId);
    }

    @Test
    @DisplayName("获取流程实例 - 成功")
    void testGetFlowInstance_Success() {
        // Given
        Long flowInstanceId = 1L;
        when(flowInstanceMapper.selectById(flowInstanceId)).thenReturn(flowInstance);

        // When
        FlowInstance result = flowService.getFlowInstance(flowInstanceId);

        // Then
        assertNotNull(result);
        assertEquals(flowInstanceId, result.getId());
        verify(flowInstanceMapper, times(1)).selectById(flowInstanceId);
    }

    @Test
    @DisplayName("根据公文ID获取流程实例 - 成功")
    void testGetFlowInstanceByDocumentId_Success() {
        // Given
        Long documentId = 100L;
        when(flowInstanceMapper.selectOne(any())).thenReturn(flowInstance);

        // When
        FlowInstance result = flowService.getFlowInstanceByDocumentId(documentId);

        // Then
        assertNotNull(result);
        verify(flowInstanceMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("创建承办记录 - 成功")
    void testCreateHandling_Success() {
        // Given
        Handling handling = new Handling();
        handling.setFlowInstanceId(1L);
        handling.setHandlerId(1000L);
        when(handlingMapper.insert(any(Handling.class))).thenAnswer(invocation -> {
            Handling h = invocation.getArgument(0);
            h.setId(1L);
            return 1;
        });

        // When
        Handling result = flowService.createHandling(handling);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(handlingMapper, times(1)).insert(any(Handling.class));
    }

    @Test
    @DisplayName("更新承办记录 - 成功")
    void testUpdateHandling_Success() {
        // Given
        Long handlingId = 1L;
        String result = "已完成";
        Integer status = 1;
        Handling handling = new Handling();
        handling.setId(handlingId);
        when(handlingMapper.selectById(handlingId)).thenReturn(handling);
        when(handlingMapper.updateById(any(Handling.class))).thenReturn(1);

        // When
        Handling updated = flowService.updateHandling(handlingId, result, status);

        // Then
        assertNotNull(updated);
        assertEquals(result, updated.getHandlingResult());
        assertEquals(status, updated.getStatus());
        verify(handlingMapper, times(1)).selectById(handlingId);
        verify(handlingMapper, times(1)).updateById(any(Handling.class));
    }

    @Test
    @DisplayName("更新承办记录 - 记录不存在")
    void testUpdateHandling_NotFound() {
        // Given
        Long handlingId = 1L;
        when(handlingMapper.selectById(handlingId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowService.updateHandling(handlingId, "结果", 1);
        });
    }

    @Test
    @DisplayName("在固定流程节点中执行自由流转 - 成功")
    void testExecuteFreeFlowInFixedNode_Success() {
        // Given
        Long nodeInstanceId = 1L;
        Long actionId = 10L;
        List<Long> selectedDeptIds = new ArrayList<>();
        List<Long> selectedUserIds = List.of(2000L);
        String comment = "执行自由流转";
        Long operatorId = 1000L;

        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);
        when(flowNodeMapper.selectById(anyLong())).thenReturn(flowNode);
        when(freeFlowEngineService.executeAction(
                eq(nodeInstanceId), eq(actionId), eq(selectedDeptIds), 
                eq(selectedUserIds), eq(comment), eq(operatorId)))
                .thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(documentService.getDocument(anyLong())).thenReturn(document);
        when(flowNodeInstanceMapper.updateById(any(FlowNodeInstance.class))).thenReturn(1);

        // When
        FlowNodeInstance result = flowService.executeFreeFlowInFixedNode(
                nodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId);

        // Then
        assertNotNull(result);
        verify(freeFlowEngineService, times(1)).executeAction(
                eq(nodeInstanceId), eq(actionId), eq(selectedDeptIds), 
                eq(selectedUserIds), eq(comment), eq(operatorId));
        verify(flowNodeInstanceMapper, times(1)).updateById(any(FlowNodeInstance.class));
        verify(taskService, times(1)).createDoneTask(any(), anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("在固定流程节点中执行自由流转 - 节点不允许自由流")
    void testExecuteFreeFlowInFixedNode_NodeNotAllowFreeFlow() {
        // Given
        Long nodeInstanceId = 1L;
        Long actionId = 10L;
        flowNode.setAllowFreeFlow(0); // 不允许自由流
        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);
        when(flowNodeMapper.selectById(anyLong())).thenReturn(flowNode);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowService.executeFreeFlowInFixedNode(
                    nodeInstanceId, actionId, new ArrayList<>(), new ArrayList<>(), "comment", 1000L);
        });
    }

    @Test
    @DisplayName("在自由流中启动固定子流程 - 成功")
    void testStartFixedSubFlowInFreeFlow_Success() {
        // Given
        Long parentFlowInstanceId = 1L;
        Long documentId = 100L;
        Long flowDefId = 10L;
        flowInstance.setFlowMode(FlowInstance.FLOW_MODE_FREE);
        when(flowInstanceMapper.selectById(parentFlowInstanceId)).thenReturn(flowInstance);
        when(flowEngineService.startFlow(documentId, flowDefId)).thenReturn(flowInstance);
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        FlowInstance result = flowService.startFixedSubFlowInFreeFlow(
                parentFlowInstanceId, documentId, flowDefId);

        // Then
        assertNotNull(result);
        verify(flowEngineService, times(1)).startFlow(documentId, flowDefId);
        verify(flowInstanceMapper, times(1)).updateById(any(FlowInstance.class));
    }

    @Test
    @DisplayName("在自由流中启动固定子流程 - 父流程不是自由流")
    void testStartFixedSubFlowInFreeFlow_InvalidParentFlow() {
        // Given
        Long parentFlowInstanceId = 1L;
        Long documentId = 100L;
        Long flowDefId = 10L;
        flowInstance.setFlowMode(FlowInstance.FLOW_MODE_FIXED); // 固定流
        when(flowInstanceMapper.selectById(parentFlowInstanceId)).thenReturn(flowInstance);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowService.startFixedSubFlowInFreeFlow(parentFlowInstanceId, documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("检查子流程并继续父流程 - 子流程已完成")
    void testCheckAndContinueParentFlow_SubFlowCompleted() {
        // Given
        Long subFlowInstanceId = 2L;
        FlowInstance subFlowInstance = new FlowInstance();
        subFlowInstance.setId(subFlowInstanceId);
        subFlowInstance.setParentFlowInstanceId(1L);
        subFlowInstance.setStatus(FlowInstance.STATUS_COMPLETED);
        when(flowInstanceMapper.selectById(subFlowInstanceId)).thenReturn(subFlowInstance);
        when(flowInstanceMapper.selectById(1L)).thenReturn(flowInstance);

        // When
        flowService.checkAndContinueParentFlow(subFlowInstanceId);

        // Then
        verify(flowInstanceMapper, times(2)).selectById(anyLong());
    }

    @Test
    @DisplayName("检查子流程并继续父流程 - 子流程未完成")
    void testCheckAndContinueParentFlow_SubFlowNotCompleted() {
        // Given
        Long subFlowInstanceId = 2L;
        FlowInstance subFlowInstance = new FlowInstance();
        subFlowInstance.setId(subFlowInstanceId);
        subFlowInstance.setStatus(FlowInstance.STATUS_PROCESSING); // 未完成
        when(flowInstanceMapper.selectById(subFlowInstanceId)).thenReturn(subFlowInstance);

        // When
        flowService.checkAndContinueParentFlow(subFlowInstanceId);

        // Then
        verify(flowInstanceMapper, times(1)).selectById(subFlowInstanceId);
        verify(flowInstanceMapper, never()).selectById(1L);
    }
}

