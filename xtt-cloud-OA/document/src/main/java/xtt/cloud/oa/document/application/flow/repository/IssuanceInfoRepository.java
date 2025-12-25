package xtt.cloud.oa.document.application.flow.repository;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.gw.IssuanceInfo;
import xtt.cloud.oa.document.domain.mapper.gw.IssuanceInfoMapper;

/**
 * 发文信息 Repository
 * 封装 IssuanceInfoMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class IssuanceInfoRepository {
    
    private final IssuanceInfoMapper mapper;
    
    public IssuanceInfoRepository(IssuanceInfoMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询发文信息
     */
    public IssuanceInfo findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据文档ID查询发文信息
     */
    public IssuanceInfo findByDocumentId(Long documentId) {
        return mapper.selectOne(null); // TODO: 添加查询条件
    }
    
    /**
     * 保存发文信息
     */
    public void save(IssuanceInfo info) {
        if (info.getId() == null) {
            mapper.insert(info);
        } else {
            mapper.updateById(info);
        }
    }
    
    /**
     * 更新发文信息
     */
    public void update(IssuanceInfo info) {
        mapper.updateById(info);
    }
    
    /**
     * 删除发文信息
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

