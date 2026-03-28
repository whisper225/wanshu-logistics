package com.wanshu.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanshu.model.entity.base.BaseFreightTemplateEconomicZone;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BaseFreightTemplateEconomicZoneMapper extends BaseMapper<BaseFreightTemplateEconomicZone> {

    @Select("SELECT economic_zone_id FROM base_freight_template_economic_zone WHERE template_id = #{templateId}")
    List<Long> listEconomicZoneIdsByTemplateId(@Param("templateId") Long templateId);

    @Delete("DELETE FROM base_freight_template_economic_zone WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);

    @Select("""
            SELECT COUNT(*) FROM base_freight_template_economic_zone
            WHERE economic_zone_id = #{zoneId}
            AND template_id <> #{excludeTemplateId}
            """)
    int countByZoneIdExcludingTemplate(@Param("zoneId") Long zoneId, @Param("excludeTemplateId") Long excludeTemplateId);

    @Select("""
            SELECT COUNT(*) FROM base_freight_template_economic_zone
            WHERE economic_zone_id = #{zoneId}
            """)
    int countByZoneId(@Param("zoneId") Long zoneId);
}
