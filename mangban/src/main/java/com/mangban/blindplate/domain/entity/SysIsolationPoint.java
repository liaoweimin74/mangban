package com.mangban.blindplate.domain.entity;

import com.mangban.common.domain.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sys_isolation_point")
public class SysIsolationPoint extends BaseEntity {
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String medium;

    @Column(name = "pressure_rating", length = 50)
    private String pressureRating;

    @Column(name = "temperature_rating", length = 50)
    private String temperatureRating;

    @Column(name = "hazard_level", length = 20)
    private String hazardLevel;

    @Column(name = "point_type", length = 50)
    private String pointType;

    @Column(name = "blind_spec", length = 100)
    private String blindSpec;

    @Column(name = "equipment_tag", length = 50)
    private String equipmentTag;

    @Column(name = "pipeline_no", length = 50)
    private String pipelineNo;

    @Column(length = 20)
    private String status = "OPEN";

    @Column(name = "occupy_status", length = 20)
    private String occupyStatus = "FREE";

    @Column(length = 500)
    private String remark;

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    public String getPressureRating() { return pressureRating; }
    public void setPressureRating(String pressureRating) { this.pressureRating = pressureRating; }
    public String getTemperatureRating() { return temperatureRating; }
    public void setTemperatureRating(String temperatureRating) { this.temperatureRating = temperatureRating; }
    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }
    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }
    public String getBlindSpec() { return blindSpec; }
    public void setBlindSpec(String blindSpec) { this.blindSpec = blindSpec; }
    public String getEquipmentTag() { return equipmentTag; }
    public void setEquipmentTag(String equipmentTag) { this.equipmentTag = equipmentTag; }
    public String getPipelineNo() { return pipelineNo; }
    public void setPipelineNo(String pipelineNo) { this.pipelineNo = pipelineNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOccupyStatus() { return occupyStatus; }
    public void setOccupyStatus(String occupyStatus) { this.occupyStatus = occupyStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}