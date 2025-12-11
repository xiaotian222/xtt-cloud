package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.domain.entity.flow.ApproverScope;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowAction;
import xtt.cloud.oa.document.domain.entity.flow.FlowActionRule;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.FreeFlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.ApproverScopeMapper;
import xtt.cloud.oa.document.domain.mapper.flow.DocumentMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowActionMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowActionRuleMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FreeFlowNodeInstanceMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FreeFlowEngineService 单元测试
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FreeFlowEngineService 单元测试")
class FreeFlowEngineServiceTest {

    @Mock
    private FlowActionMapper flowActionMapper;

    @Mock
    private FlowActionRuleMapper flowActionRuleMapper;

    @Mock
    private ApproverScopeMapper approverScopeMapper;

    @Mock
    private FreeFlowNodeInstanceMapper freeFlowNodeInstanceMapper;

    @Mock
    private FlowNodeInstanceMapper flowNodeInstanceMapper;

    @Mock
    private FlowNodeMapper flowNodeMapper;

    @Mock
    private FlowInstanceMapper flowInstanceMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private FreeFlowEngineService freeFlowEngineService;

    private Document document;
    private FlowInstance flowInstance;
    private FlowNodeInstance nodeInstance;
    private FlowAction flowAction;
    private FlowActionRule actionRule;
    private ApproverScope approverScope;
    private FlowNode flowNode;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        document = new Document();
        document.setId(100L);
        document.setTitle("测试公文");
        document.setStatus(Document.STATUS_REVIEWING);

        flowInstance = new FlowInstance();
        flowInstance.setId(1L);
        flowInstance.setDocumentId(100L);
        flowInstance.setFlowMode(FlowInstance.FLOW_MODE_FREE);
        flowInstance.setStatus(FlowInstance.STATUS_PROCESSING);

        nodeInstance = new FlowNodeInstance();
        nodeInstance.setId(1L);
        nodeInstance.setFlowInstanceId(1L);
        nodeInstance.setNodeId(1L);
        nodeInstance.setApproverId(1000L);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);

        flowAction = new FlowAction();
        flowAction.setId(10L);
        flowAction.setActionName("发送");
        flowAction.setActionType(FlowAction.TYPE_UNIT_HANDLE);
        flowAction.setEnabled(1);

        actionRule = new FlowActionRule();
        actionRule.setId(1L);
        actionRule.setActionId(10L);
        actionRule.setDocumentStatus(Document.STATUS_REVIEWING);
        actionRule.setUserRole("*");
        actionRule.setEnabled(1);
        actionRule.setPriority(1);

        approverScope = new ApproverScope();
        approverScope.setId(1L);
        approverScope.setActionId(10L);
        approverScope.setScopeType(ApproverScope.SCOPE_TYPE_DEPT);

        flowNode = new FlowNode();
        flowNode.setId(1L);
        flowNode.setNodeName("自由流节点");
        flowNode.setNodeType(FlowNode.NODE_TYPE_FREE_FLOW);
    }

    @Test
    @DisplayName("获取可用动作 - 成功")
    void testGetAvailableActions_Success() {
        // Given
        Long documentId = 100L;
        Long userId = 1000L;
        when(documentMapper.selectById(documentId)).thenReturn(document);
        when(flowActionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(actionRule));
        when(flowActionMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(flowAction));

        // When
        List<FlowAction> result = freeFlowEngineService.getAvailableActions(documentId, userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(flowAction.getId(), result.get(0).getId());
        verify(documentMapper, times(1)).selectById(documentId);
    }

    @Test
    @DisplayName("获取可用动作 - 文档不存在")
    void testGetAvailableActions_DocumentNotFound() {
        // Given
        Long documentId = 100L;
        Long userId = 1000L;
        when(documentMapper.selectById(documentId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            freeFlowEngineService.getAvailableActions(documentId, userId);
        });
    }

    @Test
    @DisplayName("获取可用动作 - 无匹配规则")
    void testGetAvailableActions_NoMatchingRules() {
        // Given
        Long documentId = 100L;
        Long userId = 1000L;
        when(documentMapper.selectById(documentId)).thenReturn(document);
        when(flowActionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<FlowAction> result = freeFlowEngineService.getAvailableActions(documentId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取审批人选择范围 - 成功")
    void testGetApproverScope_Success() {
        // Given
        Long actionId = 10L;
        when(approverScopeMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(approverScope);

        // When
        ApproverScope result = freeFlowEngineService.getApproverScope(actionId);

        // Then
        assertNotNull(result);
        assertEquals(actionId, result.getActionId());
        verify(approverScopeMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取审批人选择范围 - 使用默认范围")
    void testGetApproverScope_UseDefault() {
        // Given
        Long actionId = 10L;
        when(approverScopeMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        // When
        ApproverScope result = freeFlowEngineService.getApproverScope(actionId);

        // Then
        assertNotNull(result);
        assertEquals(actionId, result.getActionId());
    }

    @Test
    @DisplayName("执行发送动作 - 成功")
    void testExecuteAction_Success() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        List<Long> selectedDeptIds = new ArrayList<>();
        List<Long> selectedUserIds = Arrays.asList(2000L);
        String comment = "发送";
        Long operatorId = 1000L;

        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(documentMapper.selectById(anyLong())).thenReturn(document);
        when(flowActionMapper.selectById(actionId)).thenReturn(flowAction);
        when(approverScopeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(approverScope);
        when(flowNodeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(flowNodeMapper.insert(any(FlowNode.class))).thenAnswer(invocation -> {
            FlowNode node = invocation.getArgument(0);
            node.setId(2L);
            return 1;
        });
        when(flowNodeInstanceMapper.insert(any(FlowNodeInstance.class))).thenAnswer(invocation -> {
            FlowNodeInstance instance = invocation.getArgument(0);
            instance.setId(2L);
            return 1;
        });
        when(freeFlowNodeInstanceMapper.insert(any(FreeFlowNodeInstance.class))).thenReturn(1);
        when(flowNodeInstanceMapper.updateById(any(FlowNodeInstance.class))).thenReturn(1);
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        FlowNodeInstance result = freeFlowEngineService.executeAction(
                currentNodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId);

        // Then
        assertNotNull(result);
        verify(flowNodeInstanceMapper, atLeastOnce()).insert(any(FlowNodeInstance.class));
        verify(freeFlowNodeInstanceMapper, atLeastOnce()).insert(any(FreeFlowNodeInstance.class));
        verify(flowNodeInstanceMapper, times(1)).updateById(any(FlowNodeInstance.class));
    }

    @Test
    @DisplayName("执行发送动作 - 节点实例不存在")
    void testExecuteAction_NodeInstanceNotFound() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            freeFlowEngineService.executeAction(
                    currentNodeInstanceId, actionId, new ArrayList<>(), new ArrayList<>(), "comment", 1000L);
        });
    }

    @Test
    @DisplayName("执行发送动作 - 无权操作")
    void testExecuteAction_Unauthorized() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        Long operatorId = 2000L; // 不同的操作人
        nodeInstance.setApproverId(1000L);
        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(nodeInstance);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            freeFlowEngineService.executeAction(
                    currentNodeInstanceId, actionId, new ArrayList<>(), new ArrayList<>(), "comment", operatorId);
        });
    }

    @Test
    @DisplayName("执行发送动作 - 节点状态不正确")
    void testExecuteAction_InvalidStatus() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED); // 已完成状态
        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(nodeInstance);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            freeFlowEngineService.executeAction(
                    currentNodeInstanceId, actionId, new ArrayList<>(), new ArrayList<>(), "comment", 1000L);
        });
    }

    @Test
    @DisplayName("执行发送动作 - 动作不可用")
    void testExecuteAction_ActionNotAvailable() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(documentMapper.selectById(anyLong())).thenReturn(document);
        when(documentMapper.selectById(anyLong())).thenReturn(document);
        when(flowActionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList()); // 无匹配规则，动作不可用

        // When & Then
        assertThrows(BusinessException.class, () -> {
            freeFlowEngineService.executeAction(
                    currentNodeInstanceId, actionId, new ArrayList<>(), new ArrayList<>(), "comment", 1000L);
        });
    }

    @Test
    @DisplayName("执行返回动作 - 成功")
    void testExecuteAction_ReturnAction() {
        // Given
        Long currentNodeInstanceId = 1L;
        Long actionId = 10L;
        flowAction.setActionType(FlowAction.TYPE_RETURN);
        List<Long> selectedDeptIds = new ArrayList<>();
        List<Long> selectedUserIds = new ArrayList<>();
        String comment = "退回";
        Long operatorId = 1000L;

        when(flowNodeInstanceMapper.selectById(currentNodeInstanceId)).thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(documentMapper.selectById(anyLong())).thenReturn(document);
        when(flowActionMapper.selectById(actionId)).thenReturn(flowAction);
        when(approverScopeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(approverScope);
        when(flowNodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(flowNodeInstanceMapper.updateById(any(FlowNodeInstance.class))).thenReturn(1);
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        FlowNodeInstance result = freeFlowEngineService.executeAction(
                currentNodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId);

        // Then
        assertNotNull(result);
        verify(flowNodeInstanceMapper, times(1)).updateById(any(FlowNodeInstance.class));
    }

    // Note: isActionAvailable 是私有方法，通过 executeAction 间接测试
}

