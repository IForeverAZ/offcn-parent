package com.offcn.user.service.impl;

import com.offcn.user.enums.UserExceptionEnum;
import com.offcn.user.exception.UserException;
import com.offcn.user.mapper.TMemberMapper;
import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberExample;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private TMemberMapper memberMapper;
    @Override
    public void registUser(TMember member) {
        TMemberExample example = new TMemberExample();
        example.createCriteria().andLoginacctEqualTo(member.getLoginacct());
        long l = memberMapper.countByExample(example);
        if(l > 0 ){//手机号码已经被注册了
            throw new UserException(UserExceptionEnum.LOGINACCT_EXIST);
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(member.getUserpswd());//密码加密
        member.setUserpswd(encode);//设置密码
        member.setAuthstatus("0");//未实名认证
        member.setUsertype("0");
        member.setAccttype("2");
        member.setUsername(member.getLoginacct());//设置用户名
        memberMapper.insertSelective(member);
        System.out.println("数据插入成功");



    }
}
