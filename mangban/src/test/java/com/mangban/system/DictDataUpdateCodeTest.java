package com.mangban.system;

import com.mangban.system.domain.dto.DictDataUpdateRequest;
import com.mangban.system.domain.entity.SysDictData;
import com.mangban.system.domain.entity.SysDictType;
import com.mangban.system.repository.SysDictDataRepository;
import com.mangban.system.repository.SysDictTypeRepository;
import com.mangban.system.service.DictDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回归测试：编辑字典数据时支持更换字典分类（dictCode）。
 *
 * 需求：字典数据编辑表单应能通过 lookup 选择字典分类，
 * 后端 update 需支持更新 dictCode。
 *
 * 隔离策略：@ActiveProfiles("test") 连独立测试库 mangban_test；
 * @Transactional 使测试整体回滚，不残留数据。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DictDataUpdateCodeTest {

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private SysDictDataRepository dictDataRepository;

    @Autowired
    private SysDictTypeRepository dictTypeRepository;

    @Test
    void update_withNewDictCode_shouldMoveDataToNewType() {
        // 准备：两个字典类型
        SysDictType typeA = new SysDictType();
        typeA.setDictName("类型A");
        typeA.setDictCode("TYPE_A_" + System.currentTimeMillis());
        typeA.setStatus(1);
        typeA = dictTypeRepository.save(typeA);

        SysDictType typeB = new SysDictType();
        typeB.setDictName("类型B");
        typeB.setDictCode("TYPE_B_" + System.currentTimeMillis());
        typeB.setStatus(1);
        typeB = dictTypeRepository.save(typeB);

        // 准备：一条属于类型A的字典数据
        SysDictData dd = new SysDictData();
        dd.setDictCode(typeA.getDictCode());
        dd.setLabel("测试标签");
        dd.setValue("test_value");
        dd.setSortOrder(1);
        dd.setStatus(1);
        dd = dictDataRepository.save(dd);

        // 执行：编辑时更换分类到类型B
        DictDataUpdateRequest request = new DictDataUpdateRequest(
                typeB.getDictCode(), "测试标签-改", "test_value_2", 2, null);
        var updated = dictDataService.update(dd.getId(), request);

        // 断言：dictCode 已更新到类型B，其余字段也更新
        assertThat(updated.dictCode()).isEqualTo(typeB.getDictCode());
        assertThat(updated.label()).isEqualTo("测试标签-改");
        assertThat(updated.value()).isEqualTo("test_value_2");
        assertThat(updated.sortOrder()).isEqualTo(2);

        // 持久层验证：原记录 dictCode 确实变更
        SysDictData reloaded = dictDataRepository.findById(dd.getId()).orElseThrow();
        assertThat(reloaded.getDictCode()).isEqualTo(typeB.getDictCode());
    }

    @Test
    void update_withoutDictCode_shouldKeepOriginalType() {
        // 准备：一个字典类型 + 一条数据
        SysDictType typeA = new SysDictType();
        typeA.setDictName("类型A");
        typeA.setDictCode("TYPE_A_" + System.currentTimeMillis());
        typeA.setStatus(1);
        typeA = dictTypeRepository.save(typeA);

        SysDictData dd = new SysDictData();
        dd.setDictCode(typeA.getDictCode());
        dd.setLabel("原标签");
        dd.setValue("orig");
        dd.setSortOrder(1);
        dd.setStatus(1);
        dd = dictDataRepository.save(dd);

        // 执行：不传 dictCode（老逻辑兼容），只改 label
        DictDataUpdateRequest request = new DictDataUpdateRequest(
                null, "新标签", null, null, null);
        var updated = dictDataService.update(dd.getId(), request);

        // 断言：dictCode 不变
        assertThat(updated.dictCode()).isEqualTo(typeA.getDictCode());
        assertThat(updated.label()).isEqualTo("新标签");
    }
}
