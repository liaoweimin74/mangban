package com.mangban.system;

import com.mangban.system.domain.entity.SysRole;
import com.mangban.system.repository.SysRoleMenuRepository;
import com.mangban.system.repository.SysRoleRepository;
import com.mangban.system.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 回归测试：角色菜单分配重复调用不应抛异常。
 *
 * 根因：RoleServiceImpl.assignMenus 使用 JPA 派生删除（select-then-remove），
 * DELETE 被延迟到事务提交且晚于 INSERT 执行，触发 sys_role_menu 的
 * (role_id, menu_id) 唯一约束冲突。详见 SysRoleMenuRepository。
 *
 * 隔离策略：
 * - @ActiveProfiles("test")：连接独立测试库 mangban_test（见
 *   src/test/resources/application-test.yml），与开发库 mangban 物理隔离
 * - @Transactional：每个测试在事务中运行并整体回滚，测试库也不残留数据
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleAssignMenusTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private SysRoleMenuRepository roleMenuRepository;

    @Test
    void assignMenusTwiceWithOverlappingMenus_shouldNotThrow() {
        SysRole role = new SysRole();
        role.setRoleName("测试角色");
        role.setRoleCode("TEST_ROLE_" + System.currentTimeMillis());
        role.setStatus(1);
        role = roleRepository.save(role);
        Long roleId = role.getId();

        Long[] firstMenus = { 1L, 2L, 3L };
        Long[] secondMenus = { 2L, 3L, 4L }; // 与第一次有交集

        // 第一次分配：成功（表里无旧记录）
        assertThatCode(() -> roleService.assignMenus(roleId, firstMenus)).doesNotThrowAnyException();

        // 第二次分配（修改）：修复前因 flush ordering 触发唯一约束冲突
        assertThatCode(() -> roleService.assignMenus(roleId, secondMenus)).doesNotThrowAnyException();
    }
}
