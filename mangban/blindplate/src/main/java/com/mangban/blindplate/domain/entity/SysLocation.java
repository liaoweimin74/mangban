package com.mangban.blindplate.domain.entity;

import com.mangban.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_location")
public class SysLocation extends BaseEntity {
    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 500)
    private String remark;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @OrderBy("sortOrder ASC")
    private List<SysLocation> children = new ArrayList<>();

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<SysLocation> getChildren() { return children; }
    public void setChildren(List<SysLocation> children) { this.children = children; }
}