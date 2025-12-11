package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowDefinition;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.FlowDefinitionMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FlowEngineService 单元测试
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlowEngineService 单元测试")
class FlowEngineServiceTest {

    @Mock
    private FlowDefinitionMapper flowDefinitionMapper;

    @Mock
    private FlowNodeMapper flowNodeMapper;

    @Mock
    private FlowInstanceMapper flowInstanceMapper;

    @Mock
    private FlowNodeInstanceMapper flowNodeInstanceMapper;

    @Mock
    private DocumentService documentService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private FlowEngineService flowEngineService;

    private Document document;
    private FlowDefinition flowDefinition;
    private FlowNode flowNode;
    private FlowInstance flowInstance;
    private FlowNodeInstance nodeInstance;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        document = new Document();
        document.setId(100L);
        document.setTitle("测试公文");
        document.setStatus(Document.STATUS_REVIEWING);
        document.setCreatorId(1000L);
        document.setDeptId(100L);

        flowDefinition = new FlowDefinition();
        flowDefinition.setId(10L);
        flowDefinition.setName("测试流程");
        flowDefinition.setStatus(FlowDefinition.STATUS_ENABLED);

        flowNode = new FlowNode();
        flowNode.setId(1L);
        flowNode.setFlowDefId(10L);
        flowNode.setNodeName("开始节点");
        flowNode.setNodeType(FlowNode.NODE_TYPE_APPROVAL);
        flowNode.setOrderNum(1);
        flowNode.setApproverType(FlowNode.APPROVER_TYPE_USER);
        // Note: FlowNode 可能没有 approverIds 字段，这里仅用于测试

        flowInstance = new FlowInstance();
        flowInstance.setId(1L);
        flowInstance.setDocumentId(100L);
        flowInstance.setFlowDefId(10L);
        flowInstance.setStatus(FlowInstance.STATUS_PROCESSING);
        flowInstance.setCurrentNodeId(1L);

        nodeInstance = new FlowNodeInstance();
        nodeInstance.setId(1L);
        nodeInstance.setFlowInstanceId(1L);
        nodeInstance.setNodeId(1L);
        nodeInstance.setApproverId(1000L);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
    }

    @Test
    @DisplayName("启动流程 - 成功")
    void testStartFlow_Success() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        List<FlowNode> nodes = Arrays.asList(flowNode);

        when(documentService.getDocument(documentId)).thenReturn(document);
        when(flowDefinitionMapper.selectById(flowDefId)).thenReturn(flowDefinition);
        when(flowNodeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(nodes);
        when(flowInstanceMapper.insert(any(FlowInstance.class))).thenAnswer(invocation -> {
            FlowInstance instance = invocation.getArgument(0);
            instance.setId(1L);
            return 1;
        });
        when(flowNodeInstanceMapper.insert(any(FlowNodeInstance.class))).thenAnswer(invocation -> {
            FlowNodeInstance instance = invocation.getArgument(0);
            instance.setId(1L);
            return 1;
        });
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        FlowInstance result = flowEngineService.startFlow(documentId, flowDefId);

        // Then
        assertNotNull(result);
        assertEquals(FlowInstance.STATUS_PROCESSING, result.getStatus());
        verify(flowInstanceMapper, times(1)).insert(any(FlowInstance.class));
        verify(flowNodeInstanceMapper, atLeastOnce()).insert(any(FlowNodeInstance.class));
    }

    @Test
    @DisplayName("启动流程 - 公文不存在")
    void testStartFlow_DocumentNotFound() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        when(documentService.getDocument(documentId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.startFlow(documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("启动流程 - 流程定义不存在")
    void testStartFlow_FlowDefinitionNotFound() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        when(documentService.getDocument(documentId)).thenReturn(document);
        when(flowDefinitionMapper.selectById(flowDefId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.startFlow(documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("启动流程 - 流程定义已停用")
    void testStartFlow_FlowDefinitionDisabled() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        flowDefinition.setStatus(FlowDefinition.STATUS_DISABLED);
        when(documentService.getDocument(documentId)).thenReturn(document);
        when(flowDefinitionMapper.selectById(flowDefId)).thenReturn(flowDefinition);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.startFlow(documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("启动流程 - 没有配置节点")
    void testStartFlow_NoNodes() {
        // Given
        Long documentId = 100L;
        Long flowDefId = 10L;
        when(documentService.getDocument(documentId)).thenReturn(document);
        when(flowDefinitionMapper.selectById(flowDefId)).thenReturn(flowDefinition);
        when(flowNodeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.startFlow(documentId, flowDefId);
        });
    }

    @Test
    @DisplayName("处理节点审批 - 同意")
    void testProcessNodeApproval_Approve() {
        // Given
        Long nodeInstanceId = 1L;
        String action = "approve";
        String comments = "同意";
        Long approverId = 1000L;

        nodeInstance.setApproverId(approverId);
        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(flowNodeMapper.selectById(anyLong())).thenReturn(flowNode);
        when(flowNodeInstanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(nodeInstance));
        when(flowNodeInstanceMapper.updateById(any(FlowNodeInstance.class))).thenReturn(1);
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        flowEngineService.processNodeApproval(nodeInstanceId, action, comments, approverId);

        // Then
        verify(flowNodeInstanceMapper, atLeastOnce()).updateById(any(FlowNodeInstance.class));
    }

    @Test
    @DisplayName("处理节点审批 - 拒绝")
    void testProcessNodeApproval_Reject() {
        // Given
        Long nodeInstanceId = 1L;
        String action = "reject";
        String comments = "拒绝";
        Long approverId = 1000L;

        nodeInstance.setApproverId(approverId);
        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);
        when(flowInstanceMapper.selectById(anyLong())).thenReturn(flowInstance);
        when(flowNodeInstanceMapper.updateById(any(FlowNodeInstance.class))).thenReturn(1);
        when(flowInstanceMapper.updateById(any(FlowInstance.class))).thenReturn(1);

        // When
        flowEngineService.processNodeApproval(nodeInstanceId, action, comments, approverId);

        // Then
        ArgumentCaptor<FlowInstance> instanceCaptor = ArgumentCaptor.forClass(FlowInstance.class);
        verify(flowInstanceMapper).updateById(instanceCaptor.capture());
        assertEquals(FlowInstance.STATUS_TERMINATED, instanceCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("处理节点审批 - 无权操作")
    void testProcessNodeApproval_Unauthorized() {
        // Given
        Long nodeInstanceId = 1L;
        String action = "approve";
        String comments = "同意";
        Long approverId = 2000L; // 不同的审批人

        nodeInstance.setApproverId(1000L);
        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.processNodeApproval(nodeInstanceId, action, comments, approverId);
        });
    }

    @Test
    @DisplayName("处理节点审批 - 节点状态不正确")
    void testProcessNodeApproval_InvalidStatus() {
        // Given
        Long nodeInstanceId = 1L;
        String action = "approve";
        String comments = "同意";
        Long approverId = 1000L;

        nodeInstance.setApproverId(approverId);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED); // 已完成状态
        when(flowNodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            flowEngineService.processNodeApproval(nodeInstanceId, action, comments, approverId);
        });
    }

    // Note: moveToNextNode 是私有方法，通过 processNodeApproval 间接测试
}

