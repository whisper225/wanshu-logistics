package com.wanshu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.sys.SysMenu;
import com.wanshu.model.entity.sys.SysRoleMenu;
import com.wanshu.system.mapper.SysMenuMapper;
import com.wanshu.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public List<SysMenu> listAll() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder));
    }

    public List<Map<String, Object>> tree() {
        List<SysMenu> menus = listAll();
        return buildTree(menus, 0L);
    }

    private List<Map<String, Object>> buildTree(List<SysMenu> menus, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("id", menu.getId());
                node.put("parentId", menu.getParentId());
                node.put("menuName", menu.getMenuName());
                node.put("menuType", menu.getMenuType());
                node.put("path", menu.getPath());
                node.put("component", menu.getComponent());
                node.put("icon", menu.getIcon());
                node.put("sortOrder", menu.getSortOrder());
                node.put("visible", menu.getVisible());
                node.put("status", menu.getStatus());
                List<Map<String, Object>> children = buildTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                result.add(node);
            }
        }
        return result;
    }

    public SysMenu getById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return menu;
    }

    @Transactional
    public void create(SysMenu menu) {
        menuMapper.insert(menu);
    }

    @Transactional
    public void update(SysMenu menu) {
        menuMapper.updateById(menu);
    }

    @Transactional
    public void delete(Long id) {
        LambdaQueryWrapper<SysMenu> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysMenu::getParentId, id);
        if (menuMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("存在子菜单，不能删除");
        }
        menuMapper.deleteById(id);
        LambdaQueryWrapper<SysRoleMenu> rmWrapper = new LambdaQueryWrapper<>();
        rmWrapper.eq(SysRoleMenu::getMenuId, id);
        roleMenuMapper.delete(rmWrapper);
    }

    public List<SysMenu> getMenusByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        ).stream().map(SysRoleMenu::getMenuId).distinct().toList();

        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectBatchIds(menuIds);
    }
}
