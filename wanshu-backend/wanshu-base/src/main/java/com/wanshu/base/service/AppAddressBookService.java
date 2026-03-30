package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.mapper.AppAddressBookMapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.app.AppAddressBook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppAddressBookService {

    private final AppAddressBookMapper addressBookMapper;

    public List<AppAddressBook> listByUser(Long userId, String keyword) {
        LambdaQueryWrapper<AppAddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAddressBook::getUserId, userId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AppAddressBook::getName, keyword)
                    .or().like(AppAddressBook::getPhone, keyword)
                    .or().like(AppAddressBook::getAddress, keyword));
        }
        wrapper.orderByDesc(AppAddressBook::getIsDefault)
               .orderByDesc(AppAddressBook::getCreatedTime);
        return addressBookMapper.selectList(wrapper);
    }

    public AppAddressBook getById(Long id, Long userId) {
        AppAddressBook book = addressBookMapper.selectById(id);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return book;
    }

    @Transactional
    public void create(AppAddressBook book, Long userId) {
        book.setUserId(userId);
        if (book.getIsDefault() == null) {
            book.setIsDefault(0);
        }
        // @TableLogic 列表条件为 deleted=0；插入时若未赋值可能为 NULL，导致列表查不出
        if (book.getDeleted() == null) {
            book.setDeleted(0);
        }
        if (book.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressBookMapper.insert(book);
    }

    @Transactional
    public void update(Long id, AppAddressBook book, Long userId) {
        getById(id, userId);
        book.setId(id);
        book.setUserId(userId);
        if (Integer.valueOf(1).equals(book.getIsDefault())) {
            clearDefault(userId);
        }
        addressBookMapper.updateById(book);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        getById(id, userId);
        addressBookMapper.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids, Long userId) {
        for (Long id : ids) {
            getById(id, userId);
        }
        addressBookMapper.deleteBatchIds(ids);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        getById(id, userId);
        clearDefault(userId);
        AppAddressBook update = new AppAddressBook();
        update.setId(id);
        update.setIsDefault(1);
        addressBookMapper.updateById(update);
    }

    private void clearDefault(Long userId) {
        LambdaQueryWrapper<AppAddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAddressBook::getUserId, userId)
               .eq(AppAddressBook::getIsDefault, 1);
        List<AppAddressBook> defaults = addressBookMapper.selectList(wrapper);
        for (AppAddressBook d : defaults) {
            AppAddressBook update = new AppAddressBook();
            update.setId(d.getId());
            update.setIsDefault(0);
            addressBookMapper.updateById(update);
        }
    }
}
