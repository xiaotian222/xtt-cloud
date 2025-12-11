package xtt.cloud.oa.document.application.flow.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FlowApprovalService 单元测试
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlowApprovalService 单元测试")
class FlowApprovalServiceTest {

    @Mock
    private FlowEngineService flowEngineService;

    @InjectMocks
    private FlowApprovalService flowApprovalService;

    @BeforeEach
    void setUp() {
        // 设置默认行为
        doNothing().when(flowEngineService).processNodeApproval(
                anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("审批同意 - 成功")
    void testApprove_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "同意";
        Long approverId = 1000L;

        // When
        flowApprovalService.approve(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_APPROVE, comments, approverId);
    }

    @Test
    @DisplayName("审批拒绝 - 成功")
    void testReject_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "拒绝";
        Long approverId = 1000L;

        // When
        flowApprovalService.reject(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_REJECT, comments, approverId);
    }

    @Test
    @DisplayName("审批转发 - 成功")
    void testForward_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "转发";
        Long approverId = 1000L;

        // When
        flowApprovalService.forward(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_FORWARD, comments, approverId);
    }

    @Test
    @DisplayName("审批退回 - 成功")
    void testReturnBack_Success() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "退回";
        Long approverId = 1000L;

        // When
        flowApprovalService.returnBack(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_RETURN, comments, approverId);
    }

    @Test
    @DisplayName("审批同意 - 传递空评论")
    void testApprove_WithEmptyComments() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = "";
        Long approverId = 1000L;

        // When
        flowApprovalService.approve(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_APPROVE, comments, approverId);
    }

    @Test
    @DisplayName("审批拒绝 - 传递空评论")
    void testReject_WithEmptyComments() {
        // Given
        Long nodeInstanceId = 1L;
        String comments = null;
        Long approverId = 1000L;

        // When
        flowApprovalService.reject(nodeInstanceId, comments, approverId);

        // Then
        verify(flowEngineService, times(1)).processNodeApproval(
                nodeInstanceId, DoneTask.ACTION_REJECT, comments, approverId);
    }
}

