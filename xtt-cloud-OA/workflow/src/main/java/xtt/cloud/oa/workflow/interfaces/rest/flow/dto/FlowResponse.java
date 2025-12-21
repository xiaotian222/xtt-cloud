package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;

/**
 * 流程响应DTO
 * 
 * @author xtt
 */
public class FlowResponse {
    
    private boolean success;
    private String message;
    private FlowInstanceDTO data;
    
    public FlowResponse() {
    }
    
    public FlowResponse(boolean success, String message, FlowInstanceDTO data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static FlowResponse success(FlowInstanceDTO data) {
        return new FlowResponse(true, "操作成功", data);
    }
    
    public static FlowResponse success(String message, FlowInstanceDTO data) {
        return new FlowResponse(true, message, data);
    }
    
    public static FlowResponse failure(String message) {
        return new FlowResponse(false, message, null);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public FlowInstanceDTO getData() {
        return data;
    }
    
    public void setData(FlowInstanceDTO data) {
        this.data = data;
    }
}

