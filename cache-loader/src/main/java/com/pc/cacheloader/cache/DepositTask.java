package com.pc.cacheloader.cache;

import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.model.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositTask<T extends BaseDO> extends BaseDO {

    private T origin;   //原始对象
    private T transfer; //封装对象
    private T history;  //历史缓存

    private Class loader;
    private Integer executeStep;
    private LocalDateTime initTime; //任务初始化时间
    ActionType actionType;

    @Override
    public Long getChannel() {
        return origin.getChannel();
    }
}
