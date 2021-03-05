package com.offcn.user.service;

import com.offcn.user.po.TMember;

public interface UserService {
    public void registUser(TMember member);
    public TMember login(String username,String password);
}
